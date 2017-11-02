#!/usr/bin/env groovy
def call(args) {
  args = null==args ? "-b ${env.WORKSPACE}/build.gradle clean build jenkinsMakePkg -x test" : args
  sh "chmod +x ${env.WORKSPACE}/gradlew"
  sh "${env.WORKSPACE}/gradlew ${args}"
}