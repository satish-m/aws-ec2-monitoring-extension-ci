/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.ec2.config;

/**
 * @author Florencio Sarmiento
 *
 */
public class Ec2InstanceNameConfig {
	
	private String useNameInMetrics;

	private String tagKey;
	
	public boolean isUseInstanceName() {
		return Boolean.valueOf(getUseNameInMetrics());
	}

	public String getUseNameInMetrics() {
		return useNameInMetrics;
	}

	public void setUseNameInMetrics(String useNameInMetrics) {
		this.useNameInMetrics = useNameInMetrics;
	}

	public String getTagKey() {
		return tagKey;
	}

	public void setTagKey(String tagKey) {
		this.tagKey = tagKey;
	}

}
