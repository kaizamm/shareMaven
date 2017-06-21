#!/usr/bin/env groovy

def call(args) {
  args = null==args ? "clean install -Dmaven.test.skip=true" : args
  sh "${tool 'M3'}/bin/mvn ${args}"
  //对其打包好的war包进行归档
  stash includes: '**/target/*.war',name: 'app'
}
