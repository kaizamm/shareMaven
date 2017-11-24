def call(body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

	def currentTime=System.currentTimeMillis()

	//本地文件或目录
	def localFile="${env.WORKSPACE}/${config.localFile}";
	//远端发布主目录
	def remoteDir=config.remoteDir;
	def remoteIps="${env.remoteIps}"
	def remoteIpsArry=remoteIps.split(',')
	println remoteIpsArry
  // for (i = 0; i<remoteIpsArry.size(); i++) {
  //   def remoteIp=config.remoteIpsArry[i].trim();
	// 	println remoteIp
    // sh "pwd"
    // sh "scp -r ${localFile} ${remoteIp}:${remoteDir}/"
  }
}
