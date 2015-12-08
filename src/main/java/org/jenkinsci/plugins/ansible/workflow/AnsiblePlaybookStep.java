package org.jenkinsci.plugins.ansible.workflow;

import com.google.inject.Inject;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ansible.AnsiblePlaybookBuilder;
import org.jenkinsci.plugins.ansible.Inventory;
import org.jenkinsci.plugins.ansible.InventoryPath;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * The Ansible playbook invocation step for the Jenkins workflow plugin.
 */
public class AnsiblePlaybookStep extends AbstractStepImpl {

    private final String playbook;
    private String inventory;
    private String installation;
    private String credentialsId;
    private boolean sudo = false;
    private String sudoUser = "root";
    private String limit = null;
    private String tags = null;
    private String skippedTags = null;
    private String startAtTask = null;
    private String extras = null;

    @DataBoundConstructor
    public AnsiblePlaybookStep(String playbook) {
        this.playbook = playbook;
    }

    @DataBoundSetter
    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
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
    public void setInstallation(String installation) {
        this.installation = installation;
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
    public void setExtras(String extras) {
        this.extras = extras;
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

    public String getCredentialsId() {
        return credentialsId;
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

    public String getExtras() {
        return extras;
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
    }

    public static final class AnsiblePlaybookExecution extends AbstractSynchronousStepExecution<Void> {

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

        @Override
        protected Void run() throws Exception {
            Inventory inventory = StringUtils.isNotBlank(step.getInventory()) ? new InventoryPath(step.getInventory()) : null;
            AnsiblePlaybookBuilder builder = new AnsiblePlaybookBuilder(step.getPlaybook(), inventory);
            builder.setAnsibleName(step.getInstallation());
            builder.setSudo(step.isSudo());
            builder.setSudoUser(step.getSudoUser());
            builder.setCredentialsId(step.getCredentialsId(), true);
            builder.setForks(5);
            builder.setLimit(step.getLimit());
            builder.setTags(step.getTags());
            builder.setStartAtTask(step.getStartAtTask());
            builder.setSkippedTags(step.getSkippedTags());
            builder.setAdditionalParameters(step.getExtras());
            builder.setHostKeyChecking(false);
            builder.setUnbufferedOutput(true);
            builder.setColorizedOutput(false);
            builder.perform(run, computer.getNode(), ws, launcher, listener, envVars);
            return null;
        }
    }

}
