#!/usr/bin/env groovy
/*
必须包含(待测试tomcat_update.sh  AppRunAs HealthCheckFunction DIR_SRC_UPDATE CheckUrl) saltmasterIP、NgHostName 、 NGINX_CONF 、 NGINX_DAEMON 、 APP_HOSTS 和APP_PORT 五个属性
*/
def call(body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()
	
	try {
	def currentTime=System.currentTimeMillis()
	
	//nginx 主机名称 
	def NgHostName="${config.NgHostName}";
	//nginx conf file配置文件 
	def NGINX_CONF="${config.NGINX_CONF}";
    //nginx 启动文件 
	def NGINX_DAEMON="${config.NGINX_DAEMON}";
	//saltmaster主机 ip
	def saltmasterIP="${config.saltmasterIP}";
	//upstream 后端主机port
	def APP_PORT="${config.APP_PORT}";
	def CheckUrl="${config.CheckUrl}";
	def projectName="${config.projectName}";
	def dir_update="${config.dir_update}";
	//def APP_HOSTIP=config.APP_HOSTS.split(',');
	//def APP_HOSTNAMES=config.APP_HOSTNAMES.split(',');
	def APP_HOSTSIZE=config.APP_HOSTS.size();
	
	
	for (i = 0; i <${APP_HOSTSIZE}; i++) {
		//down nginx upstream host地址
		def APP_HOST=config.APP_HOSTS[i].split(',')[0].trim();
		def APP_HOSTNAME=APP_HOSTNAMES[i].split(',')[0].trim();
		println  " ${i} ${APP_HOST} ${APP_HOSTNAME}";
		println "-------------------------------------ready for ${NgHostName} nginx down ${APP_HOST} ${APP_PORT} ";
		sh  "ssh ${saltmasterIP}  'sudo salt -L \"${NgHostName}\" cmd.script salt://scripts/nginx_up_down.sh \"down ${NGINX_CONF} ${NGINX_DAEMON} ${APP_HOST} ${APP_PORT}\" ' ";
		
		println "-------------------------------------ok for ${NgHostName} nginx down ${APP_HOST} ${APP_PORT} "
		//update tomcat war and checkUrl
		sh  "ssh ${saltmasterIP}  'sudo salt -L \"${APP_HOSTNAME}\" cmd.script salt://scripts/update_tomcat.sh \"update-all ${TOMCAT_HOME} ${projectName} ${dir_update} ${CheckUrl} ${APP_HOST} ${APP_PORT}\" runas=\"${AppRunAs}\" ' ";
		//up nginx upstream hosts地址
		sh  "ssh ${saltmasterIP}  'sudo salt -L \"${NgHostName}\" cmd.script salt://scripts/nginx_up_down.sh \"up ${NGINX_CONF} ${NGINX_DAEMON} ${APP_HOST} ${APP_PORT}\" ' ";
		println "-------------------------------------ok for ${NgHostName} nginx up ${APP_HOST} ${APP_PORT} "

	}
	  

    } catch (err) {
      println "Failled: ${err}"
    }
}