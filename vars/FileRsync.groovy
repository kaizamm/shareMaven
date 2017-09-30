#!/usr/bin/groovy
/*
projectName名称需要指定，具体到子项目名称
packageName名称需要指定，具体到.jar还是.war
*/
def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()
  // 项目位置
  def saltmasterIP = "${config.saltmasterIP}"
  def saltMasterTmp = "${config.saltMasterTmp}"
  def saltMasterProjectPath = "${config.saltMasterProjectPath}"
  def APP_HOSTNAME = "${config.APP_HOSTNAME}"
  def saltMasterUPath = "${config.saltMasterUPath}"
  // 源包位置
  def DIR_SRC_UPDATE = "${config.DIR_SRC_UPDATE}"
  // 编译完成包名称
  def dstPackageName = "${config.dstPackageName}"

  // delete old buildspace
  sh (script: "scp ${env.WORKSPACE}/${dstPackageName}  ${saltmasterIP}:${saltMasterTmp}/",returnStdout: true)
  sh (script: "ssh ${saltmasterIP} 'sudo mv ${saltMasterTmp}/${dstPackageName} ${saltMasterProjectPath}/${dstPackageName} ' ",returnStdout: true)
  // unzip war file
  sh (script: "ssh ${saltmasterIP} 'sudo -L '${APP_HOSTNAME}' cp.get_file ${saltMasterUPath}/${dstPackageName} ${DIR_SRC_UPDATE}/${dstPackageName}' ",returnStdout: true)

  
}