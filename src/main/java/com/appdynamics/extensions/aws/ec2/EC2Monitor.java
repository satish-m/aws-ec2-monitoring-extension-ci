/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.ec2;

import static com.appdynamics.extensions.aws.Constants.METRIC_PATH_SEPARATOR;

import com.appdynamics.extensions.aws.SingleNamespaceCloudwatchMonitor;
import com.appdynamics.extensions.aws.collectors.NamespaceMetricStatisticsCollector;
import com.appdynamics.extensions.aws.ec2.config.EC2Configuration;
import com.appdynamics.extensions.aws.ec2.providers.EC2InstanceNameProvider;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.google.common.collect.Lists;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Florencio Sarmiento
 */
public class EC2Monitor extends SingleNamespaceCloudwatchMonitor<EC2Configuration> {

    private static final Logger LOGGER = Logger.getLogger(EC2Monitor.class);

    private static final String DEFAULT_METRIC_PREFIX = String.format("%s%s%s%s",
            "Custom Metrics", METRIC_PATH_SEPARATOR, "Amazon EC2", METRIC_PATH_SEPARATOR);

    public EC2Monitor() {
        super(EC2Configuration.class);
        LOGGER.info(String.format("Using AWS EC2 Monitor Version [%s]",
                this.getClass().getPackage().getImplementationTitle()));
    }

    protected void initialiseServiceProviders(EC2Configuration config) {

        EC2InstanceNameProvider ec2InstanceNameProvider = EC2InstanceNameProvider.getInstance();
        ec2InstanceNameProvider.initialise(config.getAccounts(),
                config.getCredentialsDecryptionConfig(),
                config.getProxyConfig(), config.getEc2InstanceNameConfig(), config.getTags(),
                config.getMetricsConfig().getMaxErrorRetrySize());
    }

    @Override
    public String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return "EC2Monitor";
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return Lists.newArrayList();
    }

    @Override
    protected void initialize(EC2Configuration config) {
        super.initialize(config);
        initialiseServiceProviders(config);
    }

    @Override
    protected NamespaceMetricStatisticsCollector getNamespaceMetricsCollector(
            EC2Configuration config) {
        MetricsProcessor metricsProcessor = createMetricsProcessor(config);

        return new NamespaceMetricStatisticsCollector
                .Builder(config.getAccounts(),
                config.getConcurrencyConfig(),
                config.getMetricsConfig(),
                metricsProcessor,
                config.getMetricPrefix(),
                config.getCustomDashboard(),
                config.getControllerInfo())
                .withCredentialsDecryptionConfig(config.getCredentialsDecryptionConfig())
                .withProxyConfig(config.getProxyConfig())
                .build();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private MetricsProcessor createMetricsProcessor(EC2Configuration config) {
        return new EC2MetricsProcessor(
                config.getMetricsConfig().getIncludeMetrics(),
                config.getEc2Instance());
    }

    public static void main(String[] args) throws TaskExecutionException, IOException {

        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        ca.setThreshold(Level.DEBUG);
        LOGGER.getRootLogger().addAppender(ca);


        /*FileAppender fa = new FileAppender(new PatternLayout("%-5p [%t]: %m%n"), "cache.log");
        fa.setThreshold(Level.DEBUG);
        LOGGER.getRootLogger().addAppender(fa);*/


        EC2Monitor monitor = new EC2Monitor();


        Map<String, String> taskArgs = new HashMap<>();
        taskArgs.put("config-file", "/Users/Muddam/AppDynamics/CI/aws-ec2-monitoring-extension-ci/src/main/resources/conf/config.yml");

        //monitor.execute(taskArgs, null);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    monitor.execute(taskArgs, null);
                } catch (Exception e) {
                    LOGGER.error("Error while running the task", e);
                }
            }
        }, 2, 60, TimeUnit.SECONDS);

    }


}