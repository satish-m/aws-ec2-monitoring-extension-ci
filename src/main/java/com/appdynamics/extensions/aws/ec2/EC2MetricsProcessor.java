/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.ec2;

import static com.appdynamics.extensions.aws.Constants.METRIC_PATH_SEPARATOR;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.ec2.providers.EC2InstanceNameProvider;
import com.appdynamics.extensions.aws.metric.AccountMetricStatistics;
import com.appdynamics.extensions.aws.metric.MetricStatistic;
import com.appdynamics.extensions.aws.metric.NamespaceMetricStatistics;
import com.appdynamics.extensions.aws.metric.RegionMetricStatistics;
import com.appdynamics.extensions.aws.metric.StatisticType;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Florencio Sarmiento
 */
public class EC2MetricsProcessor implements MetricsProcessor {

    private static final Logger LOGGER = Logger.getLogger(EC2MetricsProcessor.class);


    private static final String NAMESPACE = "AWS/EC2";

    private static final String DIMENSIONS = "InstanceId";

    private static final String INSTANCE = "Instance";

    private List<IncludeMetric> includeMetrics;

    private String ec2Instance;

    public EC2MetricsProcessor(List<IncludeMetric> includeMetrics, String ec2Instance) {
        this.includeMetrics = includeMetrics;
        this.ec2Instance = ec2Instance;
    }

    public List<AWSMetric> getMetrics(AmazonCloudWatch awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {

        List<DimensionFilter> dimensions = new ArrayList<DimensionFilter>();

        DimensionFilter dimensionFilter = new DimensionFilter();
        dimensionFilter.withName(DIMENSIONS);

        if (!Strings.isNullOrEmpty(ec2Instance)) {
            dimensionFilter.withValue(ec2Instance);
        }

        dimensions.add(dimensionFilter);

        EC2MetricPredicate metricFilter = new EC2MetricPredicate(accountName);

        return MetricsProcessorHelper.getFilteredMetrics(awsCloudWatch, awsRequestsCounter,
                NAMESPACE,
                includeMetrics,
                dimensions, metricFilter);
    }

    public StatisticType getStatisticType(AWSMetric metric) {
        return MetricsProcessorHelper.getStatisticType(metric.getIncludeMetric(), includeMetrics);
    }

    public List<com.appdynamics.extensions.metrics.Metric> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
        EC2InstanceNameProvider ec2InstanceNameProvider = EC2InstanceNameProvider.getInstance();
        List<com.appdynamics.extensions.metrics.Metric> stats = new ArrayList<>();


        if (namespaceMetricStats != null) {
            for (AccountMetricStatistics accountStats : namespaceMetricStats.getAccountMetricStatisticsList()) {
                for (RegionMetricStatistics regionStats : accountStats.getRegionMetricStatisticsList()) {
                    for (MetricStatistic metricStat : regionStats.getMetricStatisticsList()) {

                        String metricPath = createMetricPath(accountStats.getAccountName(),
                                regionStats.getRegion(), metricStat, ec2InstanceNameProvider);

                        if (metricStat.getValue() != null) {

                            Map<String, Object> metricProperties = new HashMap<>();
                            AWSMetric awsMetric = metricStat.getMetric();
                            IncludeMetric includeMetric = awsMetric.getIncludeMetric();

                            metricProperties.put("alias", includeMetric.getAlias());
                            metricProperties.put("multiplier", includeMetric.getMultiplier());
                            metricProperties.put("aggregationType", includeMetric.getAggregationType());
                            metricProperties.put("timeRollUpType", includeMetric.getTimeRollUpType());
                            metricProperties.put("clusterRollUpType", includeMetric.getClusterRollUpType());
                            metricProperties.put("delta", includeMetric.isDelta());
                            
                            com.appdynamics.extensions.metrics.Metric metric = new com.appdynamics.extensions.metrics.Metric(includeMetric.getName(), Double.toString(metricStat.getValue()),
                                    metricStat.getMetricPrefix() + metricPath, metricProperties);
                            stats.add(metric);
                        } else {
                            LOGGER.debug(String.format("Ignoring metric [ %s ] which has value null", metricPath));
                        }
                    }
                }
            }
        }

        return stats;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    private String createMetricPath(String accountName, String region,
                                    MetricStatistic metricStatistic, EC2InstanceNameProvider ec2InstanceNameProvider) {

        AWSMetric awsMetric = metricStatistic.getMetric();
        IncludeMetric includeMetric = awsMetric.getIncludeMetric();
        Metric metric = awsMetric.getMetric();
        String instanceId = metric.getDimensions().get(0).getValue();
        String instanceName = ec2InstanceNameProvider.getInstanceName(accountName, region, instanceId);

        StringBuilder metricPath = new StringBuilder(accountName)
                .append(METRIC_PATH_SEPARATOR)
                .append(region)
                .append(METRIC_PATH_SEPARATOR)
                .append(INSTANCE)
                .append(METRIC_PATH_SEPARATOR)
                .append(instanceName)
                .append(METRIC_PATH_SEPARATOR)
                .append(includeMetric.getName());

        return metricPath.toString();
    }

}
