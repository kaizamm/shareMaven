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

  for (i = 0; i<config.remoteIps.size(); i++) {
    def remoteIp=config.remoteIps[i].split(',').trim();
    sh "pwd"
    sh "scp -r ${localFile} ${remoteIp}:${remoteDir}/"
  }
}
