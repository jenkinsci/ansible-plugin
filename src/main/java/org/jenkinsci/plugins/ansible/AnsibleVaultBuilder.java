/*
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

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * A builder which wraps an Ansible vault invocation.
 *
 * @author Michael Cresswell
 */
public class AnsibleVaultBuilder extends Builder implements SimpleBuildStep {

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String ansibleName = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String action = "encrypt_string";

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String vaultCredentialsId = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String newVaultCredentialsId = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String vaultTmpPath = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String content = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String input = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String output = null;

    @DataBoundConstructor
    public AnsibleVaultBuilder() {}

    @DataBoundSetter
    public void setAnsibleName(String ansibleName) {
        this.ansibleName = ansibleName;
    }

    @DataBoundSetter
    public void setAction(String action) {
        this.action = action;
    }

    @DataBoundSetter
    public void setVaultCredentialsId(String vaultCredentialsId) {
        this.vaultCredentialsId = vaultCredentialsId;
    }

    @DataBoundSetter
    public void setNewVaultCredentialsId(String newVaultCredentialsId) {
        this.newVaultCredentialsId = newVaultCredentialsId;
    }

    @DataBoundSetter
    public void setVaultTmpPath(String vaultTmpPath) {
        this.vaultTmpPath = vaultTmpPath;
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

    @Override
    public void perform(
            @NonNull Run<?, ?> run, @NonNull FilePath ws, @NonNull Launcher launcher, @NonNull TaskListener listener)
            throws InterruptedException, IOException {
        Computer computer = ws.toComputer();
        Node node;
        if (computer == null || (node = computer.getNode()) == null) {
            throw new AbortException("The ansible vault build step requires to be launched on a node");
        }
        perform(run, node, ws, launcher, listener, run.getEnvironment(listener));
    }

    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull Node node,
            @NonNull FilePath ws,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener,
            EnvVars envVars)
            throws InterruptedException, IOException {
        try {
            CLIRunner runner = new CLIRunner(run, ws, launcher, listener);
            Computer computer = node.toComputer();
            String exe = AnsibleInstallation.getExecutable(
                    ansibleName, AnsibleCommand.ANSIBLE_VAULT, node, listener, envVars);
            AnsibleVaultInvocation invocation = new AnsibleVaultInvocation(exe, run, ws, listener, envVars);
            invocation.setAction(action);
            invocation.setVaultCredentials(
                    StringUtils.isNotBlank(vaultCredentialsId)
                            ? CredentialsProvider.findCredentialById(
                                    run.getEnvironment(listener).expand(vaultCredentialsId),
                                    StandardCredentials.class,
                                    run)
                            : null);
            invocation.setNewVaultCredentials(
                    StringUtils.isNotBlank(newVaultCredentialsId)
                            ? CredentialsProvider.findCredentialById(
                                    run.getEnvironment(listener).expand(newVaultCredentialsId),
                                    StandardCredentials.class,
                                    run)
                            : null);
            invocation.setVaultTmpPath(
                    StringUtils.isNotBlank(vaultTmpPath)
                            ? new FilePath(computer.getChannel(), new File(vaultTmpPath).getAbsolutePath())
                            : null);
            invocation.setContent(content);
            invocation.setInput(input);
            invocation.setOutput(output);
            if (!invocation.execute(runner)) {
                throw new AbortException("Ansible vault execution failed");
            }
        } catch (IOException ioe) {
            Util.displayIOException(ioe, listener);
            ioe.printStackTrace(listener.fatalError("command execution failed"));
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
    public static final class DescriptorImpl extends AbstractAnsibleBuilderDescriptor {
        public DescriptorImpl() {
            super("Invoke Ansible Vault");
        }

        public FormValidation doCheckVaultCredentialsId(@QueryParameter String vaultCredentialsId) {
            return checkNotNullOrEmpty(vaultCredentialsId, "Vault credentials must not be empty");
        }
    }
}
