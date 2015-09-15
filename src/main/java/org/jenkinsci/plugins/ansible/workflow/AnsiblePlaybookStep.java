package org.jenkinsci.plugins.ansible.workflow;

import javax.annotation.Nonnull;

import java.io.IOException;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.google.inject.Inject;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ansible.AnsibleCommand;
import org.jenkinsci.plugins.ansible.AnsibleInstallation;
import org.jenkinsci.plugins.ansible.AnsibleInvocationException;
import org.jenkinsci.plugins.ansible.AnsiblePlaybookInvocation;
import org.jenkinsci.plugins.ansible.CLIRunner;
import org.jenkinsci.plugins.ansible.InventoryPath;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Created with IntelliJ IDEA.
 * User: jcsirot
 * Date: 14/09/15
 * Time: 16:22
 * To change this template use File | Settings | File Templates.
 */
public class AnsiblePlaybookStep extends AbstractStepImpl {

    private final String playbook;
    private String inventory;
    private String credentialId;
    private boolean sudo = false;
    private String sudoUser = "root";

    @DataBoundConstructor

    public AnsiblePlaybookStep(String playbook) {
        this.playbook = playbook;
    }

    @DataBoundSetter
    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    @DataBoundSetter
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    @DataBoundSetter
    public void setSudo(boolean sudo) {
        this.sudo = sudo;
    }

    @DataBoundSetter
    public void setSudoUser(String sudoUser) {
        this.sudoUser = sudoUser;
    }

    public String getPlaybook() {
        return playbook;
    }

    public String getInventory() {
        return inventory;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public boolean isSudo() {
        return sudo;
    }

    public String getSudoUser() {
        return sudoUser;
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
            try {
                CLIRunner runner = new CLIRunner(run, ws, launcher, listener);
                String exe = AnsibleInstallation.getExecutable(null, AnsibleCommand.ANSIBLE_PLAYBOOK, computer.getNode(), listener, envVars);
                AnsiblePlaybookInvocation invocation = new AnsiblePlaybookInvocation(exe, run, ws, listener);
                invocation.setPlaybook(step.getPlaybook());
                if (StringUtils.isNotBlank(step.getInventory())) {
                    invocation.setInventory(new InventoryPath(step.getInventory()));
                }
                invocation.setSudo(step.isSudo(), step.getSudoUser());
                invocation.setCredentials(StringUtils.isNotBlank(step.getCredentialId()) ?
                        CredentialsProvider.findCredentialById(step.getCredentialId(), StandardUsernameCredentials.class, run) :
                        null);
                invocation.setForks(5);
                invocation.setHostKeyCheck(false);
                invocation.setUnbufferedOutput(true);
                invocation.setColorizedOutput(false);
                if (!invocation.execute(runner)) {
                    throw new AbortException("Ansible playbook execution failure");
                }
            } catch (IOException ioe) {
                Util.displayIOException(ioe, listener);
                throw new AbortException(ioe.getMessage());
            } catch (AnsibleInvocationException aie) {
                throw new AbortException(aie.getMessage());
            }
            return null;
        }
    }

}
