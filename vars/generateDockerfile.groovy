#!/usr/bin/groovy
/*
propertiesPath 指定properties文件路径
*/
// 调用第三方库，读取properties文件
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
    //先对其解档
    unstash 'app'
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

    def svnRevision = sh (script: "svn info ${env.WORKSPACE}/${env.appTargetName} |grep 'Last Changed Rev' | awk '{print \$4}'",returnStdout: true).trim()
    // 生成Dockerfile
    sh (script: "rm -rf ${buildPath}",returnStdout: true)
    sh (script: "mkdir -p ${buildPath}",returnStdout: true)
    sh (script: "cp -af ${localFile} ${buildPath}",returnStdout: true)
    writeFile encoding: 'UTF-8', file: "${buildPath}/Dockerfile",text: dockerFileContext

    // 执行docker build
    sh (script: "docker pull ${env.fromImage}",returnStdout: true)
    sh (script: "docker build --no-cache=true -t ${env.toImage}:${svnRevision} ${buildPath}",returnStdout: true)
    // sh (script: "docker push ${env.toImage}",returnStdout: true)
    // sh (script: "docker rmi ${env.toImage}",returnStdout: true)
  }

}
