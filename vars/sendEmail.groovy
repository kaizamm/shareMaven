#!/usr/bin/env groovy
/*
需要指定emailRecipients,来源于jenkinspipeline.properties中的projectRecipientList，以逗号分隔
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
        error "exit"
    }
}
