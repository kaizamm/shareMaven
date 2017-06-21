#!/usr/bin/env groovy
@Library("shareMaven") _
def call(string repo='') {
  checkout([$class: 'SubversionSCM',
  additionalCredentials: [],
  excludedCommitMessages: '',
  excludedRegions: '',
  excludedRevprop: '',
  excludedUsers: '',
  filterChangelog: false,
  ignoreDirPropChanges: false,
  includedRegions: '',
  locations: [[credentialsId: 'c9baf728-2463-4d59-8643-2181a681fdd4', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "${repo}"]],
  workspaceUpdater: [$class: 'UpdateUpdater']])
}
