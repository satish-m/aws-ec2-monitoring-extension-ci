package EXT_AwsEc2monitoringExtensionCi.buildTypes

import EXT_AwsEc2monitoringExtensionCi.vcsRoots.EXT_AwsEc2monitoringExtensionCi_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

/**
 * @author Satish Muddam
 */
object EXT_AwsEc2monitoringExtensionCi_CleanBuild : BuildType({
    uuid = "6F983815-981B-4EDA-89AF-B60A6AADA9E4"
    id ("EXT_AwsEc2monitoringExtensionCi_CleanBuild")
    name = "CleanBuild"

    vcs {
        root(EXT_AwsEc2monitoringExtensionCi_VCS)
    }
    steps {
        maven {
            goals = "clean install -Pno-integration-tests"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
    }

    triggers {
        vcs {
        }
    }
})
