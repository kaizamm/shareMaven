#!/usr/bin/env groovy
/*
唯一需要指定的就是propertiesPath，其他的来源于该文件
*/

def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // 读取properties文件
  def envList = myLoadProperties "${config.propertiesPath}"
  withEnv(envList) {
    def appOrg="${env.appOrg}"
    def appEnv="${env.appEnv}"
    def appTargetName="${env.appTargetName}"
    def etcdClusterIp="${env.etcdClusterIp}"
    def fromImage="${env.fromImage}"
    def toImage="${env.toImage}"+":"+"${env.svnRevision}"
    def appCfgs="${env.appCfgs}"
    def projectRecipintList="${env.projectRecipintList}"
    def dockerRunOpts="${env.dockerRunOpts}"
    def dockerHosts="${env.dockerHosts}"

    def javaOpts="${env.javaOpts}"

    def hostsArry = dockerHosts.split(' ')
    for (int i = 0;i<hostsArry.size();i++) {
      def appAddress = hostsArry[i].split(',')[0].trim()
      def appIp = appAddress.split('_')[0].trim()
      def appPort = appAddress.split('_')[1].trim()
      def appExpose = hostsArry[i].split(',')[1].trim()
      def instanceId = (appOrg+"_"+appEnv+"_"+appTargetName).toUpperCase().trim()
      def containerName = (instanceId+"_"+appIp+"_"+appPort).toUpperCase().replace(".","").trim()
      def int jmxPort = (appPort.toInteger()+10)

      // 保留当前容器的镜像sha值
      try {
        RESULT = sh (script: "docker -H"+" "+appIp+":2375 inspect -f '{{.Image}}'"+" "+containerName,returnStdout: true).trim()
        println RESULT
      } catch (err) {
        println "Failled: ${err}"
      }

      // 停止并删除当前容器
      try {
        sh (script: "docker -H"+" "+appIp+":2375 stop"+" "+containerName,returnStdout: true)
        sh (script: "docker -H"+" "+appIp+":2375 rm"+" "+containerName,returnStdout: true)
      } catch (err) {
        println "Failled: ${err}"
      }

      sleep(3)

      // 拉取push到registry的image
      sh (script: "docker -H"+" "+appIp+":2375 pull"+" "+toImage,returnStdout: true)
      // 运行容器
      sh (script: "docker -H"+" "+appIp+":2375 run -d --restart=always --name="+containerName+" "+"-e etcdClusterIp="+etcdClusterIp+" "+"-e appCfgs="+appCfgs.replace("null","").trim()+" "+"-e appTargetName="+appTargetName+" "+"-e instanceId="+instanceId+" "+"-e jmxIp="+appIp+" "+"-e jmxPort="+jmxPort+" "+"-e JAVA_OPTS="+javaOpts.trim()+" " +"-v /data/logs/"+containerName+":/AppLogs -p"+" "+jmxPort+":"+jmxPort+" "+"-p"+" "+appExpose+" "+dockerRunOpts.replace("null","").trim()+" "+toImage.trim(),returnStdout: true)

      sleep(10)
      // 获取当前运行容器的状态码
      def containerStatus = sh (script: "docker -H"+" "+appIp+":2375 inspect -f '{{.State.Status}}'"+" "+containerName,returnStdout: true).trim()
      println containerStatus
      // 检测状态，如果不是running状态，停止并删除，返回错误
      if (containerStatus != 'running') {
        sh (script: "docker -H"+" "+appIp+":2375 stop"+" "+containerName,returnStdout: true)
        sh (script: "docker -H"+" "+appIp+":2375 rm"+" "+containerName,returnStdout: true)
        error "containerStatus is ${containerStatus}"
      } else {
        println "Deploy Success!"
      }

      // 删除前面保存的容器的镜像
      try {
        sh (script: "docker -H"+" "+appIp+":2375 rmi"+" "+RESULT,returnStdout: true)
      } catch (err) {
        println "Failled: ${err}"
      }
    }
  }
}
