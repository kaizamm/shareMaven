#!/usr/bin/env groovy
def call(String host,int port,String location,env){
	def context = sh (script: "/data/jenkins_etcd/etcdRead.py ${host} ${port} ${location}",returnStdout: true)
	// def props = readProperties file: path
	def props = readProperties text: context
	def dlist = []
	for (entry in props) {
		env[entry.key]=entry.value
	}
}
