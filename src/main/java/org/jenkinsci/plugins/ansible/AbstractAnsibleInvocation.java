/*
 *     Copyright 2015 Jean-Christophe Sirot <sirot@chelonix.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.ansible;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Ansible command invocation
 */
abstract class AbstractAnsibleInvocation<T extends AbstractAnsibleInvocation<T>> {

    protected final EnvVars envVars;
    protected final BuildListener listener;
    protected final AbstractBuild<?, ?> build;
    protected final ArgumentListBuilder args = new ArgumentListBuilder();
    protected final Map<String, String> environment = new HashMap<String, String>();
    protected final Launcher launcher;

    private File key = null;
    private Inventory inventory;

    protected AbstractAnsibleInvocation(String ansibleInstallation, AnsibleCommand command, AbstractBuild<?, ?> build,
                                        Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        this.build = build;
        this.envVars = build.getEnvironment(listener);
        this.launcher = launcher;
        this.listener = listener;

        String exe = getInstallation(ansibleInstallation).getExecutable(command, launcher);
        if (exe == null) {
            throw new AnsibleInvocationException("Ansible executable not found, check your installation.");
        }
        args.add(exe);
    }

    public T setInventory(Inventory inventory) throws IOException, InterruptedException, AnsibleInvocationException {
        this.inventory = inventory;
        if (inventory == null) {
            throw new AnsibleInvocationException(
                    "The inventory of hosts and groups is not defined. Check the job configuration.");
        }
        inventory.getHandler().addArgument(args, envVars, listener);
        return (T) this;
    }

    public T setForks(int forks) {
        args.add("-f").add(forks);
        return (T) this;
    }

    public T setAdditionalParameters(String additionalParameters) {
        args.addTokenized(envVars.expand(additionalParameters));
        return (T) this;
    }

    public T setSudo(boolean sudo, String sudoUser) {
        if (sudo) {
            args.add("-s");
            if (StringUtils.isNotBlank(sudoUser)) {
                args.add("-U").add(envVars.expand(sudoUser));
            }
        }
        return (T) this;
    }

    public T setCredentials(String credentialsId) throws IOException, InterruptedException {
        if (StringUtils.isNotBlank(credentialsId)) {
            SSHUserPrivateKey credentials = CredentialsProvider.findCredentialById(credentialsId, SSHUserPrivateKey.class, build);
            key = Utils.createSshKeyFile(key, credentials);
            args.add("--private-key").add(key);
            args.add("-u").add(credentials.getUsername());
        }
        return (T) this;
    }

    public T setUnbufferedOutput(boolean unbufferedOutput) {
        if (unbufferedOutput) {
            environment.put("PYTHONUNBUFFERED", "1");
        }
        return (T) this;
    }

    public T setColorizedOutput(boolean colorizedOutput) {
        if (colorizedOutput) {
            environment.put("ANSIBLE_FORCE_COLOR", "true");
        }
        return (T) this;
    }

    public T setHostKeyCheck(boolean hostKeyChecking) {
        if (! hostKeyChecking) {
            environment.put("ANSIBLE_HOST_KEY_CHECKING", "False");
        }
        return (T) this;
    }

    private AnsibleInstallation getInstallation(String ansibleInstallation) throws IOException {
        if (ansibleInstallation == null) {
            if (AnsibleInstallation.allInstallations().length == 0) {
                throw new IOException("Ansible not found");
            }
            return AnsibleInstallation.allInstallations()[0];
        } else {
            for (AnsibleInstallation installation: AnsibleInstallation.allInstallations()) {
                if (ansibleInstallation.equals(installation.getName())) {
                    return installation;
                }
            }
        }
        throw new IOException("Ansible not found");
    }

    public boolean execute() throws IOException, InterruptedException {
        try {
            if (launcher.launch().pwd(build.getWorkspace()).envs(environment).cmds(args).stdout(listener).join() != 0) {
                return false;
            }
        } finally {
            inventory.getHandler().tearDown(listener);
            Utils.deleteTempFile(key, listener);
        }
        return true;
    }
}
