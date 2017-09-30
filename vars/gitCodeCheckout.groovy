#!/usr/bin/env groovy
/*
gitRepo 指定git repo地址，必须指定
gitCredentialsId 指定证书ID，不指定，默认是jenkinsadmin用户
gitLocal 指定checkout的位置，不指定，默认是当前workspace目录
gitTag 指定git Tag 版本，不指定，默认从master拉取
*/

def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  config.gitCredentialsId = null == config.gitCredentialsId ? "c9baf728-2463-4d59-8643-2181a681fdd4" : config.gitCredentialsId
  config.gitLocal = null == config.gitLocal ? "." : config.gitLocal
  config.gitTag = null == config.gitTag ? "*/master" : config.gitTag
  def gitRepo = config.gitRepo
  def gitTag = config.gitTag

  dir("${config.gitLocal}") {
    checkout([$class: 'GitSCM',
    branches: [[name: "refs/tags/${gitTag}"]],
    userRemoteConfigs: [[url: "${gitRepo}",credentialsId: "${config.gitCredentialsId}"]]])
  }
}
