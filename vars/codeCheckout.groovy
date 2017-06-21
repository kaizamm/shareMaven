#!/usr/bin/env groovy
/*
svnRepo 是必须指定的属性值。
svnCredentialsId 证书id，为可选参数，不填为默认值
svnLocal 代码checkout地址，为可选参数，不填为默认值
*/

def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def svnCredentialsId=config.svnCredentialsId
  def svnLocal=config.svnLocal
  def svnRepo=config.svnRepo
  checkout([$class: 'SubversionSCM',
  additionalCredentials: [],
  excludedCommitMessages: '',
  excludedRegions: '',
  excludedRevprop: '',
  excludedUsers: '',
  filterChangelog: false,
  ignoreDirPropChanges: false,
  includedRegions: '',
  locations: [[credentialsId: "${svnCredentialsId}", depthOption: 'infinity', ignoreExternalsOption: true, local: "${svnLocal}", remote: "${svnRepo}"]],
  workspaceUpdater: [$class: 'UpdateUpdater']])
}
