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
package org.jenkinsci.plugins.ansible.workflow;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.google.inject.Inject;
import hudson.*;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.ansible.AnsibleInstallation;
import org.jenkinsci.plugins.ansible.AnsibleVaultBuilder;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * The Ansible vault invocation step for the Jenkins workflow plugin.
 */
public class AnsibleVaultStep extends AbstractStepImpl {

    private String installation;
    private String action;
    private String vaultCredentialsId;
    private String newVaultCredentialsId;
    private String content = null;
    private String input = null;
    private String output = null;

    @DataBoundConstructor
    public AnsibleVaultStep() {
    	
    }

    @DataBoundSetter
    public void setAction(String action) {
        this.action = action;
    }

    @DataBoundSetter
    public void setVaultCredentialsId(String vaultCredentialsId) {
        this.vaultCredentialsId = Util.fixEmptyAndTrim(vaultCredentialsId);
    }

    @DataBoundSetter
    public void setNewVaultCredentialsId(String newVaultCredentialsId) {
        this.newVaultCredentialsId = Util.fixEmptyAndTrim(newVaultCredentialsId);
    }

    @DataBoundSetter
    public void setContent(String content) {
        this.content = content;
    }

    @DataBoundSetter
    public void setInput(String input) {
        this.input = input;
    }

    @DataBoundSetter
    public void setOutput(String output) {
        this.output = output;
    }

    @DataBoundSetter
    public void setInstallation(String installation) {
        this.installation = Util.fixEmptyAndTrim(installation);
    }

    public String getInstallation() {
        return installation;
    }

    public String getAction() {
        return action;
    }

    public String getVaultCredentialsId() {
        return vaultCredentialsId;
    }

    public String getNewVaultCredentialsId() {
        return newVaultCredentialsId;
    }

    public String getContent() {
        return content;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(AnsibleVaultExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "ansibleVault";
        }

        @Override
        public String getDisplayName() {
            return "Invoke ansible vault";
        }

        public ListBoxModel doFillVaultCredentialsIdItems(@AncestorInPath Project project) {
            return new StandardListBoxModel()
                .withEmptySelection()
                .withMatching(anyOf(
                    instanceOf(FileCredentials.class),
                    instanceOf(StringCredentials.class)),
                    CredentialsProvider.lookupCredentials(StandardCredentials.class, project));
        }

        public ListBoxModel doFillNewVaultCredentialsIdItems(@AncestorInPath Project project) {
            return new StandardListBoxModel()
                .withEmptySelection()
                .withMatching(anyOf(
                    instanceOf(FileCredentials.class),
                    instanceOf(StringCredentials.class)),
                    CredentialsProvider.lookupCredentials(StandardCredentials.class, project));
        }

        public ListBoxModel doFillInstallationItems() {
            ListBoxModel model = new ListBoxModel();
            for (AnsibleInstallation tool : AnsibleInstallation.allInstallations()) {
                model.add(tool.getName());
            }
            return model;
        }
    }

    public static final class AnsibleVaultExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1;

        @Inject
        private transient AnsibleVaultStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient Launcher launcher;

        @StepContextParameter
        private transient Run<?,?> run;

        @StepContextParameter
        private transient FilePath ws;

        @StepContextParameter
        private transient EnvVars envVars;

        @StepContextParameter
        private transient Computer computer;

        @Override
        protected Void run() throws Exception {
            AnsibleVaultBuilder builder = new AnsibleVaultBuilder();
            builder.setAnsibleName(step.getInstallation());
            builder.setAction(step.getAction());
            builder.setVaultCredentialsId(step.getVaultCredentialsId());
            builder.setNewVaultCredentialsId(step.getNewVaultCredentialsId());
            builder.setContent(step.getContent());
            builder.setInput(step.getInput());
            builder.setOutput(step.getOutput());
            Node node;
            if (computer == null || (node = computer.getNode()) == null) {
                throw new AbortException("The ansible vault build step requires to be launched on a node");
            }
            builder.perform(run, node, ws, launcher, listener, envVars);
            return null;
        }
    }

}
