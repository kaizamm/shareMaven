#!/usr/bin/env groovy

def call(args) {
  args = null==args ? "clean install -Dmaven.test.skip=true" : args
  sh "${tool 'M3'}/bin/mvn ${args}"
}
