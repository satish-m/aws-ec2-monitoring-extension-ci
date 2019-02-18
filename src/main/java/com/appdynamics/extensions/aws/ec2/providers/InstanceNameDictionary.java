/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.ec2.providers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Florencio Sarmiento
 *
 */
public class InstanceNameDictionary {
	
	private Map<String, String> ec2Instaces = new ConcurrentHashMap<String, String>();
	
	public Map<String, String> getEc2Instaces() {
		return ec2Instaces;
	}

	public void setEc2Instaces(Map<String, String> ec2Instaces) {
		this.ec2Instaces = new ConcurrentHashMap<String, String>(ec2Instaces);
	}
	
	public void addEc2Instance(String instanceId, String instanceName) {
		this.ec2Instaces.put(instanceId, instanceName);
	}
}
