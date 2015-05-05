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
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ansible.Inventory.InventoryHandler;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * A builder which wraps an Ansible playbook invocation.
 */
public class AnsiblePlaybookBuilder extends Builder
{

    public final String ansibleName;

    public final String limit;

    public final String tags;

    public final String skippedTags;

    public final String startAtTask;

    /**
     * The id of the credentials to use.
     */
    public final String credentialsId;

    public final String playbook;

    public final Inventory inventory;

    public final boolean sudo;

    public final String sudoUser;

    public final int forks;

    public final boolean unbufferedOutput;

    public final boolean colorizedOutput;

    public final boolean hostKeyChecking;

    public final String additionalParameters;

    @DataBoundConstructor
    public AnsiblePlaybookBuilder(String ansibleName, String playbook, Inventory inventory, String limit, String tags,
                                  String skippedTags, String startAtTask, String credentialsId, boolean sudo,
                                  String sudoUser, int forks, boolean unbufferedOutput, boolean colorizedOutput,
                                  boolean hostKeyChecking, String additionalParameters)
    {
        this.ansibleName = ansibleName;
        this.playbook = playbook;
        this.inventory = inventory;
        this.limit = limit;
        this.tags = tags;
        this.skippedTags = skippedTags;
        this.startAtTask = startAtTask;
        this.credentialsId = credentialsId;
        this.sudo = sudo;
        this.sudoUser = sudoUser;
        this.forks = forks;
        this.unbufferedOutput = unbufferedOutput;
        this.colorizedOutput = colorizedOutput;
        this.hostKeyChecking = hostKeyChecking;
        this.additionalParameters = additionalParameters;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException
    {
        AnsibleInstallation installation = getInstallation();
        EnvVars envVars = build.getEnvironment(listener);
        File key = null;
        String exe = installation.getExecutable(AnsibleCommand.ANSIBLE_PLAYBOOK, launcher);
        if (exe == null) {
            listener.fatalError("Ansible executable not found, check your installation.");
            return false;
        }
        ArgumentListBuilder args = new ArgumentListBuilder();

        Map<String, String> env = buildEnvironment();

        String playbook = envVars.expand(this.playbook);
        String limit = envVars.expand(this.limit);
        String tags = envVars.expand(this.tags);
        String skippedTags = envVars.expand(this.skippedTags);
        String startAtTask = envVars.expand(this.startAtTask);
        String sudoUser = envVars.expand(this.sudoUser);
        String additionalParameters = envVars.expand(this.additionalParameters);

        InventoryHandler inventoryHandler = inventory.getHandler();

        args.add(exe);
        args.add(playbook);
        inventoryHandler.addArgument(args, envVars, listener);

        if (StringUtils.isNotBlank(limit)) {
            args.add("-l").add(limit);
        }

        if (StringUtils.isNotBlank(tags)) {
            args.add("-t").add(tags);
        }

        if (StringUtils.isNotBlank(skippedTags)) {
            args.addKeyValuePair("", "--skip-tags", skippedTags, false);
        }

        if (StringUtils.isNotBlank(startAtTask)) {
            args.addKeyValuePair("", "--start-at-task", startAtTask, false);
        }

        if (sudo) {
            args.add("-s");
            if (sudoUser != null && !sudoUser.isEmpty()) {
                args.add("-U").add(sudoUser);
            }
        }

        args.add("-f").add(forks);

        if (StringUtils.isNotBlank(credentialsId)) {
            SSHUserPrivateKey credentials = CredentialsProvider.findCredentialById(credentialsId, SSHUserPrivateKey.class, build);
            key = Utils.createSshKeyFile(key, credentials);
            args.add("--private-key").add(key);
            args.add("-u").add(credentials.getUsername());
        }
        args.addTokenized(additionalParameters);

        try {
            if (launcher.launch().pwd(build.getWorkspace()).envs(env).cmds(args).stdout(listener).join() != 0) {
                return false;
            }
        } catch (IOException ioe) {
            Util.displayIOException(ioe, listener);
            ioe.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_CommandFailed()));
            return false;
        } finally {
            inventoryHandler.tearDown(listener);
            Utils.deleteTempFile(key, listener);
        }
        return true;
    }

    private Map<String, String> buildEnvironment() {
        Map<String, String> env = new HashMap<String, String>();
        if (unbufferedOutput) {
            env.put("PYTHONUNBUFFERED", "1");
        }
        if (colorizedOutput) {
            env.put("ANSIBLE_FORCE_COLOR", "true");
        }
        if (! hostKeyChecking) {
            env.put("ANSIBLE_HOST_KEY_CHECKING", "False");
        }
        return env;
    }

    public AnsibleInstallation getInstallation() throws IOException {
        if (ansibleName == null) {
            if (AnsibleInstallation.allInstallations().length == 0) {
                throw new IOException("Ansible not found");
            }
            return AnsibleInstallation.allInstallations()[0];
        } else {
            for (AnsibleInstallation installation: AnsibleInstallation.allInstallations()) {
                if (ansibleName.equals(installation.getName())) {
                    return installation;
                }
            }
        }
        throw new IOException("Ansible not found");
    }

    @Extension
    public static final class DescriptorImpl extends AbstractAnsibleBuilderDescriptor
    {
        public DescriptorImpl() {
            super("Invoke Ansible Playbook");
        }

        public FormValidation doCheckPlaybook(@QueryParameter String playbook) {
            return checkNotNullOrEmpty(playbook, "Path to playbook must not be empty");
        }
    }
}
