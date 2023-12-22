pipeline {
    agent {
        label('test-agent')
    }
    stages {
        stage('Ansible adhoc command') {
            steps {
                ansibleAdhoc(
                    inventory: '/ansible/inventory.yml',
                    hosts: '127.0.0.1',
                    moduleArguments: 'echo something',
                )
            }
        }
    }
}
