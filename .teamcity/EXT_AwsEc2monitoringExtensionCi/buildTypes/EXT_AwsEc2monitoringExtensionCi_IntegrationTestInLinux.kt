package EXT_AwsEc2monitoringExtensionCi.buildTypes

import EXT_AwsEc2monitoringExtensionCi.vcsRoots.EXT_AwsEc2monitoringExtensionCi_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

/**
 * @author Satish Muddam
 */
object EXT_AwsEc2monitoringExtensionCi_IntegrationTestInLinux : BuildType({
    uuid = "4EDD1511-B00C-4953-A7C8-18A3EE6393A3"
    id ("EXT_AwsEc2monitoringExtensionCi_IntegrationTestInLinux")
    name = "IntegrationTest in Linux"

    vcs {
        root(EXT_AwsEc2monitoringExtensionCi_VCS)

    }

    steps {
        maven {
            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
    }

    dependencies {
        dependency(EXT_AwsEc2monitoringExtensionCi_SetupInLinux) {
            snapshot {

            }
        }
    }

    triggers {
        vcs {
        }
    }
})