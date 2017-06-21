#!/usr/bin/env groovy
def call(args) {
  args = null==args ? "-Dmaven.test.failure.ignore  test" : args
  sh "${tool 'M3'}/bin/mvn ${args}"
}
