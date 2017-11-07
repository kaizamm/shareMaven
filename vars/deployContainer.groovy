#!/usr/bin/env groovy
def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()
  def appOrg="${env.appOrg}"
  def appEnv="${env.appEnv}"
  def appTargetName="${env.appTargetName}"
  // def appTargetName1="${env.appTargetName1}"
  def etcdClusterIp="${env.etcdClusterIp}"
  def fromImage="${env.fromImage}"
  def toImage="${env.toImage}"+":"+"${env.imageTag}"
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
    def appExpose = 0
    def hostsArryLenth = hostsArry[i].split('_')
    def dubboPort = 0
    if (hostsArryLenth.size() == 3) {
        appExpose = hostsArry[i].split(',')[1].trim().split('_')[0].trim()
        dubboPort = hostsArry[i].split('_')[2].trim()
    }
    else {
        appExpose = hostsArry[i].split(',')[1].trim()
    }
    // def instanceId = (appOrg+"_"+appEnv+"_"+appTargetName.substring(0,appTargetName.lastIndexOf("."))+"_"+appTargetName1.substring(0,appTargetName1.lastIndexOf("."))).toUpperCase().trim()
    // def instanceId = (appOrg+"_"+appEnv+"_"+appTargetName.substring(0,appTargetName.lastIndexOf("."))).toUpperCase().trim()
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
    sleep(10)
    // 拉取push到registry的image
    sh (script: "docker -H"+" "+appIp+":2375 pull"+" "+toImage,returnStdout: true)
    // 运行容器
    if (hostsArryLenth.size() == 3) {
      sh (script: "docker -H"+" "+appIp+":2375 run -d --restart=always --name="+containerName+" "+"-e etcdClusterIp="+etcdClusterIp+" "+"-e appCfgs="+appCfgs.replace("null","").trim()+" "+"-e appTargetName="+appTargetName+" "+"-e instanceId="+instanceId+" "+"-e jmxIp="+appIp+" "+"-e jmxPort="+jmxPort+" "+"-e JAVA_OPTS="+javaOpts.trim()+" " +"-v /opt/logs/"+containerName+":/AppLogs"+" "+"-v /opt/app_persistent/"+containerName+":/opt/app_persistent"+" "+"-p"+" "+jmxPort+":"+jmxPort+" "+"-p"+" "+appExpose+" -p"+" "+dubboPort+" "+dockerRunOpts.replace("null","").trim()+" "+toImage.trim(),returnStdout: true)
    } else {
      sh (script: "docker -H"+" "+appIp+":2375 run -d --restart=always --name="+containerName+" "+"-e etcdClusterIp="+etcdClusterIp+" "+"-e appCfgs="+appCfgs.replace("null","").trim()+" "+"-e appTargetName="+appTargetName+" "+"-e instanceId="+instanceId+" "+"-e jmxIp="+appIp+" "+"-e jmxPort="+jmxPort+" "+"-e JAVA_OPTS="+javaOpts.trim()+" " +"-v /opt/logs/"+containerName+":/AppLogs -p"+" "+jmxPort+":"+jmxPort+" "+"-p"+" "+appExpose+" "+dockerRunOpts.replace("null","").trim()+" "+toImage.trim(),returnStdout: true)
    }
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
