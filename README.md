# AWS EC2 Monitoring Extension

## Use Case
Captures statistics for EC2 instances from Amazon CloudWatch and displays them in the AppDynamics Metric Browser.

## Prerequisites
Please give the following permissions to the account being used to with the extension. 
1. **cloudwatch:ListMetrics**
2. **cloudwatch:GetMetricStatistics**
3. **ec2:describeinstances**

In order to use this extension, you do need a [Standalone JAVA Machine Agent](https://docs.appdynamics.com/display/PRO44/Standalone+Machine+Agents) or [SIM Agent](https://docs.appdynamics.com/display/PRO44/Server+Visibility).  For more details on downloading these products, please  visit [here](https://download.appdynamics.com/).

The extension needs to be able to connect to AWS Cloudwatch in order to collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.


## Installation

1. Run 'mvn clean install' from aws-ec2-monitoring-extension
2. Copy and unzip AWSEC2Monitor-\<version\>.zip from 'target' directory into \<machine_agent_dir\>/monitors/
3. Edit config.yml file in AWSEC2Monitor and provide the required configuration (see Configuration section)
4. Restart the Machine Agent.

Please place the extension in the "**monitors**" directory of your Machine Agent installation directory. Do not place the extension in the "**extensions**" directory of your Machine Agent installation directory.

## Configuration

In order to use the extension, you need to update the config.yml file that is present in the extension folder. The following is an explanation of the configurable fields that are present in the config.yml file.

### config.yaml

**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

| Section | Fields | Description | Example |
| ----- | ----- | ----- | ----- |
| **accounts** | | Fields under this section can be repeated for multiple accounts config |  |
| | awsAccessKey | AWS Access Key, keep it empty if using instance profile |  |
| | awsSecretKey | AWS Secret Key, keep it empty if using instance profile |  |
| | displayAccountName | Display name used in metric path | "MyAWSEC2" |
| | regions | Regions where ec2 is registered | "ap-southeast-1",<br/>"ap-southeast-2",<br/>"ap-northeast-1",<br/>"eu-central-1",<br/>"eu-west-1",<br/>"us-east-1",<br/>"us-west-1",<br/>"us-west-2",<br/>"sa-east-1" |
| **credentialsDecryptionConfig** | ----- | ----- | ----- |
| | enableDecryption | If set to "true", then all aws credentials provided (access key and secret key) will be decrypted - see AWS Credentials Encryption section |  |
| | encryptionKey | The key used when encypting the credentials |  |
| **proxyConfig** | ----- | ----- | ----- |
| | host | The proxy host (must also specify port) |  |
| | port | The proxy port (must also specify host) |  |
| | username | The proxy username (optional)  |  |
| | password | The proxy password (optional)  |  |
| **ec2InstanceNameConfig** | ----- | ----- | ----- |
| | useNameInMetrics | Set to "true" if you wish to display the instance name rather than  instance Id in the metric browser. Note, name must be configured in your EC2 instance. |  |
| | tagKey | Tag name of the ec2 instance name which is "Name" by default.  |  |
| **tags** | ----- | ----- | ----- |
| | name | Tag name to filter the ec2 instances. If value for this tag is not specified, extension will get all the instances with this tag. |  |
| | value | Tag value to filter the ec2 instances.|  |
| **cloudWatchMonitoring** |  | Monitoring type of the CloudWatch. Allowed values are Basic and Detailed. In Basic mode extension sends API calls every 5 minues. In Detailed mode extension sends API calls every minute. |  |
| **cloudWatchMonitoringInterval** |  | If you want to send API calls in other than the above defined intervals configure it here in minutes |  |
| **concurrencyConfig** |  |  |  |
| | noOfAccountThreads | The no of threads to process multiple accounts concurrently | 3 |
| | noOfRegionThreadsPerAccount | The no of threads to process multiple regions per account concurrently | 3 |
| | noOfMetricThreadsPerRegion | The no of threads to process multiple metrics per region concurrently | 3 |
| | threadTimeOut | Default timeout in seconds to be configured for the above threads | 30 |
| **regionEndPoints** |  | All the region end points for the AWS cloudwatch to be used in the extension. Refer https://docs.aws.amazon.com/general/latest/gr/rande.html#cw_region |  |
| | ap-southeast-1 | monitoring.ap-southeast-1.amazonaws.com |  |
| **metricsConfig** | ----- | ----- | ----- |
| includeMetrics |  |  |  |
| | name | The metric name | "CPUUtilization" |
| | alias | Allows you to give another name that you would like to see on the metric browser | "CPUUsage" |
| | statType | The statistic type | **Allowed values:**<br/>"ave"<br/>"max"<br/>"min"<br/>"sum"<br/>"samplecount" |
| | delta | Configurutaion to collect delta value of this metric. If true will report value difference of privous minute value and current value |  |
| | multiplier | Allows you to multiply the resultant metric with a number. This can be used if you receive a very small result that is even less than zero and in order to see it on the metric browser you multiply it with a number so that its greater than 0. |  |
| | aggregationType | The aggregator qualifier specifies how the Machine Agent aggregates the values reported during a one-minute period. | |
| | timeRollUpType | The time-rollup qualifier specifies how the Controller rolls up the values when it converts from one-minute granularity tables to 10-minute granularity and 60-minute granularity tables over time. |  |
| | clusterRollUpType |  The cluster-rollup qualifier specifies how the controller aggregates metric values in a tier. |  |
| | ----- | ----- | ----- |
| | excludeMetrics | Metrics to exclude - supports regex | "CPUUtilization",<br/>"Swap.*" |
| metricsTimeRange |  |  |  |
| | startTimeInMinsBeforeNow | The no of mins to deduct from current time for start time of query | 5 |
| | endTimeInMinsBeforeNow | The no of mins to deduct from current time for end time of query.<br>Note, this must be less than startTimeInMinsBeforeNow | 0 |
| | ----- | ----- | ----- |
| | getMetricStatisticsRateLimit |  Rate limit ( per second ) for GetMetricStatistics, default value is 400. https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_limits.html | 400 |
| | ----- | ----- | ----- |
| | maxErrorRetrySize | The max number of retry attempts for failed retryable requests | 1 |
| | ----- | ----- | ----- |
|**metricPrefix** |  | The path prefix for viewing metrics in the metric browser. | "Server\|Component:<COMPONENT_ID>\|Custom Metrics\|Amazon EC2\|" |


**Below is an example config for monitoring multiple accounts and regions:**

~~~
accounts:
  - awsAccessKey: "XXXXXXXX1"
    awsSecretKey: "XXXXXXXXXX1"
    displayAccountName: "TestAccount_1"
    regions: ["us-east-1","us-west-1","us-west-2"]

  - awsAccessKey: "XXXXXXXX2"
    awsSecretKey: "XXXXXXXXXX2"
    displayAccountName: "TestAccount_2"
    regions: ["eu-central-1","eu-west-1"]

credentialsDecryptionConfig:
    enableDecryption: "false"
    decryptionKey:
    
proxyConfig:
    host:
    port:
    username:
    password:
    
ec2InstanceNameConfig:
    useNameInMetrics: "true"
    tagKey: "Name"

#Filter the instances by tag name. tagValue is optional. if tagValue is not provided, we will fetch all the instances with the tagName.
tags:
  - name: "Name"
    #value: ["tag1", "tag2", "tag3"]

# Global metrics config for all accounts
metricsConfig:

    # By default, all metrics retrieved from cloudwatch are 'Average' values.	
    # This option allows you to override the metric type. 
    #
    # Allowed statTypes are: ave, max, min, sum, samplecount
    #
    # Note: Irrespective of the metric type, value will still be reported as
    # Observed value to the Controller
    includeMetrics:
       - name: "CPUUtilization"
         alias: "CPUUtilization"
         statType: "ave"
         aggregationType: "OBSERVATION"
         timeRollUpType: "CURRENT"
         clusterRollUpType: "COLLECTIVE"
         delta: false
         multiplier: 1
       - name: "NetworkOut"
       - name: "NetworkIn"

    metricsTimeRange:
      startTimeInMinsBeforeNow: 10
      endTimeInMinsBeforeNow: 0

    # Rate limit ( per second ) for GetMetricStatistics, default value is 400. https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_limits.html
    getMetricStatisticsRateLimit: 400

    # 
    # The max number of retry attempts for failed retryable requests
    # (ex: 5xx error responses from a service) or throttling errors
    #
    maxErrorRetrySize: 0


#Allowed values are Basic and Detailed. Refer https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-cloudwatch-new.html for more information
# Basic will fire CloudWatch API calls every 5 minutes
# Detailed will fire CloudWatch API calls every 1 minutes
cloudWatchMonitoring: "Basic"

#If you want any other interval ( other than the mentioned values in the above configuration ) configure it here, if not leave it empty. This value is in minutes
cloudWatchMonitoringInterval: 0

concurrencyConfig:
  noOfAccountThreads: 3
  noOfRegionThreadsPerAccount: 3
  noOfMetricThreadsPerRegion: 3
  #Thread timeout in seconds
  threadTimeOut: 30

regionEndPoints:
  ap-southeast-1: monitoring.ap-southeast-1.amazonaws.com
  ap-southeast-2: monitoring.ap-southeast-2.amazonaws.com
  ap-northeast-1: monitoring.ap-northeast-1.amazonaws.com
  eu-central-1: monitoring.eu-central-1.amazonaws.com
  eu-west-1: monitoring.eu-west-1.amazonaws.com
  us-east-1: monitoring.us-east-1.amazonaws.com
  us-west-1: monitoring.us-west-1.amazonaws.com
  us-west-2: monitoring.us-west-2.amazonaws.com
  sa-east-1: monitoring.sa-east-1.amazonaws.com

#prefix used to show up metrics in AppDynamics. This will create this metric in all the tiers, under this path
#metricPrefix: "Custom Metrics|Amazon EC2|"

#This will create it in specific Tier/Component. Make sure to replace <COMPONENT_ID> with the appropriate one from your environment.
#To find the <COMPONENT_ID> in your environment, please follow the screenshot https://docs.appdynamics.com/display/PRO42/Build+a+Monitoring+Extension+Using+Java
metricPrefix: "Server|Component:<COMPONENT_ID>|Custom Metrics|Amazon EC2|"
~~~

## Configuration Steps

1. Configure the "COMPONENT_ID" under which the metrics need to be reported. This can be done by changing the value of `<COMPONENT_ID>` in
     metricPrefix: "Server|Component:<COMPONENT_ID>|Custom Metrics|Amazon EC2|".

     For example,
     ```
     metricPrefix: "Server|Component:100|Custom Metrics|Amazon EC2|"
     ```
2. Configure "awsAccessKey" and "awsSecretKey". If you are running this extension inside an EC2 instance which has IAM profile configured then you don't have to configure these values, extension will use IAM profile to authenticate.
3. Configure "regions". Extension collects metrics from all the regions configured here.
4. If you want to encrypt the "awsAccessKey" and "awsSecretKey" then follow the "Credentials Encryption" section and provide the encrypted values in "awsAccessKey" and "awsSecretKey". Configure "enableDecryption" of "credentialsDecryptionConfig" to true and provide the encryption key in "encryptionKey"

## Metrics

Typical metric path: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Amazon EC2|\<Account Name\>|\<Region\>|Instance|\<instance id or name\>** followed by the metrics defined in the link below:

- [EC2 Metrics](http://docs.aws.amazon.com/AmazonCloudWatch/latest/DeveloperGuide/ec2-metricscollected.html)

## Credentials Encryption

Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

Once you encrypt the access key and secret key add the excrypted values to the awsAccessKey and awsSecretKey respectively. Based on the "enableDecryption" in "credentialsDecryptionConfig" section extension will decide if it has to decrypt the keys or not. Provide the encryption key used to encrypt in the "encryptionKey" of "credentialsDecryptionConfig".


## Extensions Workbench

Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting

Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) to contact the support team.

## Support Tickets
If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.

    1. Stop the running machine agent.
    2. Delete all existing logs under <MachineAgent>/logs.
    3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug.
        <logger name="com.singularity">
        <logger name="com.appdynamics">
    4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
    5. Attach the zipped <MachineAgent>/conf/* directory here.
    6. Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory here.

For any support related questions, you can also contact help@appdynamics.com.



## Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://github.com/Appdynamics/aws-ec2-monitoring-extension).

## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |2.0.0       |
|Controller Compatibility  |3.7 or Later|
|Last Update               |17 Apr 2018 |
