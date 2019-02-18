/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.ec2.providers;

import static com.appdynamics.extensions.aws.Constants.DEFAULT_THREAD_TIMEOUT;
import static com.appdynamics.extensions.aws.util.AWSUtil.createAWSClientConfiguration;
import static com.appdynamics.extensions.aws.util.AWSUtil.createAWSCredentials;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.appdynamics.extensions.MonitorExecutorService;
import com.appdynamics.extensions.MonitorThreadPoolExecutor;
import com.appdynamics.extensions.aws.config.Account;
import com.appdynamics.extensions.aws.config.CredentialsDecryptionConfig;
import com.appdynamics.extensions.aws.config.ProxyConfig;
import com.appdynamics.extensions.aws.ec2.config.Ec2InstanceNameConfig;
import com.appdynamics.extensions.aws.ec2.config.Tag;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Florencio Sarmiento
 */
public class EC2InstanceNameProvider {

    private static final Logger LOGGER = Logger.getLogger(EC2InstanceNameProvider.class);

    private AtomicReference<List<Account>> accounts = new AtomicReference<List<Account>>();

    private AtomicReference<CredentialsDecryptionConfig> credentialsDecryptionConfig =
            new AtomicReference<CredentialsDecryptionConfig>();

    private AtomicReference<ProxyConfig> proxyConfig =
            new AtomicReference<ProxyConfig>();

    private AtomicReference<Ec2InstanceNameConfig> ec2InstanceNameConfig =
            new AtomicReference<Ec2InstanceNameConfig>();

    private AtomicReference<List<Tag>> tags =
            new AtomicReference<List<Tag>>();

    private Map<String, InstanceNameDictionary> accountInstancesDictionaries =
            new ConcurrentHashMap<String, InstanceNameDictionary>();

    private volatile int maxErrorRetrySize;

    private static final int SLEEP_TIME_IN_MINS = 5;

    private static final String EC2_REGION_ENDPOINT = "ec2.%s.amazonaws.com";

    private AtomicBoolean initialised = new AtomicBoolean(false);

    private MonitorExecutorService ec2WorkerPool = new MonitorThreadPoolExecutor(new ScheduledThreadPoolExecutor(3));


    private static EC2InstanceNameProvider instance;

    private EC2InstanceNameProvider() {
    }

    public static EC2InstanceNameProvider getInstance() {
        if (instance == null) {
            instance = new EC2InstanceNameProvider();
        }

        return instance;
    }

    public void initialise(List<Account> accounts,
                           CredentialsDecryptionConfig credentialsDecryptionConfig,
                           ProxyConfig proxyConfig,
                           Ec2InstanceNameConfig ec2InstanceNameConfig,
                           List<Tag> tags, int maxErrorRetrySize) {

        this.accounts.set(accounts);
        this.credentialsDecryptionConfig.set(credentialsDecryptionConfig);
        this.proxyConfig.set(proxyConfig);
        this.ec2InstanceNameConfig.set(ec2InstanceNameConfig);
        this.tags.set(tags);
        this.maxErrorRetrySize = maxErrorRetrySize;

        if (!initialised.get()) {
            LOGGER.info("Initialiasing EC2 instance names...");
            retrieveInstances();
            initiateBackgroundTask(SLEEP_TIME_IN_MINS);
            initialised.set(true);
        }
    }

