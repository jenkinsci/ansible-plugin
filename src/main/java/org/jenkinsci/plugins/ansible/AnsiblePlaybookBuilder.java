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

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
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
        try {
            AnsiblePlaybookInvocation invocation = new AnsiblePlaybookInvocation(ansibleName, build, launcher, listener);
            invocation.setPlaybook(playbook);
            invocation.setInventory(inventory);
            invocation.setLimit(limit);
            invocation.setTags(tags);
            invocation.setSkippedTags(skippedTags);
            invocation.setStartTask(startAtTask);
            invocation.setSudo(sudo, sudoUser);
            invocation.setForks(forks);
            invocation.setCredentials(credentialsId);
            invocation.setAdditionalParameters(additionalParameters);
            invocation.setHostKeyCheck(hostKeyChecking);
            invocation.setUnbufferedOutput(unbufferedOutput);
            invocation.setColorizedOutput(colorizedOutput);
            return invocation.execute();
        } catch (IOException ioe) {
            Util.displayIOException(ioe, listener);
            ioe.printStackTrace(listener.fatalError(hudson.tasks.Messages.CommandInterpreter_CommandFailed()));
            return false;
        } catch (AnsibleInvocationException aie) {
            listener.fatalError(aie.getMessage());
            return false;
        }
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
