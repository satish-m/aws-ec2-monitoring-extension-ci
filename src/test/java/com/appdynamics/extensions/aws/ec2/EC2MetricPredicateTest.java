/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.aws.ec2;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.appdynamics.extensions.aws.ec2.providers.EC2InstanceNameProvider;
import com.appdynamics.extensions.aws.ec2.providers.InstanceNameDictionary;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

/**
 * @author Satish Muddam
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({EC2InstanceNameProvider.class,
        InstanceNameDictionary.class})
@PowerMockIgnore({"org.apache.*, javax.xml.*"})
public class EC2MetricPredicateTest {

    @Mock
    private EC2InstanceNameProvider ec2InstanceNameProvider;

    @Mock
    private InstanceNameDictionary instanceNameDictionary;

    @Mock
    private Metric metric;

    @Mock
    private Dimension dimension;

    private EC2MetricPredicate classUnderTest;

    @Before
    public void setUp() {
        mockStatic(EC2InstanceNameProvider.class);
        when(EC2InstanceNameProvider.getInstance()).thenReturn(ec2InstanceNameProvider);
        when(ec2InstanceNameProvider.getInstanceNameDictionary(anyString())).thenReturn(instanceNameDictionary);
    }

    @Test
    public void matchedInstanceMetricShouldPass() {

        classUnderTest = new EC2MetricPredicate("test");

        Map<String, String> instances = Maps.newHashMap();
        instances.put("id1", "name1");
        instances.put("id2", "name2");

        when(instanceNameDictionary.getEc2Instaces()).thenReturn(instances);
        when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
        when(dimension.getValue()).thenReturn("id1");

        Assert.assertTrue("Matched instance should pass", classUnderTest.apply(metric));
    }

    @Test
    public void unMatchedInstanceMetricShouldNotPass() {

        classUnderTest = new EC2MetricPredicate("test");

        Map<String, String> instances = Maps.newHashMap();
        instances.put("id1", "name1");
        instances.put("id2", "name2");

        when(instanceNameDictionary.getEc2Instaces()).thenReturn(instances);
        when(metric.getDimensions()).thenReturn(Lists.newArrayList(dimension));
        when(dimension.getValue()).thenReturn("id3");

        Assert.assertFalse("Unmatched instance should not pass", classUnderTest.apply(metric));
    }
}