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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.inject.Inject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.*;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ansible.AnsibleInstallation;
import org.jenkinsci.plugins.ansible.AnsiblePlaybookBuilder;
import org.jenkinsci.plugins.ansible.ExtraVar;
import org.jenkinsci.plugins.ansible.Inventory;
import org.jenkinsci.plugins.ansible.InventoryPath;
import org.jenkinsci.plugins.ansible.InventoryContent;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * The Ansible playbook invocation step for the Jenkins workflow plugin.
 */
public class AnsiblePlaybookStep extends AbstractStepImpl {

    private final String playbook;
    private String inventory;
    private String inventoryContent;
    private boolean dynamicInventory = false;
    private String installation;
    private String credentialsId;
    private String vaultCredentialsId;
    private boolean become = false;
    private String becomeUser = "root";
    private boolean sudo = false;
    private String sudoUser = "root";
    private String limit = null;
    private String tags = null;
    private String skippedTags = null;
    private String startAtTask = null;
    private Map extraVars = null;
    private String extras = null;
    private boolean colorized = false;
    private int forks = 0;
    private boolean disableHostKeyChecking = false;
    @Deprecated
    @SuppressWarnings("unused")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private transient boolean hostKeyChecking = true;

    @DataBoundConstructor
    public AnsiblePlaybookStep(String playbook) {
        this.playbook = playbook;
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
    public void setBecome(boolean become) {
        this.become = become;
    }

    @DataBoundSetter
    public void setBecomeUser(String becomeUser) {
        this.becomeUser = Util.fixEmptyAndTrim(becomeUser);
    }

    @DataBoundSetter
    public void setSudo(boolean sudo) {
        this.sudo = sudo;
    }

    @DataBoundSetter
    public void setSudoUser(String sudoUser) {
        this.sudoUser = Util.fixEmptyAndTrim(sudoUser);
    }

    @DataBoundSetter
    public void setInstallation(String installation) {
        this.installation = Util.fixEmptyAndTrim(installation);
    }

    @DataBoundSetter
    public void setLimit(String limit) {
        this.limit = Util.fixEmptyAndTrim(limit);
    }

    @DataBoundSetter
    public void setTags(String tags) {
        this.tags = Util.fixEmptyAndTrim(tags);
    }

    @DataBoundSetter
    public void setSkippedTags(String skippedTags) {
        this.skippedTags = Util.fixEmptyAndTrim(skippedTags);
    }

    @DataBoundSetter
    public void setStartAtTask(String startAtTask) {
        this.startAtTask = Util.fixEmptyAndTrim(startAtTask);
    }

    @DataBoundSetter
    public void setExtraVars(Map extraVars) {
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
    public void setDisableHostKeyChecking(boolean disableHostKeyChecking) {
        this.disableHostKeyChecking = disableHostKeyChecking;
    }

    @DataBoundSetter
    @Deprecated
    public void setHostKeyChecking(boolean hostKeyChecking) {
    }

    public String getInstallation() {
        return installation;
    }

    public String getPlaybook() {
        return playbook;
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

    public boolean isBecome() {
        return become;
    }

    public String getBecomeUser() {
        return becomeUser;
    }

    public boolean isSudo() {
        return sudo;
    }

    public String getSudoUser() {
        return sudoUser;
    }

    public String getLimit() {
        return limit;
    }

    public String getTags() {
        return tags;
    }

    public String getSkippedTags() {
        return skippedTags;
    }

    public String getStartAtTask() {
        return startAtTask;
    }

    public Map<String, Object> getExtraVars() {
        return extraVars;
    }

    public String getExtras() {
        return extras;
    }

    public boolean isDisableHostKeyChecking() {
        return disableHostKeyChecking;
    }

    @Deprecated
    public boolean isHostKeyChecking() {
        return true;
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
            super(AnsiblePlaybookExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "ansiblePlaybook";
        }

        @Override
        public String getDisplayName() {
            return "Invoke an ansible playbook";
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item,
                                                     @QueryParameter String credentialsId) {

            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }

            return result.includeEmptyValue()
                    .withMatching(anyOf(
                            instanceOf(SSHUserPrivateKey.class),
                            instanceOf(UsernamePasswordCredentials.class)),
                            CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, item))
                    .includeCurrentValue(credentialsId);
        }

        public ListBoxModel doFillVaultCredentialsIdItems(@AncestorInPath Item item,
                                                          @QueryParameter String vaultCredentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(vaultCredentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(vaultCredentialsId);
                }
            }

            return result.includeEmptyValue()
                    .withMatching(anyOf(
                            instanceOf(FileCredentials.class),
                            instanceOf(StringCredentials.class)),
                            CredentialsProvider.lookupCredentials(StandardCredentials.class, item))
                    .includeCurrentValue(vaultCredentialsId);
        }

        public ListBoxModel doFillInstallationItems() {
            ListBoxModel model = new ListBoxModel();
            for (AnsibleInstallation tool : AnsibleInstallation.allInstallations()) {
                model.add(tool.getName());
            }
            return model;
        }
    }

    public static final class AnsiblePlaybookExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1;

        @Inject
        private transient AnsiblePlaybookStep step;

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

        private List<ExtraVar> convertExtraVars(Map<String, Object> extraVars) {
            if (extraVars == null) {
                return null;
            }
            List<ExtraVar> extraVarList = new ArrayList<ExtraVar>();
            for (Map.Entry<String, Object> entry: extraVars.entrySet()) {
                ExtraVar var = new ExtraVar();
                var.setKey(entry.getKey());
                Object o = entry.getValue();
                if (o instanceof Map) {
                    var.setValue(((Map)o).get("value").toString());
                    var.setHidden((Boolean)((Map)o).get("hidden"));
                } else {
                    var.setValue(o.toString());
                    var.setHidden(false);
                }
                extraVarList.add(var);
            }
            return extraVarList;
        }

        @Override
        protected Void run() throws Exception {
            Inventory inventory = null;
            if (StringUtils.isNotBlank(step.getInventory())) {
                inventory = new InventoryPath(step.getInventory());
            } else if (StringUtils.isNotBlank(step.getInventoryContent())) {
                inventory = new InventoryContent(
                        step.getInventoryContent(),
                        step.isDynamicInventory()
                );
            }
            AnsiblePlaybookBuilder builder = new AnsiblePlaybookBuilder(step.getPlaybook(), inventory);
            builder.setAnsibleName(step.getInstallation());
            builder.setBecome(step.isBecome());
            builder.setBecomeUser(step.getBecomeUser());
            builder.setSudo(step.isSudo());
            builder.setSudoUser(step.getSudoUser());
            builder.setCredentialsId(step.getCredentialsId(), true);
            builder.setVaultCredentialsId(step.getVaultCredentialsId());
            builder.setForks(step.getForks());
            builder.setLimit(step.getLimit());
            builder.setTags(step.getTags());
            builder.setStartAtTask(step.getStartAtTask());
            builder.setSkippedTags(step.getSkippedTags());
            builder.setExtraVars(convertExtraVars(step.extraVars));
            builder.setAdditionalParameters(step.getExtras());
            builder.setDisableHostKeyChecking(step.isDisableHostKeyChecking());
            builder.setUnbufferedOutput(true);
            builder.setColorizedOutput(step.isColorized());
            Node node;
            if (computer == null || (node = computer.getNode()) == null) {
                throw new AbortException("The ansible playbook build step requires to be launched on a node");
            }
            builder.perform(run, node, ws, launcher, listener, envVars);
            return null;
        }
    }

}
