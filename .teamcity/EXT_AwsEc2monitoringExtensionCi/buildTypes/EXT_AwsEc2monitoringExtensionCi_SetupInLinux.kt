package EXT_AwsEc2monitoringExtensionCi.buildTypes

import EXT_AwsEc2monitoringExtensionCi.vcsRoots.EXT_AwsEc2monitoringExtensionCi_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

/**
 * @author Satish Muddam
 */
object EXT_AwsEc2monitoringExtensionCi_SetupInLinux : BuildType({
    uuid = "06156D83-1B8A-4CB4-BD65-6FBDF192C0C6"
    id ("EXT_AwsEc2monitoringExtensionCi_SetupInLinux")
    name = "Setup Linux Environment"

    vcs {
        root(EXT_AwsEc2monitoringExtensionCi_VCS)

    }

    steps {
        exec {
            path = "make"
            arguments = "dockerRun"
        }

        //Waits for 5 minutes to send metrics to the controller
        exec {
            path = "make"
            arguments = "sleep"
        }
    }

    dependencies {
        dependency(EXT_AwsEc2monitoringExtensionCi_CleanBuild) {
            snapshot {

            }
        }
    }

    triggers {
        vcs {
        }
    }
})