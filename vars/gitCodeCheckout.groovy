#!/usr/bin/env groovy
/*
gitRepo 指定git repo地址，必须指定
gitCredentialsId 指定证书ID，不指定，默认是jenkinsadmin用户
gitLocal 指定checkout的位置，不指定，默认是当前workspace目录
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
    userRemoteConfigs: [[url: "${gitRepo}",credentialsId: "${config.gitCredentialsId}"]]])
  }
}
