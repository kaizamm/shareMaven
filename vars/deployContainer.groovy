#!/usr/bin/env groovy
@NonCPS
def mapToList(depmap) {
    def dlist = []
    for (entry in depmap) {
        dlist.add([entry.key, entry.value])
    }
    dlist
}
def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // 读取properties文件
  def props = readProperties file: "${config.propertiesPath}"
  def envList = []
  for (it2 in mapToList(props)) {
      def key=it2[0]
      def val=it2[1]
      envList << key+"="+val
  }

  withEnv(envList) {
    def appOrg="${env.appOrg}"
    def appEnv="${env.appEnv}"
    def appTargetName="${env.appTargetName}"
    def etcdClusterIp="${env.etcdClusterIp}"
    def fromImage="${env.fromImage}"
    def toImage="${env.toImage}"+":"+"${env.svnRevision}"
    def appCfgs="${env.appCfgs}"
    def projectRecipintList="${env.projectRecipintList}"
    def dockerRunOpt="${env.dockerRunOpt}"
    def dockerHosts="${env.dockerHosts}"
println dockerHosts
    def hostsArry = dockerHosts.split(' ')
    for (int i = 0;i<hostsArry.size();i++) {
      def appAddress = hostsArry[i].split(',')[0].trim()
      def appIp = appAddress.split('_')[0].trim()
      def appPort = appAddress.split('_')[1].trim()
      def appExpose = hostsArry[i].split(',')[1].trim()
      def instanceId = (appOrg+"_"+appEnv+"_"+appTargetName).toUpperCase().trim()
      def containerName = (instanceId+"_"+appIp+"_"+appPort).toUpperCase().replace(".","").trim()
      def int jmxPort = (appPort.toInteger()+10)

      try {
        RESULT = sh (script: "docker -H"+" "+appIp+":2375 inspect -f '{{.Image}}'"+" "+containerName,returnStdout: true).trim()
        println RESULT
      } catch (err) {
        println "Failled: ${err}"
      }

      try {
        sh (script: "docker -H"+" "+appIp+":2375 stop"+" "+containerName,returnStdout: true)
        sh (script: "docker -H"+" "+appIp+":2375 rm"+" "+containerName,returnStdout: true)
      } catch (err) {
        println "Failled: ${err}"
      }

      sleep(3)
    }
  }
}
