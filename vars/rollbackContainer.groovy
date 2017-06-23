#!/usr/bin/env groovy
/*
需要指定propertiesPath和getRegistryTagList的路径
*/
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
    stage('选择回滚版本') {
    // 获取当前项目在docker-registry上的所有版本
    def allImage =sh (script: "python ${config.getRegistryTagList} ${env.appTargetName}",returnStdout: true)
    // 选择当前项目要回滚的版本
    def userInput = input(
            id: 'userInput', message: 'Choice your rollback version!', parameters: [
                [$class: 'ChoiceParameterDefinition', choices: "${allImage}", description: 'rollbackAppTargetName from registry', name: 'rollbackAppTargetName']
                ])

    // rollbackAppTargetName 来自于与registry
    def rollbackAppTargetName = userInput.trim()
  }
    def appOrg="${env.appOrg}"
    def appEnv="${env.appEnv}"
    def appTargetName="${env.appTargetName}"
    def etcdClusterIp="${env.etcdClusterIp}"
    def fromImage="${env.fromImage}"
    // 和部署唯一不一样的就是toImage来源于registry
    def toImage="${env.DOCKER_REGISTRY}"+"/"+"${rollbackAppTargetName}"
    def appCfgs="${env.appCfgs}"
    def projectRecipintList="${env.projectRecipintList}"
    def dockerRunOpt="${env.dockerRunOpt}"
    def dockerHosts="${env.dockerHosts}"

    stage('确认回滚生产') {
    input "Your choice is ${rollbackAppTargetName} \nAre you sure deploy to Production?"
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
      sh (script: "docker -H"+" "+appIp+":2375 run -d --restart=always --name="+containerName+" "+"-e etcdClusterIp="+etcdClusterIp+" "+"-e appCfgs="+appCfgs.replace("null","").trim()+" "+"-e appTargetName="+appTargetName+" "+"-e instanceId="+instanceId+" "+"-e jmxIp="+appIp+" "+"-e jmxPort="+jmxPort+" "+"-e JAVA_OPTS='-server -Xms12g -Xmx12g -Xss512k -XX:PermSize=512m -XX:MaxPermSize=768m -XX:+AggressiveOpts -XX:+UseBiasedLocking -XX:MaxTenuringThreshold=7 -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -Djava.awt.headless=true -XX:+UseConcMarkSweepGC -XX:+UseParNewGC' -v /data/logs/"+containerName+":/AppLogs -p"+" "+jmxPort+":"+jmxPort+" "+"-p"+" "+appExpose+" "+dockerRunOpt.replace("null","").trim()+" "+toImage.trim(),returnStdout: true)

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
        println "RollBack Success!"
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
}
