freeStyleJob('ansible') {
    steps {
        ansibleAdHoc('module', 'command') {
            ansibleName('1.9.1')
            credentialsId('credsid')
            hostPattern('pattern')
            inventoryContent('content')
        }
    }
}