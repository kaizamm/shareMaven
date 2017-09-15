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
  projectPath = "${env.WORKSPACE}/${config.projectName}"
  // 编译包位置
  def packagePath = "${projectPath}/target"
  // 编译包名称
  def packageName = config.packageName
  //需要将编译后的软件包拷贝到的路径
  def buildPath="${env.WORKSPACE}/buildspace"


  // 收集所有打好的包到buildspace
  if (fileExists("${buildPath}")) {
    sh (script: "cp -af ${packagePath}/${packageName} ${buildPath}",returnStdout: true)
  } else {
    sh (script: "mkdir -p ${buildPath}",returnStdout: true)
    sh (script: "cp -af ${packagePath}/${packageName} ${buildPath}",returnStdout: true)
  }

}
