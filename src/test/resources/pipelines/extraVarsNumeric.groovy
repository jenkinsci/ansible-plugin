pipeline {
    agent {
        label('test-agent')
    }
    stages {
        stage('Ansible playbook') {
            steps {
                ansiblePlaybook(
                        playbook: '/ansible/playbook.yml',
                        extraVars: [foo1: 8],
                )
            }
        }
    }
}
