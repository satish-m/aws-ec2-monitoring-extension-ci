/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.ec2;

import com.amazonaws.services.cloudwatch.model.Metric;
import com.appdynamics.extensions.aws.ec2.providers.EC2InstanceNameProvider;
import com.appdynamics.extensions.aws.ec2.providers.InstanceNameDictionary;
import com.google.common.base.Predicate;

import java.util.Set;

/**
 * @author Satish Muddam
 */
public class EC2MetricPredicate implements Predicate<Metric> {
    private InstanceNameDictionary instanceNameDictionary;

    public EC2MetricPredicate(String accountName) {
        EC2InstanceNameProvider ec2InstanceNameProvider = EC2InstanceNameProvider.getInstance();
        instanceNameDictionary = ec2InstanceNameProvider.getInstanceNameDictionary(accountName);
    }

    public boolean apply(Metric metric) {

        Set<String> ec2InstanceIds = instanceNameDictionary.getEc2Instaces().keySet();

        String instanceId = metric.getDimensions().get(0).getValue();
        if (ec2InstanceIds.contains(instanceId)) {
            return true;
        }
        return false;
    }
}