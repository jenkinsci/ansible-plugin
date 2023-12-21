freeStyleJob('ansible') {
    steps {
        ansiblePlaybook('path/playbook.yml') {
            inventoryPath('hosts.ini')
            ansibleName('1.9.4')
            limit('retry.limit')
            checkMode(true)
        }
    }
}
