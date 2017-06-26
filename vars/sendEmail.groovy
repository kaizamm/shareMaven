#!/usr/bin/env groovy
/*
def call(String buildStatus = 'STARTED',String emailTo = '') {
	buildStatus =  buildStatus ?: 'SUCCESSFUL'
	def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
	def summary = "${subject} (${env.BUILD_URL})"
	def details = """<p>${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
	<p>点击查看详情 &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>
	<p>此邮件为<b>开发环境</b>jenkins发出。</p>
	<p>如有疑问请联系 ChenglanGuo@quarkfinance.com</p>"""
	emailext attachLog: true,
	body: details, mimeType: 'text/html', replyTo: "${env.projectRecipientList}",
	subject: subject,
	recipientProviders:  [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider'], [$class: 'CulpritsRecipientProvider'], [$class: 'FailingTestSuspectsRecipientProvider'], [$class: 'FirstFailingBuildSuspectsRecipientProvider'], [$class: 'UpstreamComitterRecipientProvider']],
	to: emailTo
}
*/

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    try {
        body()
    } catch(e) {
        currentBuild.result = "FAILURE";
        throw e;
    } finally {
        def subject = config.subject ? config.subject : "${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${currentBuild.result}!"
        def content = '${JELLY_SCRIPT,template="static-analysis"}'
        // Attach buildlog when the build is not successfull
        def attachLog = (config.attachLog != null) ? config.attachLog : (currentBuild.result != "SUCCESS")
        // Send email
        emailext(body: content, mimeType: 'text/html',
                replyTo: '$DEFAULT_REPLYTO', subject: subject,
                to: config.emailRecipients, attachLog: attachLog, recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']])
    }
}
