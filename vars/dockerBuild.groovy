#!/usr/bin/groovy
/*
propertiesPath 指定properties文件路径
*/

def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // 读取properties文件
  def envList = myLoadProperties "${config.propertiesPath}"

  withEnv(envList) {
    //本地编译后的软件包
    def localFile="${env.WORKSPACE}/${env.appTargetName}/target/${env.appTargetName}.war"
    //需要将编译后的软件包拷贝到的路径
    def buildPath="${env.WORKSPACE}/buildspace"
    //Dockerfile内容
    def dockerFileContext="""FROM ${env.fromImage}
MAINTAINER devops "devops@quarkfinance.com"
ADD ${env.appTargetName}.war \${CATALINA_HOME}/webapps
RUN cd \${CATALINA_HOME}/webapps && unzip ${env.appTargetName}.war -d ${env.appTargetName} && rm -rf ${env.appTargetName}.war
    """

    // 生成env上下文的svnRevision
    env.svnRevision = sh (script: "svn info ${env.WORKSPACE}/${env.appTargetName} |grep 'Last Changed Rev' | awk '{print \$4}'",returnStdout: true).trim()
    // 生成Dockerfile
    sh (script: "rm -rf ${buildPath}",returnStdout: true)
    sh (script: "mkdir -p ${buildPath}",returnStdout: true)
    sh (script: "cp -af ${localFile} ${buildPath}",returnStdout: true)
    writeFile encoding: 'UTF-8', file: "${buildPath}/Dockerfile",text: dockerFileContext

    // 执行docker build
    sh (script: "docker pull ${env.fromImage}",returnStdout: true)
    sh (script: "docker build --no-cache=true -t ${env.toImage}:${svnRevision} ${buildPath}",returnStdout: true)
    sh (script: "docker push ${env.toImage}:${svnRevision}",returnStdout: true)
    sh (script: "docker rmi ${env.toImage}:${svnRevision}",returnStdout: true)
  }

}
