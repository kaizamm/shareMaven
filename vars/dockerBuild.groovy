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
  def projectPath = "${env.WORKSPACE}/${config.projectName}"
  // 编译包位置
  def packagePath = "${projectPath}/target"
  // 编译包名称
  def packageName = config.packageName
  //解压目录名
  def packageUnzipName = packageName.substring(0,packageName.lastIndexOf("."))
  //需要将编译后的软件包拷贝到的路径
  def buildPath="${env.WORKSPACE}/buildspace"
  //Dockerfile内容
  def dockerFileContext="""FROM ${env.fromImage}
MAINTAINER devops "devops@quarkfinance.com"
ADD ${packageName} \${CATALINA_HOME}/webapps
RUN cd \${CATALINA_HOME}/webapps && unzip ${packageName} -d ${packageUnzipName}
    """

  // 生成env上下文的svnRevision
  env.svnRevision = sh (script: "svn info ${projectPath} |grep 'Last Changed Rev' | awk '{print \$4}'",returnStdout: true).trim()
  // 生成Dockerfile
  sh (script: "rm -rf ${buildPath}",returnStdout: true)
  sh (script: "mkdir -p ${buildPath}",returnStdout: true)
  sh (script: "cp -af ${packagePath}/${packageName} ${buildPath}",returnStdout: true)
  writeFile encoding: 'UTF-8', file: "${buildPath}/Dockerfile",text: dockerFileContext

  // 执行docker build
  sh (script: "docker pull ${env.fromImage}",returnStdout: true)
  sh (script: "docker build --no-cache=true -t ${env.toImage}:${svnRevision} ${buildPath}",returnStdout: true)
  sh (script: "docker push ${env.toImage}:${svnRevision}",returnStdout: true)
  sh (script: "docker rmi ${env.toImage}:${svnRevision}",returnStdout: true)
}
