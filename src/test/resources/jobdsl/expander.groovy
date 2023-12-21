job('ansible') {
    steps {
        shell('''cat > playbook.yml << EOL
- hosts: localhost
  connection: local
  gather_facts: no
  tasks:
    - debug: msg=test
EOL
        ''')
        shell('mkdir -p inventory')
        ansiblePlaybook('playbook.yml') {
            inventoryPath('${inventory_repository}/inventory.yml')
            vaultCredentialsId('${vault_credentials_id}')
            credentialsId('${credentials_id}')
        }
    }
    parameters {
        stringParam('inventory_repository')
        stringParam('vault_credentials_id')
        stringParam('credentials_id')
    }
}
