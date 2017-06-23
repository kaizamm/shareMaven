#!/usr/bin/env groovy
def call(path){
	def props = readProperties file: path
	def dlist = []
	for (entry in props) {
		dlist.add(entry.key+"="+entry.value)
	}
	return dlist
}
