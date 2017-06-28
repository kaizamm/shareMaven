#!/usr/bin/env groovy
// def call(path){
def call(String host,int port=2379,String location){
	def context = sh (script: "/data/jenkins_etcd/etcdRead.py ${host} ${port} ${location}",returnStdout: true)
	// def props = readProperties file: path
	def props = readProperties text: context
	def dlist = []
	for (entry in props) {
		dlist.add(entry.key+"="+entry.value)
	}
	return dlist
}
