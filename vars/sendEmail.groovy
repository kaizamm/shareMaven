#!/usr/bin/env groovy
/*
需要指定emailRecipients,来源于jenkinspipeline.properties中的projectRecipientList，以逗号分隔
*/
def call(body) {
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
        def attachLog = (config.attachLog != null) ? config.attachLog : (currentBuild.result != "SUCCESS")
        emailext(body: content, mimeType: 'text/html',
                replyTo: '$DEFAULT_REPLYTO', subject: subject,
                to: config.emailRecipients, attachLog: attachLog, recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']])
    }
}
