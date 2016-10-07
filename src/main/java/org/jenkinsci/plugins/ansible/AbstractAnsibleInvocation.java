/*
 *     Copyright 2015-2016 Jean-Christophe Sirot <sirot@chelonix.com>
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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

/**
 * Ansible command invocation
 */
abstract class AbstractAnsibleInvocation<T extends AbstractAnsibleInvocation<T>> {

    protected final EnvVars envVars;
    protected final TaskListener listener;
    protected final Run<?, ?> build;
    protected final Map<String, String> environment;

    protected String exe;
    protected int forks;
    protected boolean sudo;
    protected String sudoUser;
    protected StandardUsernameCredentials credentials;
    protected List<ExtraVar> extraVars;
    protected String additionalParameters;

    private FilePath key = null;
    private FilePath script = null;
    private Inventory inventory;
    private boolean copyCredentialsInWorkspace = false;
    private final FilePath ws;

    protected AbstractAnsibleInvocation(String exe, Run<?, ?> build, FilePath ws, TaskListener listener, EnvVars envVars)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        this.build = build;
        this.ws = ws;
        this.envVars = envVars;
        this.environment = new HashMap<String, String>(this.envVars);
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
//            throw new AnsibleInvocationException(
//                    "The inventory of hosts and groups is not defined. Check the job configuration.");
            return args;
        }
        inventory.addArgument(args, ws, envVars, listener);
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

    public T setExtraVars(List<ExtraVar> extraVars) {
        this.extraVars = extraVars;
        return (T) this;
    }

    public ArgumentListBuilder appendExtraVars(ArgumentListBuilder args) {
        if (extraVars != null && ! extraVars.isEmpty()) {
            for (ExtraVar var : extraVars) {
                args.add("-e");
                String value = envVars.expand(var.getValue());
                if (Pattern.compile("\\s").matcher(value).find()) {
                    value = Util.singleQuote(value);
                }
                StringBuilder sb = new StringBuilder();
                sb.append(envVars.expand(var.getKey())).append("=").append(value);
                if (var.isHidden()) {
                    args.addMasked(sb.toString());
                } else {
                    args.add(sb.toString());
                }
            }
        }
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

    public T setCredentials(StandardUsernameCredentials credentials, boolean copyCredentialsInWorkspace) {
        this.copyCredentialsInWorkspace = copyCredentialsInWorkspace;
        return setCredentials(credentials);
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
            key = Utils.createSshKeyFile(key, ws, privateKeyCredentials, copyCredentialsInWorkspace);
            args.add("--private-key").add(key);
            args.add("-u").add(privateKeyCredentials.getUsername());
            if (privateKeyCredentials.getPassphrase() != null) {
                script = Utils.createSshAskPassFile(script, ws, privateKeyCredentials, copyCredentialsInWorkspace);
                environment.put("SSH_ASKPASS", script.getRemote());
                // inspired from https://github.com/jenkinsci/git-client-plugin/pull/168
                // but does not work with MacOSX
                if (! environment.containsKey("DISPLAY")) {
                    environment.put("DISPLAY", ":123.456");
                }
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
            if (inventory != null) {
                inventory.tearDown(listener);
            }
            Utils.deleteTempFile(key, listener);
            Utils.deleteTempFile(script, listener);
        }
    }
}
