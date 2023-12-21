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

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.inject.Inject;
import hudson.*;
import hudson.model.Computer;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ansible.AnsibleAdHocCommandBuilder;
import org.jenkinsci.plugins.ansible.AnsibleInstallation;
import org.jenkinsci.plugins.ansible.ExtraVar;
import org.jenkinsci.plugins.ansible.Inventory;
import org.jenkinsci.plugins.ansible.InventoryContent;
import org.jenkinsci.plugins.ansible.InventoryDoNotSpecify;
import org.jenkinsci.plugins.ansible.InventoryPath;
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
 * The Ansible adhoc invocation step for the Jenkins workflow plugin.
 */
public class AnsibleAdhocStep extends AbstractStepImpl {

    private String hosts;
    private String module;
    private String moduleArguments;
    private String inventory;
    private String inventoryContent;
    private boolean dynamicInventory = false;
    private String installation;
    private String credentialsId;
    private String vaultCredentialsId;
    private String vaultTmpPath = null;
    private boolean become = false;
    private String becomeUser = "root";
    private List<ExtraVar> extraVars = null;
    private String extras = null;
    private boolean colorized = false;
    private int forks = 0;
    private boolean hostKeyChecking = false;

    @DataBoundConstructor
    public AnsibleAdhocStep(String hosts) {
        this.hosts = hosts;
    }

    @DataBoundSetter
    public void setModule(String module) {
        this.module = Util.fixEmptyAndTrim(module);
    }

    @DataBoundSetter
    public void setModuleArguments(String moduleArguments) {
        this.moduleArguments = Util.fixEmptyAndTrim(moduleArguments);
    }

    @DataBoundSetter
    public void setInventory(String inventory) {
        this.inventory = Util.fixEmptyAndTrim(inventory);
    }

    @DataBoundSetter
    public void setInventoryContent(String inventoryContent) {
        this.inventoryContent = Util.fixEmptyAndTrim(inventoryContent);
    }

    @DataBoundSetter
    public void setDynamicInventory(boolean dynamicInventory) {
        this.dynamicInventory = dynamicInventory;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmptyAndTrim(credentialsId);
    }

    @DataBoundSetter
    public void setVaultCredentialsId(String vaultCredentialsId) {
        this.vaultCredentialsId = Util.fixEmptyAndTrim(vaultCredentialsId);
    }

    @DataBoundSetter
    public void setVaultTmpPath(String vaultTmpPath) {
        this.vaultTmpPath = vaultTmpPath;
    }

    @DataBoundSetter
    public void setBecome(boolean become) {
        this.become = become;
    }

    @DataBoundSetter
    public void setBecomeUser(String becomeUser) {
        this.becomeUser = Util.fixEmptyAndTrim(becomeUser);
    }

    @DataBoundSetter
    public void setInstallation(String installation) {
        this.installation = Util.fixEmptyAndTrim(installation);
    }

    @DataBoundSetter
    public void setExtraVars(List<ExtraVar> extraVars) {
        this.extraVars = extraVars;
    }

    @DataBoundSetter
    public void setExtras(String extras) {
        this.extras = Util.fixEmptyAndTrim(extras);
    }

    @DataBoundSetter
    public void setColorized(boolean colorized) {
        this.colorized = colorized;
    }

    @DataBoundSetter
    public void setForks(int forks) {
        this.forks = forks;
    }

    @DataBoundSetter
    public void setHostKeyChecking(boolean hostKeyChecking) {
        this.hostKeyChecking = hostKeyChecking;
    }

    public String getInstallation() {
        return installation;
    }

    public String getHosts() {
        return hosts;
    }

    public String getModule() {
        return module;
    }

    public String getModuleArguments() {
        return moduleArguments;
    }

    public String getInventory() {
        return inventory;
    }

    public String getInventoryContent() {
        return inventoryContent;
    }

    public boolean isDynamicInventory() {
        return dynamicInventory;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getVaultCredentialsId() {
        return vaultCredentialsId;
    }

    public String getVaultTmpPath() {
        return vaultTmpPath;
    }

    public boolean isBecome() {
        return become;
    }

    public String getBecomeUser() {
        return becomeUser;
    }

    public List<ExtraVar> getExtraVars() {
        return extraVars;
    }

    public String getExtras() {
        return extras;
    }

    public boolean isHostKeyChecking() {
        return hostKeyChecking;
    }

    public int getForks() {
        return forks;
    }

    public boolean isColorized() {
        return colorized;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(AnsibleAdhocExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "ansibleAdhoc";
        }

        @Override
        public String getDisplayName() {
            return "Invoke an ansible adhoc command";
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Project project) {
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withMatching(
                            anyOf(instanceOf(SSHUserPrivateKey.class), instanceOf(UsernamePasswordCredentials.class)),
                            CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, project));
        }

        public ListBoxModel doFillVaultCredentialsIdItems(@AncestorInPath Project project) {
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withMatching(
                            anyOf(instanceOf(FileCredentials.class), instanceOf(StringCredentials.class)),
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

    public static final class AnsibleAdhocExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1;

        @Inject
        private transient AnsibleAdhocStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient Launcher launcher;

        @StepContextParameter
        private transient Run<?, ?> run;

        @StepContextParameter
        private transient FilePath ws;

        @StepContextParameter
        private transient EnvVars envVars;

        @StepContextParameter
        private transient Computer computer;

        @Override
        protected Void run() throws Exception {
            Inventory inventory = null;
            if (StringUtils.isNotBlank(step.getInventory())) {
                inventory = new InventoryPath(step.getInventory());
            } else if (StringUtils.isNotBlank(step.getInventoryContent())) {
                inventory = new InventoryContent(step.getInventoryContent(), step.isDynamicInventory());
            } else {
                inventory = new InventoryDoNotSpecify();
            }
            AnsibleAdHocCommandBuilder builder = new AnsibleAdHocCommandBuilder(
                    step.getHosts(), inventory, step.getModule(), step.getModuleArguments());
            builder.setAnsibleName(step.getInstallation());
            builder.setBecome(step.isBecome());
            builder.setBecomeUser(step.getBecomeUser());
            builder.setCredentialsId(step.getCredentialsId());
            builder.setVaultCredentialsId(step.getVaultCredentialsId());
            builder.setVaultTmpPath(step.getVaultTmpPath());
            builder.setForks(step.getForks());
            builder.setExtraVars(step.getExtraVars());
            builder.setAdditionalParameters(step.getExtras());
            builder.setHostKeyChecking(step.isHostKeyChecking());
            builder.setColorizedOutput(step.isColorized());
            builder.perform(run, ws, launcher, listener);
            return null;
        }
    }
}
