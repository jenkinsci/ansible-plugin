freeStyleJob('ansible') {
    steps {
        ansiblePlaybookBuilder {
            playbook('path/playbook.yml')
            inventory {
                inventoryDoNotSpecify()
            }
            unbufferedOutput(true)
            extraVars {
                extraVar {
                    key('key')
                    secretValue(hudson.util.Secret.fromString('value'))
                    hidden(true)
                }
            }
        }
    }
}