    /**
     * Instance name should be available post initialisation {@link EC2InstanceNameProvider#initialise}.
     * <p>
     * <p>It's possible that a new instance is created post this, so name wouldn't have been retrieved yet.
     * Background task may pick this up (depends on timing), but if not or for whatever reason the instance name
     * isn't available at the time, this method will attempt to retrieve it and return if available.
     * Otherwise, the name is set the same as the ID, until the background task runs again to refresh it.
     *
     * @param accountName - the accountName
     * @param region      - the region
     * @param instanceId  - the instanceId
     * @return the instanceName
     */
    public String getInstanceName(String accountName, String region, String instanceId) {
        String instanceName = null;

        if (this.ec2InstanceNameConfig.get().isUseInstanceName()) {
            InstanceNameDictionary accountInstancesDictionary = getAccountInstanceNameDictionary(accountName);
            instanceName = accountInstancesDictionary.getEc2Instaces().get(instanceId);

            if (StringUtils.isBlank(instanceName)) {
                try {
                    retrieveInstancesPerAccountPerRegion(getAccount(accountName), region,
                            accountInstancesDictionary, instanceId);

                } catch (Exception e) {
                    LOGGER.error(String.format("Error while retrieving instance name for Account [%s] Region [%s]",
                            accountName, region), e);
                }

                instanceName = accountInstancesDictionary.getEc2Instaces().get(instanceId);

                if (StringUtils.isBlank(instanceName)) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("No name found for ec2 instance with id " + instanceId);
                    }

                    accountInstancesDictionary.addEc2Instance(instanceId, instanceId);
                    instanceName = instanceId;
                }
            }

        } else {
            instanceName = instanceId;
        }

        return instanceName;
    }

    public InstanceNameDictionary getInstanceNameDictionary(String accountName) {
        return accountInstancesDictionaries.get(accountName);
    }

    private void initiateBackgroundTask(long delay) {
        LOGGER.info("Initiating background task...");

        MonitorExecutorService executorService = new MonitorThreadPoolExecutor(new ScheduledThreadPoolExecutor(1));

        executorService.scheduleAtFixedRate("InstanceNameFetcher",
                new Runnable() {

                    public void run() {
                        retrieveInstances();
                    }
                },
                (int) delay, SLEEP_TIME_IN_MINS, TimeUnit.MINUTES);
    }

    private void retrieveInstances() {
        LOGGER.info("Retrieving ec2 instances' names...");

        List<FutureTask<Void>> allFutureTasks = new ArrayList<FutureTask<Void>>();
        for (Account account : accounts.get()) {
            try {
                InstanceNameDictionary accountInstancesDictionary = getAccountInstanceNameDictionary(account.getDisplayAccountName());
                allFutureTasks.addAll(addParallelTask(account, accountInstancesDictionary));
            } catch (IllegalArgumentException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Issue while creating task for Account [%s]",
                            account.getDisplayAccountName()), e);
                }
            }
        }

        checkAllTasksCompleted(allFutureTasks);
    }

    private void checkAllTasksCompleted(List<FutureTask<Void>> futureTasks) {
        for (FutureTask<Void> task : futureTasks) {
            try {
                task.get(DEFAULT_THREAD_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Task interrupted. ", e);
            } catch (ExecutionException e) {
                LOGGER.error("Task execution failed. ", e);
            } catch (TimeoutException e) {
                LOGGER.error("Task timed out. ", e);
            }
        }
    }

    private List<FutureTask<Void>> addParallelTask(Account account, InstanceNameDictionary accountInstancesDictionary) {

        List<FutureTask<Void>> futureTasks = Lists.newArrayList();


        for (final String region : account.getRegions()) {
            InstancesPerAccountPerRegionCollectorTask ec2NamesCollectorTask = new InstancesPerAccountPerRegionCollectorTask(account, region, accountInstancesDictionary);
            FutureTask<Void> ec2NamesTaskExecutor = new FutureTask<Void>(ec2NamesCollectorTask);

            ec2WorkerPool.submit("EC2NamesCollector", ec2NamesTaskExecutor);
            futureTasks.add(ec2NamesTaskExecutor);
        }

        return futureTasks;
    }

    private class InstancesPerAccountPerRegionCollectorTask implements Callable<Void> {

        private Account account;
        private String region;
        private InstanceNameDictionary accountInstanceNameDictionary;

        InstancesPerAccountPerRegionCollectorTask(Account account, String region, InstanceNameDictionary accountInstanceNameDictionary) {
            this.account = account;
            this.region = region;
            this.accountInstanceNameDictionary = accountInstanceNameDictionary;

        }

        @Override
        public Void call() {
            try {
                retrieveInstancesPerAccountPerRegion(account, region, accountInstanceNameDictionary);
            } catch (Exception e) {
                LOGGER.error(String.format("Error while retrieving EC2 instances for Account [%s] Region [%s]",
                        account.getDisplayAccountName(), region), e);
            }
            return null;
        }
    }

    private InstanceNameDictionary getAccountInstanceNameDictionary(String accountName) {
        InstanceNameDictionary accountInstancesDictionary =
                accountInstancesDictionaries.get(accountName);

        if (accountInstancesDictionary == null) {
            accountInstancesDictionary = new InstanceNameDictionary();
            accountInstancesDictionaries.put(accountName, accountInstancesDictionary);
        }

        return accountInstancesDictionary;
    }

    private void retrieveInstancesPerAccountPerRegion(Account account, String region,
                                                      InstanceNameDictionary accountInstanceNameDictionary, String... instanceIds) {

        AWSCredentials awsCredentials = null;
        if (StringUtils.isNotEmpty(account.getAwsAccessKey()) && StringUtils.isNotEmpty(account.getAwsSecretKey())) {
            awsCredentials = createAWSCredentials(account, credentialsDecryptionConfig.get());
        }

        ClientConfiguration awsClientConfig = createAWSClientConfiguration(maxErrorRetrySize, proxyConfig.get());

        AmazonEC2Client ec2Client = null;
        if (awsCredentials == null) {
            LOGGER.info("Credentials not provided trying to use instance profile credentials");
            ec2Client = new AmazonEC2Client(new InstanceProfileCredentialsProvider(), awsClientConfig);
        } else {
            ec2Client = new AmazonEC2Client(awsCredentials, awsClientConfig);
        }

        ec2Client.setEndpoint(String.format(EC2_REGION_ENDPOINT, region));

        List<Filter> filters = createFilters(tags);

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.setFilters(filters);

        if (instanceIds != null && instanceIds.length > 0) {
            request.setInstanceIds(Arrays.asList(instanceIds));
        }

        DescribeInstancesResult result = ec2Client.describeInstances(request);

        if (result != null) {
            List<Reservation> reservations = result.getReservations();

            while (StringUtils.isNotBlank(result.getNextToken())) {
                request.setNextToken(result.getNextToken());
                result = ec2Client.describeInstances(request);
                reservations.addAll(result.getReservations());
            }

            for (Reservation reservation : result.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    accountInstanceNameDictionary.addEc2Instance(instance.getInstanceId(),
                            getInstanceName(instance.getInstanceId(), instance.getTags()));
                }
            }
        }
    }

    private List<Filter> createFilters(AtomicReference<List<Tag>> tags) {

        List<Filter> filters = new ArrayList<Filter>();

        List<Tag> allTags = tags.get();

        if (allTags != null && allTags.size() > 0) {

            Filter tagWithoutKeyValue = new Filter("tag-key");

            for (Tag tag : allTags) {

                String name = tag.getName();
                List<String> value = tag.getValue();

                if ((value == null || value.size() == 0) && !Strings.isNullOrEmpty(name)) {
                    tagWithoutKeyValue.getValues().add(name);
                } else {
                    if (Strings.isNullOrEmpty(name)) {
                        if (value != null && value.size() > 0) {
                            LOGGER.info("Tag name can not be null. Ignoring the value [ " + value + " ]");
                        }
                        continue;
                    }
                    Filter tagWithKeyValue = new Filter("tag:" + name);
                    tagWithKeyValue.withValues(value);
                    filters.add(tagWithKeyValue);
                }

                if (tagWithoutKeyValue.getValues().size() > 0) {
                    filters.add(tagWithoutKeyValue);
                }
            }
        }
        return filters;
    }

    private String getInstanceName(String defaultValue, List<com.amazonaws.services.ec2.model.Tag> tags) {
        for (com.amazonaws.services.ec2.model.Tag tag : tags) {
            if (ec2InstanceNameConfig.get().getTagKey().equals(tag.getKey())) {
                if (StringUtils.isNotBlank(tag.getValue())) {
                    return tag.getValue();
                }

                break;
            }
        }

        return defaultValue;
    }

    private Account getAccount(String accountName) {
        for (Account account : accounts.get()) {
            if (account.getDisplayAccountName().equalsIgnoreCase(accountName)) {
                return account;
            }
        }

        return null;
    }
}
