package EXT_AwsEc2monitoringExtensionCi.buildTypes

import EXT_AwsEc2monitoringExtensionCi.vcsRoots.EXT_AwsEc2monitoringExtensionCi_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

/**
 * @author Satish Muddam
 */
object EXT_AwsEc2monitoringExtensionCi_StopLinux : BuildType({
    uuid = "C2EBAE86-0772-41EE-8999-C539F48BDCB0"
    id  ("EXT_AwsEc2monitoringExtensionCi_StopLinux")
    name = "Stop Linux docker"

    vcs {
        root(EXT_AwsEc2monitoringExtensionCi_VCS)

    }

    steps {
        exec {
            path = "make"
            arguments = "dockerStop"
        }
    }

    dependencies {
        dependency(EXT_AwsEc2monitoringExtensionCi_IntegrationTestInLinux) {
            snapshot {

            }
        }
    }

    triggers {
        vcs {
        }
    }
})