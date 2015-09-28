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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

/**
 * Ansible command invocation
 */
abstract class AbstractAnsibleInvocation<T extends AbstractAnsibleInvocation<T>> {

    protected final EnvVars envVars;
    protected final BuildListener listener;
    protected final AbstractBuild<?, ?> build;
    protected final Map<String, String> environment = new HashMap<String, String>();

    protected String exe;
    protected int forks;
    protected boolean sudo;
    protected String sudoUser;
    protected StandardUsernameCredentials credentials;
    protected String additionalParameters;

    private FilePath key = null;
    private FilePath script = null;
    private Inventory inventory;

    protected AbstractAnsibleInvocation(String exe, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        this.build = build;
        this.envVars = build.getEnvironment(listener);
        this.listener = listener;
        this.exe = exe;
        if (exe == null) {
            throw new AnsibleInvocationException("Ansible executable not found, check your installation.");
        }
    }

    protected ArgumentListBuilder appendExecutable(ArgumentListBuilder args) {
        args.add(exe);
        return args;
    }

    public T setInventory(Inventory inventory) {
        this.inventory = inventory;
        return (T) this;
    }

    protected ArgumentListBuilder appendInventory(ArgumentListBuilder args)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        if (inventory == null) {
            throw new AnsibleInvocationException(
                    "The inventory of hosts and groups is not defined. Check the job configuration.");
        }
        inventory.addArgument(args, build.getWorkspace(), envVars, listener);
        return args;
    }

    public T setForks(int forks) {
        this.forks = forks;
        return (T) this;
    }

    public ArgumentListBuilder appendForks(ArgumentListBuilder args) {
        args.add("-f").add(forks);
        return args;
    }

    public T setAdditionalParameters(String additionalParameters) {
        this.additionalParameters = additionalParameters;
        return (T) this;
    }

    public ArgumentListBuilder appendAdditionalParameters(ArgumentListBuilder args) {
        args.addTokenized(envVars.expand(additionalParameters));
        return args;
    }

    public T setSudo(boolean sudo, String sudoUser) {
        this.sudo = sudo;
        this.sudoUser = sudoUser;
        return (T) this;
    }

    protected ArgumentListBuilder appendSudo(ArgumentListBuilder args) {
        if (sudo) {
            args.add("-s");
            if (StringUtils.isNotBlank(sudoUser)) {
                args.add("-U").add(envVars.expand(sudoUser));
            }
        }
        return args;
    }

    public T setCredentials(StandardUsernameCredentials credentials) {
        this.credentials = credentials;
        return (T) this;
    }

    protected ArgumentListBuilder prependPasswordCredentials(ArgumentListBuilder args) {
        if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials passwordCredentials = (UsernamePasswordCredentials)credentials;
            args.add("sshpass").addMasked("-p" + Secret.toString(passwordCredentials.getPassword()));
        }
        return args;
    }

    protected ArgumentListBuilder appendCredentials(ArgumentListBuilder args)
            throws IOException, InterruptedException
    {
        if (credentials instanceof SSHUserPrivateKey) {
            SSHUserPrivateKey privateKeyCredentials = (SSHUserPrivateKey)credentials;
            key = Utils.createSshKeyFile(key, build.getWorkspace(), privateKeyCredentials);
            args.add("--private-key").add(key);
            args.add("-u").add(privateKeyCredentials.getUsername());
            if (privateKeyCredentials.getPassphrase() != null) {
                script = Utils.createSshAskPassFile(script, build.getWorkspace(), privateKeyCredentials);
                environment.put("SSH_ASKPASS", script.getRemote());
            }
        } else if (credentials instanceof UsernamePasswordCredentials) {
            args.add("-u").add(credentials.getUsername());
            args.add("-k");
        }
        return args;
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

    abstract protected ArgumentListBuilder buildCommandLine()
            throws InterruptedException, AnsibleInvocationException, IOException;

    public boolean execute(CLIRunner runner) throws IOException, InterruptedException, AnsibleInvocationException {
        try {
            return runner.execute(buildCommandLine(), environment);
        } finally {
            inventory.tearDown(listener);
            Utils.deleteTempFile(key, listener);
            Utils.deleteTempFile(script, listener);
        }
    }
}
