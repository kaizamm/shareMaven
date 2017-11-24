def call(body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()
	config.javaOpts= config.javaOpts==null ? "" : config.javaOpts

	def currentTime=System.currentTimeMillis()

	//本地文件或目录
	def localFile="${env.WORKSPACE}/${config.localFile}";
	//远端发布主目录
	def remoteDir=config.remoteDir;
	def remoteIps="${env.remoteIps}"
	def remoteIpsArry=remoteIps.split(' ')

  for (i = 0; i<remoteIpsArry.size(); i++) {
    def remoteIp=remoteIpsArry[i].split(',').trim();
		println remoteIp
    // sh "pwd"
    // sh "scp -r ${localFile} ${remoteIp}:${remoteDir}/"
  }
}
