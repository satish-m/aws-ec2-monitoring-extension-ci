/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.ec2.config;

import com.appdynamics.extensions.aws.config.Configuration;

import java.util.List;

/**
 * @author Florencio Sarmiento
 */
public class EC2Configuration extends Configuration {

    private Ec2InstanceNameConfig ec2InstanceNameConfig;
    private String ec2Instance;
    private List<Tag> tags;

    public Ec2InstanceNameConfig getEc2InstanceNameConfig() {
        return ec2InstanceNameConfig;
    }

    public void setEc2InstanceNameConfig(Ec2InstanceNameConfig ec2InstanceNameConfig) {
        this.ec2InstanceNameConfig = ec2InstanceNameConfig;
    }

    public String getEc2Instance() {
        return ec2Instance;
    }

    public void setEc2Instance(String ec2Instance) {
        this.ec2Instance = ec2Instance;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
