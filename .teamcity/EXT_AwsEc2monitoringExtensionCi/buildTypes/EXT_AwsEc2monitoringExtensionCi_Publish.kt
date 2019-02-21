package EXT_AwsEc2monitoringExtensionCi.buildTypes

import EXT_AwsEc2monitoringExtensionCi.vcsRoots.EXT_AwsEc2monitoringExtensionCi_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

/**
 * @author Satish Muddam
 */

object EXT_AwsEc2monitoringExtensionCi_Publish : BuildType({
    uuid = "5E0AD16F-805D-4DCF-8195-CCDEE0C62C26"
    id ("EXT_AwsEc2monitoringExtensionCi_Publish")
    name = "Publish build artifact"

    vcs {
        root(EXT_AwsEc2monitoringExtensionCi_VCS)
    }

    steps {
        maven {
            goals = "github-release:release"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
    }

    dependencies {
        dependency(EXT_AwsEc2monitoringExtensionCi_StopLinux) {
            snapshot {

            }
        }
    }

    triggers {
        vcs {
        }
    }


    artifactRules = """
       target/ApacheMonitor-*.zip
    """.trimIndent()

})