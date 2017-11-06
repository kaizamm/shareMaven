#!/usr/bin/env groovy
def call(args) {
  args = null==args ? "-b ${env.WORKSPACE}/src/build.gradle clean build jenkinsMakePkg -x test" : args
  sh "chmod +x ${env.WORKSPACE}/src/gradlew"
  sh "${env.WORKSPACE}/src/gradlew ${args}"
}