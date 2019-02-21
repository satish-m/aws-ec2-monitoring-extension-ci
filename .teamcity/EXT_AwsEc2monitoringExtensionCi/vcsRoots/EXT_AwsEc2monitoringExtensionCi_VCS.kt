package EXT_AwsEc2monitoringExtensionCi.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object EXT_AwsEc2monitoringExtensionCi_VCS : GitVcsRoot({
    uuid = "9e485810-acec-4364-8666-c208c767db0e"
    name = "aws-ec2-monitoring-extension-ci"
    url = "git@github.com:satish-m/aws-ec2-monitoring-extension-ci.git"
    authMethod = uploadedKey {
        uploadedKey = "ssh-github"
        passphrase = "credentialsJSON:34a47f56-af6b-4133-85c0-70ad8057f7f2"
    }
})
