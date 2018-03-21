freeStyleJob('ansible') {
    steps {
        ansiblePlaybook('path/playbook.yml') {
            inventoryPath('hosts.ini')
            ansibleName('1.9.4')
            limit('retry.limit')
            tags('one,two')
            skippedTags('three')
            startAtTask('task')
            credentialsId('credsid')
            sudo(true)
            sudoUser("user")
            forks(6)
            unbufferedOutput(false)
            colorizedOutput(true)
            hostKeyChecking(false)
            disableHostKeyChecking(true)
            additionalParameters('params')
            extraVars {
                extraVar ("key","value",true)
            }
        }
    }
}
