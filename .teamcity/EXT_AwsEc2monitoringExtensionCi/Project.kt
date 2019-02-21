package EXT_AwsEc2monitoringExtensionCi

import EXT_AwsEc2monitoringExtensionCi.buildTypes.*
import EXT_AwsEc2monitoringExtensionCi.vcsRoots.EXT_AwsEc2monitoringExtensionCi_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

object Project : Project({
    uuid = "985d90ec-005b-4105-9a8a-7cc9cd4beb43"
    id("EXT_AwsEc2monitoringExtensionCi")
    parentId("EXT")
    name = "aws-ec2-monitoring-extension-ci"

    vcsRoot(EXT_AwsEc2monitoringExtensionCi_VCS)

    buildType(EXT_AwsEc2monitoringExtensionCi_CleanBuild)
    buildType(EXT_AwsEc2monitoringExtensionCi_SetupInLinux)
    buildType(EXT_AwsEc2monitoringExtensionCi_IntegrationTestInLinux)
    buildType(EXT_AwsEc2monitoringExtensionCi_StopLinux)
    buildType(EXT_AwsEc2monitoringExtensionCi_Publish)

    features {
        versionedSettings {
            id = "PROJECT_EXT_5"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = "${EXT_AwsEc2monitoringExtensionCi_VCS.id}"
            showChanges = false
            settingsFormat = VersionedSettings.Format.KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }

    buildTypesOrder = arrayListOf(
            EXT_AwsEc2monitoringExtensionCi_CleanBuild,
            EXT_AwsEc2monitoringExtensionCi_SetupInLinux,
            EXT_AwsEc2monitoringExtensionCi_IntegrationTestInLinux,
            EXT_AwsEc2monitoringExtensionCi_StopLinux,
            EXT_AwsEc2monitoringExtensionCi_Publish
    )
})
