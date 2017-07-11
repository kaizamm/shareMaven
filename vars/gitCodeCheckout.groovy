#!/usr/bin/env groovy
/*
gitRepo
gitCredentialsId
gitLocal
*/

def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  config.gitCredentialsId = null == config.gitCredentialsId ? "c9baf728-2463-4d59-8643-2181a681fdd4" : config.gitCredentialsId
  config.gitLocal = null == config.gitLocal ? "." : config.gitLocal
  def gitRepo = config.gitRepo

  dir("${config.gitLocal}") {
    checkout([$class: 'GitSCM',
    branches: [[name: '*/master']],
    userRemoteConfigs: [[url: "${gitRepo}",credentialsId: "${config.credentialsId}"]]])
  }
}
