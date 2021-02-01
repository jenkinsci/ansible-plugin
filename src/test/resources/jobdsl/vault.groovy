freeStyleJob('ansible') {
    steps {
        ansibleVault() {
            action('encrypt_string')
            content('my_secret')
            vaultCredentialsId('ansible_vault_credentials')
        }
    }
}
