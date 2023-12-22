pipeline {
    agent {
        label('test-agent')
    }
    stages {
        stage('Ansible playbook') {
            steps {
                withCredentials([file(credentialsId: 'vaultCredentialsFileViaExtras', variable: 'VAULT_FILE')]) {
                    ansiblePlaybook(
                        playbook: '/ansible/playbook.yml',
                        extras: '--vault-password-file $VAULT_FILE',
                    )
                }
            }
        }
    }
}
