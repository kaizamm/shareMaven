#!/usr/bin/env groovy

def call(args,dir) {
  args = null==args ? "clean install -Dmaven.test.skip=true" : args
  dir = null==dir ? "" : dir
  if (${dir}) {
    sh "cd ${env.WORKSPACE}/${dir} && touch text.txt"
  }
  sh "${tool 'M3'}/bin/mvn ${args}"
}
