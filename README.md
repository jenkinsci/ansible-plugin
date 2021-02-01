Jenkins Ansible Plugin
======================

This plugin gives the possibility to run [Ansible](http://www.ansible.com/) ad-hoc command or playbooks as a build step.

Jenkins Wiki page: https://wiki.jenkins-ci.org/display/JENKINS/Ansible+Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/ansible-plugin/master)](https://ci.jenkins.io/job/Plugins/job/ansible-plugin/job/master/)

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
        extraVars {
            extraVar("key1", "value1", false)
            extraVar("key2", "value2", true)
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

Extra variables can be passed to ansible by using a map in the pipeline script. Use the `hidden` parameter 
to keep the variable secret in the build log.

```groovy  
node {
    ansiblePlaybook(
        inventory: 'local_inventory/hosts.cfg',
        playbook: 'cloud_playbooks/create-aws.yml',
        extraVars: [
            login: 'mylogin',
            secret_key: [value: 'g4dfKWENpeF6pY05', hidden: true]
        ])
}
```

### Colorized Console Log

You need to install the [AnsiColor plugin](https://wiki.jenkins-ci.org/display/JENKINS/AnsiColor+Plugin) to output a 
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
