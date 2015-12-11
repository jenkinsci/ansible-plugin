Jenkins Ansible Plugin
======================

This plugin gives the possibility to run [Ansible](http://www.ansible.com/) ad-hoc command or playbooks as a build step.

Jenkins Wiki page: https://wiki.jenkins-ci.org/display/JENKINS/Ansible+Plugin

[![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=plugins/ansible-plugin)](https://jenkins.ci.cloudbees.com/job/plugins/job/ansible-plugin/)

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
        sudo(boolean sudo = true)
        sudoUser(String user = 'root')
        forks(int forks = 5)
        unbufferedOutput(boolean unbufferedOutput = true)
        colorizedOutput(boolean colorizedOutput = false)
        hostKeyChecking(boolean hostKeyChecking = false)
        additionalParameters(String params)
    }
        
    ansibleAdHoc(String module, String command) {
        ansibleName(String name)
        inventoryPath(String path)
        inventoryContent(String content, boolean dynamic = false)
        credentialsId(String id)
        hostPattern(String pattern)
        sudo(boolean sudo = true)
        sudoUser(String user = 'root')
        forks(int forks = 5)
        unbufferedOutput(boolean unbufferedOutput = true)
        colorizedOutput(boolean colorizedOutput = false)
        hostKeyChecking(boolean hostKeyChecking = false)
        additionalParameters(String params)
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
        sudo(true)
        sudoUser("user")
    }
}
```
