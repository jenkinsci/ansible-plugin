pipeline {
    agent {
        label('test-agent')
    }
    stages {
        stage('Create playbook') {
            steps {
                writeFile(encoding: 'UTF-8', file: 'playbook.yml', text: '''- hosts: localhost
  connection: local
  gather_facts: no
  tasks:
    - debug: msg=test
                ''')
                }
        }
        stage('Ansible playbook') {
            steps {
                warnError(message: 'ansible command not found?') {
                    withCredentials([file(credentialsId: 'vaultCredentialsFileViaExtras', variable: 'VAULT_FILE')]) {
                        ansiblePlaybook(
                            playbook: 'playbook.yml',
                            extras: '--vault-password-file $VAULT_FILE',
                        )
                    }
                }
            }
        }
    }
}
