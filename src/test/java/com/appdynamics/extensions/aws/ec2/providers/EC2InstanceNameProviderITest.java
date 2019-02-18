/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.ec2.providers;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.appdynamics.extensions.aws.config.Account;
import com.appdynamics.extensions.aws.config.CredentialsDecryptionConfig;
import com.appdynamics.extensions.aws.config.ProxyConfig;
import com.appdynamics.extensions.aws.ec2.config.Ec2InstanceNameConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Satish Muddam
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({EC2InstanceNameProvider.class,
        InstanceNameDictionary.class})
@PowerMockIgnore({"org.apache.*, javax.xml.*"})
public class EC2InstanceNameProviderITest {

    @Mock
    private Account account;

    @Mock
    private CredentialsDecryptionConfig credentialsDecryptionConfig;

    @Mock
    private ProxyConfig proxyConfig;

    @Mock
    private Ec2InstanceNameConfig ec2InstanceNameConfig;

    @Mock
    private DescribeInstancesResult describeInstancesResult;

    private EC2InstanceNameProvider classUnderTest;

    @Before
    public void init() throws Exception {
        Mockito.when(account.getDisplayAccountName()).thenReturn("TestAccount");
        Mockito.when(account.getRegions()).thenReturn(Sets.newHashSet("TestRegion"));
        Mockito.when(account.getAwsAccessKey()).thenReturn("TestAccessKey");
        Mockito.when(account.getAwsSecretKey()).thenReturn("TestSecretKey");

        AmazonEC2Client amazonEC2Client = PowerMockito.mock(AmazonEC2Client.class);

        PowerMockito.whenNew(AmazonEC2Client.class).withAnyArguments().thenReturn(amazonEC2Client);

        Mockito.when(amazonEC2Client.describeInstances(Matchers.any(DescribeInstancesRequest.class))).thenReturn(describeInstancesResult);

        classUnderTest = EC2InstanceNameProvider.getInstance();
    }

    @After
    public void destroy() throws IllegalAccessException {

        //resetting the initialised to false in EC2InstanceNameProvider
        Field field = MemberModifier.field(EC2InstanceNameProvider.class, "initialised");
        field.setAccessible(true);
        field.set(classUnderTest, new AtomicBoolean(false));
    }

    @Test
    public void testInstanceIdIsReturnedWhenUseNameInMetricsIsFalse() {


        Mockito.when(ec2InstanceNameConfig.isUseInstanceName()).thenReturn(false);


        Reservation reservation = Mockito.mock(Reservation.class);

        Mockito.when(describeInstancesResult.getReservations()).thenReturn(Lists.newArrayList(reservation));
        Mockito.when(describeInstancesResult.getNextToken()).thenReturn("");

        Instance instance1 = Mockito.mock(Instance.class);
        Mockito.when(instance1.getInstanceId()).thenReturn("Instance1");

        Instance instance2 = Mockito.mock(Instance.class);
        Mockito.when(instance2.getInstanceId()).thenReturn("Instance2");


        Mockito.when(reservation.getInstances()).thenReturn(Lists.newArrayList(instance1, instance2));


        classUnderTest.initialise(Lists.newArrayList(account), credentialsDecryptionConfig, proxyConfig,
                ec2InstanceNameConfig, Lists.<com.appdynamics.extensions.aws.ec2.config.Tag>newArrayList(), 2);

        String instanceName1 = classUnderTest.getInstanceName("TestAccount",
                "TestRegion", "Instance1");
        Assert.assertEquals("Instance1", instanceName1);

        String instanceName2 = classUnderTest.getInstanceName("TestAccount",
                "TestRegion", "Instance2");
        Assert.assertEquals("Instance2", instanceName2);

    }

    @Test
    public void testInstanceNameIsReturnedWhenUseNameInMetricsIsTrue() throws Exception {

        Mockito.when(ec2InstanceNameConfig.isUseInstanceName()).thenReturn(true);
        Mockito.when(ec2InstanceNameConfig.getTagKey()).thenReturn("Name");

        Reservation reservation = Mockito.mock(Reservation.class);

        Mockito.when(describeInstancesResult.getReservations()).thenReturn(Lists.newArrayList(reservation));
        Mockito.when(describeInstancesResult.getNextToken()).thenReturn("");

        Instance instance1 = Mockito.mock(Instance.class);
        Mockito.when(instance1.getInstanceId()).thenReturn("Instance1");
        Mockito.when(instance1.getTags()).thenReturn(Lists.newArrayList(new Tag("Name", "InstanceName1")));

        Instance instance2 = Mockito.mock(Instance.class);
        Mockito.when(instance2.getInstanceId()).thenReturn("Instance2");
        Mockito.when(instance2.getTags()).thenReturn(Lists.newArrayList(new Tag("Name", "InstanceName2")));


        Mockito.when(reservation.getInstances()).thenReturn(Lists.newArrayList(instance1, instance2));


        EC2InstanceNameProvider classUnderTest = EC2InstanceNameProvider.getInstance();
        classUnderTest.initialise(Lists.newArrayList(account), credentialsDecryptionConfig, proxyConfig,
                ec2InstanceNameConfig, Lists.<com.appdynamics.extensions.aws.ec2.config.Tag>newArrayList(), 2);

        String instanceName1 = classUnderTest.getInstanceName("TestAccount",
                "TestRegion", "Instance1");
        Assert.assertEquals("InstanceName1", instanceName1);

        String instanceName2 = classUnderTest.getInstanceName("TestAccount",
                "TestRegion", "Instance2");
        Assert.assertEquals("InstanceName2", instanceName2);

    }
}
