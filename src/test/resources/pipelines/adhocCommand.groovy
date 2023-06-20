pipeline {
    agent {
        label('test-agent')
    }
    stages {
        stage('Create inventory') {
            steps {
                writeFile(encoding: 'UTF-8', file: 'inventory', text: '''127.0.0.1 ansible_connection=local''')
                }
        }
        stage('Ansible adhoc command') {
            steps {
                warnError(message: 'ansible command not found?') {
                    ansibleAdhoc(
                        inventory: 'inventory',
                        hosts: '127.0.0.1',
                        moduleArguments: 'echo something',
                    )
                }
            }
        }
    }
}
