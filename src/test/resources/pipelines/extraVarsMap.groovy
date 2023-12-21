pipeline {
    agent {
        label('test-agent')
    }
    stages {
        stage('Ansible playbook') {
            steps {
                ansiblePlaybook(
                    playbook: '/ansible/playbook.yml',
                    extraVars: [foo1: [value: 'bar1', hidden: false]],
                )
                ansiblePlaybook(
                    playbook: '/ansible/playbook.yml',
                    extraVars: [foo2: [value: 'bar2', hidden: true]],
                )
            }
        }
    }
}
