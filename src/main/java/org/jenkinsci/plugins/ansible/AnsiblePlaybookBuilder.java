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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * A builder which wraps an Ansible playbook invocation.
 */
public class AnsiblePlaybookBuilder extends Builder implements SimpleBuildStep
{

    public final String playbook;

    public final Inventory inventory;

    public String ansibleName = null;

    public String limit = null;

    public String tags = null;

    public String skippedTags = null;

    public String startAtTask = null;

    /**
     * The id of the credentials to use.
     */
    public String credentialsId = null;

    public boolean sudo = false;

    public String sudoUser = "root";

    public int forks = 5;

    public boolean unbufferedOutput = true;

    public boolean colorizedOutput = false;

    public boolean hostKeyChecking = false;

    public String additionalParameters = null;

    public boolean copyCredentialsInWorkspace = false;

    public List<ExtraVar> extraVars;

    @Deprecated
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

    @DataBoundConstructor
    public AnsiblePlaybookBuilder(String playbook, Inventory inventory) {
        this.playbook = playbook;
        this.inventory = inventory;
    }

    @DataBoundSetter
    public void setAnsibleName(String ansibleName) {
        this.ansibleName = ansibleName;
    }

    @DataBoundSetter
    public void setLimit(String limit) {
        this.limit = limit;
    }

    @DataBoundSetter
    public void setTags(String tags) {
        this.tags = tags;
    }

    @DataBoundSetter
    public void setSkippedTags(String skippedTags) {
        this.skippedTags = skippedTags;
    }

    @DataBoundSetter
    public void setStartAtTask(String startAtTask) {
        this.startAtTask = startAtTask;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        setCredentialsId(credentialsId, false);
    }

    public void setCredentialsId(String credentialsId, boolean copyCredentialsInWorkspace) {
        this.credentialsId = credentialsId;
        this.copyCredentialsInWorkspace = copyCredentialsInWorkspace;
    }

    @DataBoundSetter
    public void setSudo(boolean sudo) {
        this.sudo = sudo;
    }

    @DataBoundSetter
    public void setSudoUser(String sudoUser) {
        this.sudoUser = sudoUser;
    }

    @DataBoundSetter
    public void setForks(int forks) {
        this.forks = forks;
    }

    @DataBoundSetter
    public void setUnbufferedOutput(boolean unbufferedOutput) {
        this.unbufferedOutput = unbufferedOutput;
    }

    @DataBoundSetter
    public void setColorizedOutput(boolean colorizedOutput) {
        this.colorizedOutput = colorizedOutput;
    }

    @DataBoundSetter
    public void setHostKeyChecking(boolean hostKeyChecking) {
        this.hostKeyChecking = hostKeyChecking;
    }

    @DataBoundSetter
    public void setAdditionalParameters(String additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    @DataBoundSetter
    public void setExtraVars(List<ExtraVar> extraVars) {
        this.extraVars = extraVars;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath ws, @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws InterruptedException, IOException
    {
        Computer computer = Computer.currentComputer();
        if (computer == null) {
            throw new AbortException("The ansible playbook build step requires to be launched on a node");
        }
        perform(run, computer.getNode(), ws, launcher, listener, run.getEnvironment(listener));
    }

    public void perform(@Nonnull Run<?, ?> run, @Nonnull Node node, @Nonnull FilePath ws, @Nonnull Launcher launcher, @Nonnull TaskListener listener, EnvVars envVars)
            throws InterruptedException, IOException
    {
        try {
            CLIRunner runner = new CLIRunner(run, ws, launcher, listener);
            String exe = AnsibleInstallation.getExecutable(ansibleName, AnsibleCommand.ANSIBLE_PLAYBOOK, node, listener, envVars);
            AnsiblePlaybookInvocation invocation = new AnsiblePlaybookInvocation(exe, run, ws, listener, envVars);
            invocation.setPlaybook(playbook);
            invocation.setInventory(inventory);
            invocation.setLimit(limit);
            invocation.setTags(tags);
            invocation.setSkippedTags(skippedTags);
            invocation.setStartTask(startAtTask);
            invocation.setSudo(sudo, sudoUser);
            invocation.setForks(forks);
            invocation.setCredentials(StringUtils.isNotBlank(credentialsId) ?
                CredentialsProvider.findCredentialById(credentialsId, StandardUsernameCredentials.class, run) : null,
                copyCredentialsInWorkspace);
            invocation.setExtraVars(extraVars);
            invocation.setAdditionalParameters(additionalParameters);
            invocation.setHostKeyCheck(hostKeyChecking);
            invocation.setUnbufferedOutput(unbufferedOutput);
            invocation.setColorizedOutput(colorizedOutput);
            if (!invocation.execute(runner)) {
                throw new AbortException("Ansible playbook execution failed");
            }
        } catch (IOException ioe) {
            Util.displayIOException(ioe, listener);
            ioe.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_CommandFailed()));
            throw ioe;
        } catch (AnsibleInvocationException aie) {
            listener.fatalError(aie.getMessage());
            throw new AbortException(aie.getMessage());
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
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
