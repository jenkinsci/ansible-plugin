# Ansible Plugin

![Build](https://ci.jenkins.io/job/Plugins/job/ansible-plugin/job/main/badge/icon)
[![Coverage](https://ci.jenkins.io/job/Plugins/job/ansible-plugin/job/main/badge/icon?status=${instructionCoverage}&subject=coverage&color=${colorInstructionCoverage})](https://ci.jenkins.io/job/Plugins/job/ansible-plugin/job/main)
[![LOC](https://ci.jenkins.io/job/Plugins/job/ansible-plugin/job/main/badge/icon?job=test&status=${lineOfCode}&subject=line%20of%20code&color=blue)](https://ci.jenkins.io/job/Plugins/job/ansible-plugin/job/main)
![Contributors](https://img.shields.io/github/contributors/jenkinsci/ansible-plugin.svg?color=blue)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/ansible-plugin.svg?label=changelog)](https://github.com/jenkinsci/ansible-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/ansible.svg?color=blue)](https://plugins.jenkins.io/ansible)
[![GitHub license](https://img.shields.io/github/license/jenkinsci/ansible-plugin)](https://github.com/jenkinsci/ansible-plugin/blob/main/LICENSE.md)

This plugin allows to execute [Ansible](http://www.ansible.com/) tasks as a job build step.

## Global Configuration

Ansible needs to be on the PATH for the build job in order to be used.
This can be done through either Jenkins Global Tool Configuration or
including Ansible on the OS User PATH variable.

### Global Tool Configuration

Configuring Ansible through the Global Tool Configuration in Jenkins
(Jenkins → Manage Jenkins → Global Tool Configuration) allows for
multiple Ansible installations to be present and used by different
Jenkins jobs.

1.  Click "Add Ansible"
2.  Configure the name and path

    | Field name                            | Description                                                                                               |
    | --------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
    | Name                                  | Symbolic name used to identify a specific Ansible installation when multiple installations are configured |
    | Path to ansible executables directory | Directory containing the *ansible,* *ansible-playbook*, and *ansible-vault* binaries                      |

3.  Repeat for any additional desired installations

There is no automatic ansible installation possible using Global Tools.

### OS User PATH

Ansible can also be added to the PATH user used by the Jenkins executor
instead of configured through Global Tool Configuration. This is done
through normal OS tools outside of Jenkins and is not covered by this
guide.

------------------------------------------------------------------------

## Supported versions

The plugin is tested against supported ansible-core versions (https://endoflife.date/ansible-core). It might work with older versions, but this is not guaranteed.

See `PipelineTest.java`

```java
private static Stream<String> ansibleVersions() {
    return Stream.of("2.18.12", "2.19.5", "2.20.1");
}
```

## Adhoc

[Adhoc commands](http://docs.ansible.com/ansible/latest/intro_adhoc.html) allow
for simple operations to be done without writing a full playbook. This
allows for a convenient way of doing quick tasks with Ansible.

### Examples

#### Scripted

**Jenkinsfile**

``` groovy
ansibleAdhoc credentialsId: 'private_key', inventory: 'inventories/a/hosts', hosts: 'hosts_pattern', moduleArguments: 'module_arguments'
```

#### Declarative

**Jenkinsfile**

``` groovy
ansibleAdhoc(credentialsId: 'private_key', inventory: 'inventories/a/hosts', hosts: 'hosts_pattern', moduleArguments: 'module_arguments')
```

### Arguments

See also [jenkins.io](https://jenkins.io/doc/pipeline/steps/ansible/) documentation.

| Freestyle Name                         | Pipeline Name      |  Description                                                  |
| -------------------------------------- | ------------------ | ------------------------------------------------------------- |
| Ansible installation                   | installation       | Ansible installation to use for the playbook invocation       |
| Host pattern                           | hosts              | The host pattern to manage. See Ansible Patterns for details. |
| Module                                 | module             | CLI arg: `-m`                                                 |
| Module arguments or command to execute | moduleArguments    | CLI arg: `-a`                                                 |
| Inventory file or host list            | inventory          | CLI arg: `-i`: See the Inventory section for additional details. |
| Inventory inline content               | inventoryContent   | CLI arg: `-i`: See the Inventory section for additional details. |
| Credentials                            | credentialsId      | The Jenkins credential to use for the SSH connection. See the Authentication section for additional details. |
| Vault Credentials                      | vaultCredentialsId | CLI arg: `--vault-password-file`: The Jenkins credential to use as the vault credential. See the Vault Credentials section for additional details. |
| Vault temp path                        | vaultTmpPath       | Path where to store temporary vault secrets files, ssh key files, etc... Default is in workspace. |
| sudo                                   | become             | CLI arg: `-s` |
| sudo user                              | becomeUser         | CLI arg: `-U` |
| Number of parallel processes           | forks              | CLI arg: `-f` |
| Check host SSH key                     | hostKeyChecking    | Toggle checking of the host key. Sets the environment variable `ANSIBLE_HOST_KEY_CHECKING`, similar to the recommendations for running with Vagrant. |
| Unbuffered stdout                      |                    | Toggle buffering of standard out. Sets the environment variable `PYTHONUNBUFFERED`, similar to the recommendations for running with Vagrant. |
| Colorized stdout                       | colorized          | Toggle color codes in console text. See Colorized Output section for example usage. Sets the environment variable `ANSIBLE_FORCE_COLOR`, similar to the recommendations for running with Vagrant. |
| Extra Variables                        | extraVars          | CLI arg: `-e` |
| Additional parameters                  | extras             | String passed to the Ansible Command Line invocation as-is. |

## Playbook

[Ansible playbook](http://docs.ansible.com/ansible/latest/playbooks.html)
operations can be run with the plugin. The plugin provides several
conveniences such as easily using credentials from the Jenkins
credential store, unbuffered color output in the log, etc.

### Examples

#### Scripted

**Jenkinsfile**

``` groovy
ansiblePlaybook credentialsId: 'private_key', inventory: 'inventories/a/hosts', playbook: 'my_playbook.yml'
```

#### Declarative

**Jenkinsfile**

``` groovy
ansiblePlaybook(credentialsId: 'private_key', inventory: 'inventories/a/hosts', playbook: 'my_playbook.yml')
```

Additional scripted and declarative pipeline examples can be found on
the plugin's [GitHub
readme](https://github.com/jenkinsci/ansible-plugin).

### Arguments

Refer to [jenkins.io](https://jenkins.io/doc/pipeline/steps/ansible/)
for documentation extracted from the online help of the plugin.

| Freestyle Name                         | Pipeline Name       | Description                                                   |
| -------------------------------------- | ------------------- | ------------------------------------------------------------- |
| Ansible installation                   | installation        | Ansible installation to use for the playbook invocation       |
| Playbook path                          | playbook            | Mandatory. The name of the playbook to run                    |
| Inventory file or host list            | inventory           | CLI arg: `-i`: See the inventory section for details.         |
| Inventory inline content               | inventoryContent    | CLI arg: `-i`: See the inventory section for details.         |
| Credentials                            | credentialsId       | The Jenkins credential to use for the SSH connection. See the Authentication section for additional details |
| Vault Credentials                      | vaultCredentialsId  | The Jenkins credential to use as the vault credential. See the Vault Credentials section for more details |
| Vault temp path                        | vaultTmpPath        | Path where to store temporary vault secrets files, ssh key files, etc... rkspace. |
| sudo                                   | sudo                | CLI arg: `-s`                                                 |
| sudo user                              | sudoUser            | CLI arg: `-U`                                                 |
| Host subset                            | limit               | CLI arg: `-l`                                                 |
| Tags to run                            | tags                | CLI arg: `-t`                                                 |
| Tags to skip                           | skippedTags         | CLI arg: `--skip-tags`                                        |
| Task to start at                       | startAtTask         | CLI arg: `--start-at-task`                                    |
| Number of parallel processes           | forks               | CLI arg: `-f`                                                 |
| Check host SSH key                     | hostKeyChecking     | Toggle checking of the host key. Sets the environment variable ANSIBLE_HOST_KEY_CHECKING, similar to the recommendations for running with Vagrant. |
| Colorized stdout                       | colorized           | Toggle color codes in console text. See Colorized Output section for example usage. Sets the environment variable ANSIBLE_FORCE_COLOR, similar to the recommendations for running with Vagrant.  |
| Additional parameters                  | extras              | String passed to the Ansible Command Line invocation as-is    |
| Extra Variables                        | extraVars           | CLI arg: `-e`                                                 |

Refer to the ansible-playbook manual page for details on how each
command line argument is interpreted.

### Authentication

#### SSH Keys

[SSH keys](https://help.ubuntu.com/community/SSH/OpenSSH/Keys) are the
recommended authentication method for SSH connections. The plugin
supports the credential type "SSH Username with private key" configured
in the Jenkins credential store through the [SSH crendentials
plugin](https://plugins.jenkins.io/ssh-credentials).

#### Password

Even if using SSH keys is recommended authentication method, password
authentication may sometimes be required. The plugin has supported
password based authentication since 0.3.0. When using password based
authentication, the [sshpass](http://sourceforge.net/projects/sshpass/)
binary is expected to be on the PATH. The plugin supports the credential
type "Username with password" configured in the Jenkins credential store
through the [SSH crendentials
plugin](https://plugins.jenkins.io/ssh-credentials).

### Vault Credentials

Vault credentials can be setup in the Jenkins credential store as either
a "Secret text" or a "Secret file".

### Colorized Output

The [AnsiColor plugin](https://plugins.jenkins.io/ansicolor) is needed
for colorized console output. Once installed, colorized output can be
enabled with the argument "colorized: true".

**Jenkinsfile**

``` groovy
ansiColor('xterm') {
    ansiblePlaybook(
        playbook: 'path/to/playbook.yml',
        inventory: 'path/to/inventory.ini',
        credentialsId: 'sample-ssh-key',
        colorized: true)
}
```

![](docs/images/jenkins-deploy-ansible-console.png)

### Extra Parameters

Extra parameters is a string passed to the Ansible Command Line
invocation as-is and can be useful for arguments occasionally added to
an invocation at runtime, such as tags and host limits.

### Inventory

#### File

A string path to the inventory file to use with the playbook invocation.

#### Inline

The provided content is used as the content of the inventory file for
the playbook invocation.

### Using Jenkins Environment Variables

Jenkins environment variables can be accessed from within an Ansible
playbook. The Jenkins variables are injected as environment variables
making them available through the Ansible [lookup
plugin](http://docs.ansible.com/ansible/latest/playbooks_lookups.html).

The following Ansible playbook accesses the Jenkins BUILD\_TAG variable:

**playbook.yml**

``` groovy
---
- hosts: example
  tasks:
    - debug: msg="{{ lookup('env','BUILD_TAG') }}"
```

------------------------------------------------------------------------

## Vault

Most [Ansible Vault](https://docs.ansible.com/ansible/latest/vault.html)
operations can be performed with the plugin. Interactive operations such
as create, edit, and view are not supported through the plugin. One use
case for this enabling developers to encrypt secret values while keeping
the vault password a secret.

### Examples

#### Scripted

**Encrypts a File**

``` groovy
ansibleVault action: 'encrypt', input: 'vars/secrets.yml', vaultCredentialsId: 'ansible_vault_credentials'
```

**Encrypts a String**

``` groovy
ansibleVault action: 'encrypt_string', content: 'secret_content', vaultCredentialsId: 'ansible_vault_credentials'
```

#### Declarative

**Jenkinsfile**

``` groovy
ansibleVault(action: 'encrypt', input: 'vars/secrets.yml', vaultCredentialsId: 'ansible_vault_credentials')
```

**Jenkinsfile**

``` groovy
ansibleVault(action: 'encrypt_string', content: 'secret_content', vaultCredentialsId: 'ansible_vault_password')
```

### Arguments

See also [jenkins.io Pipeline step](https://jenkins.io/doc/pipeline/steps/ansible/) documentation.

| Freestyle Name                         | Pipeline Name         | Description                                                   |
| -------------------------------------- | --------------------- | ------------------------------------------------------------- |
| Ansible installation                   | installation          | Ansible installation to use for the playbook invocation       |
| Action                                 | action                | Mandatory. The name of the action to use. Interactive operations such as create, edit, and view are not supported. |
| Vault Credentials                      | vaultCredentialsId    | CLI arg: `--vault-password-file`. The Jenkins credential to use as the vault credential. See the Vault Credentials section for more details |
| New Vault Credentials                  | newVaultCredentialsId | CLI arg: `--new-vault-password-file`. The Jenkins credential to use as the vault credential. See the Vault Credentials section for more details |
| Vault temp path                        | vaultTmpPath          | Path where to store temporary vault secrets files, ssh key files, etc... Default is in workspace. |
| Content                                | content               | The content to encrypt with the 'encrypt_string' action.      |
| Input                                  | input                 | The file to encrypt with the encrypt actions.                 |
| Output                                 | output                | CLI arg: `--output`                                           |

### Vault Credentials

Vault credentials can be setup in the Jenkins credential store as either
a "Secret text" or a "Secret file".

------------------------------------------------------------------------

## Changelog

Changelog is now published on GitHub release.

## Using Jenkins Build and Environment Variables

It is possible to access build and environment variables in ansible playbooks. These variables are injected as environment variables within the ansible process. For example, use this code in an ansible playbook to access Jenkins ```BUILD_TAG``` variable.

```yaml
---
- hosts: example
  tasks:
    - debug: msg="{{ lookup('env','BUILD_TAG') }}"
```

## Job DSL support

```groovy
steps {
    ansiblePlaybook(String playbook) {
        inventoryPath(String path)
        inventoryContent(String content, boolean dynamic = false)
        ansibleName(String name)
        limit(String limit)
        tags(String tags)
        skippedTags(String tags)
        startAtTask(String task)
        credentialsId(String id)
        checkMode(boolean checkMode = false)
        become(boolean become = true)
        becomeUser(String user = 'root')
        sudo(boolean sudo = true)
        sudoUser(String user = 'root')
        forks(int forks = 5)
        unbufferedOutput(boolean unbufferedOutput = true)
        colorizedOutput(boolean colorizedOutput = false)
        hostKeyChecking(boolean hostKeyChecking = false)
        additionalParameters(String params)
        extraVars {
            extraVar(String key, String value, boolean hidden)
        }
    }

    ansibleAdHoc(String module, String command) {
        ansibleName(String name)
        inventoryPath(String path)
        inventoryContent(String content, boolean dynamic = false)
        credentialsId(String id)
        hostPattern(String pattern)
        become(boolean become = true)
        becomeUser(String user = 'root')
        sudo(boolean sudo = true)
        sudoUser(String user = 'root')
        forks(int forks = 5)
        unbufferedOutput(boolean unbufferedOutput = true)
        colorizedOutput(boolean colorizedOutput = false)
        hostKeyChecking(boolean hostKeyChecking = false)
        additionalParameters(String params)
        extraVars {
            extraVar(String key, String value, boolean hidden)
        }
    }
}
```

### Example

```groovy
steps {
    ansiblePlaybook('path/playbook.yml') {
        inventoryPath('hosts.ini')
        ansibleName('1.9.4')
        tags('one,two')
        credentialsId('credsid')
        become(true)
        becomeUser("user")
        checkMode(false)
        extraVars {
            extraVar("key1", "value1", false)
            extraVar("key2", "value2", true)
        }
    }
}
```

```groovy
steps {
    ansiblePlaybookBuilder {
        playbook('path/playbook.yml')
        inventory {
            inventoryDoNotSpecify()
        }
        unbufferedOutput(true)
        extraVars {
            extraVar {
                key('key1')
                secretValue(hudson.util.Secret.fromString('value1'))
                hidden(false)
            }
            extraVar {
                key('key2')
                secretValue(hudson.util.Secret.fromString('value2'))
                hidden(true)
            }
        }
    }
}
```

## Pipeline support

Ansible playbooks can be executed from workflow scripts. Only the `playbook` parameter is mandatory.

### Example

```groovy
node {
    ansiblePlaybook(
        playbook: 'path/to/playbook.yml',
        inventory: 'path/to/inventory.ini',
        credentialsId: 'sample-ssh-key',
        extras: '-e parameter="some value"')
}
```

### Extra Variables

Extra variables can be passed to ansible by using a map in the pipeline script.
Supported value types are: `String`, `Boolean`, `Number`.
By default the value will be considered potentially sensitive and masked in the logs.
To override this give a map with keys `value` and `hidden`.

```groovy
node {
    ansiblePlaybook(
        inventory: 'local_inventory/hosts.cfg',
        playbook: 'cloud_playbooks/create-aws.yml',
        extraVars: [
            login: 'mylogin',
            toggle: true,
            forks: 8,
            not_secret: [value: 'I want to see this in the logs', hidden: false]
        ])
}
```

### Colorized Console Log

You need to install the [AnsiColor plugin](https://plugins.jenkins.io/ansicolor/) to output a
colorized Ansible log.

```groovy
node {
    wrap([$class: 'AnsiColorBuildWrapper', colorMapName: "xterm"]) {
        ansiblePlaybook(
            playbook: 'path/to/playbook.yml',
            inventory: 'path/to/inventory.ini',
            credentialsId: 'sample-ssh-key',
            colorized: true)
    }
}
```

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)