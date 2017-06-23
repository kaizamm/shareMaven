#!/usr/bin/env groovy
def call(path){
  def envList = []
  def props = readProperties file: path
  for (it2 in mapToList(props)) {
      def key=it2[0]
      def val=it2[1]
      envList << key+"="+val
  }
  return envList
}