pipeline {
    agent {
        label('test-agent')
    }
    stages {
        stage('Ansible playbook') {
            steps {
                ansiblePlaybook(
                    playbook: '/ansible/playbook.yml',
                    vaultCredentialsId: 'vaultCredentialsFile',
                )
            }
        }
    }
}
