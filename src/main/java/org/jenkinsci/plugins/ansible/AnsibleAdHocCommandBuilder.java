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

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * A builder which wraps an Ansible Ad-Hoc command invocation.
 */
public class AnsibleAdHocCommandBuilder extends Builder implements SimpleBuildStep {

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String ansibleName;

    // SSH settings
    /**
     * The id of the credentials to use.
     */
    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String credentialsId = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String vaultCredentialsId = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String vaultTmpPath = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public final String hostPattern;

    /**
     * Path to the inventory file.
     */
    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public final Inventory inventory;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public final String module;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public final String command;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public boolean become = false;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String becomeUser = "root";

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public boolean sudo = false;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String sudoUser = "root";

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public int forks = 0;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public boolean unbufferedOutput = true;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public boolean colorizedOutput = false;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public boolean disableHostKeyChecking = false;

    @Deprecated
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", "PA_PUBLIC_PRIMITIVE_ATTRIBUTE"})
    public transient boolean hostKeyChecking = true;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String additionalParameters = null;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public List<ExtraVar> extraVars;

    @Deprecated
    public AnsibleAdHocCommandBuilder(
            String ansibleName,
            String hostPattern,
            Inventory inventory,
            String module,
            String command,
            String credentialsId,
            boolean sudo,
            String sudoUser,
            int forks,
            boolean unbufferedOutput,
            boolean colorizedOutput,
            boolean hostKeyChecking,
            String additionalParameters) {
        this.ansibleName = ansibleName;
        this.hostPattern = hostPattern;
        this.inventory = inventory;
        this.module = module;
        this.command = command;
        this.credentialsId = credentialsId;
        this.sudo = sudo;
        this.sudoUser = sudoUser;
        this.forks = forks;
        this.unbufferedOutput = unbufferedOutput;
        this.colorizedOutput = colorizedOutput;
        // ignored because of SECURITY-630
        // this.hostKeyChecking = hostKeyChecking;
        this.additionalParameters = additionalParameters;
    }

    @DataBoundConstructor
    public AnsibleAdHocCommandBuilder(String hostPattern, Inventory inventory, String module, String command) {
        this.hostPattern = hostPattern;
        this.inventory = inventory;
        this.module = module;
        this.command = command;
    }

    @DataBoundSetter
    public void setAnsibleName(String ansibleName) {
        this.ansibleName = ansibleName;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @DataBoundSetter
    public void setVaultCredentialsId(String vaultCredentialsId) {
        this.vaultCredentialsId = vaultCredentialsId;
    }

    @DataBoundSetter
    public void setVaultTmpPath(String vaultTmpPath) {
        this.vaultTmpPath = vaultTmpPath;
    }

    public void setBecome(boolean become) {
        this.become = become;
    }

    @DataBoundSetter
    public void setBecomeUser(String becomeUser) {
        this.becomeUser = becomeUser;
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
    public void setDisableHostKeyChecking(boolean disableHostKeyChecking) {
        this.disableHostKeyChecking = disableHostKeyChecking;
    }

    @DataBoundSetter
    @Deprecated
    public void setHostKeyChecking(boolean hostKeyChecking) {
        this.hostKeyChecking = true;
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
    public void perform(
            @Nonnull Run<?, ?> run, @Nonnull FilePath ws, @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws InterruptedException, IOException {
        try {
            CLIRunner runner = new CLIRunner(run, ws, launcher, listener);
            Computer computer = ws.toComputer();
            if (computer == null) {
                throw new AbortException("The ansible ad-hoc command build step requires to be launched on a node");
            }
            EnvVars envVars = run.getEnvironment(listener);
            String exe = AnsibleInstallation.getExecutable(
                    ansibleName, AnsibleCommand.ANSIBLE, computer.getNode(), listener, envVars);
            AnsibleAdHocCommandInvocation invocation =
                    new AnsibleAdHocCommandInvocation(exe, run, ws, listener, envVars);
            invocation.setHostPattern(hostPattern);
            invocation.setInventory(inventory);
            invocation.setModule(module);
            invocation.setModuleCommand(command);
            invocation.setBecome(become, becomeUser);
            invocation.setSudo(sudo, sudoUser);
            invocation.setForks(forks);
            invocation.setCredentials(
                    StringUtils.isNotBlank(credentialsId)
                            ? CredentialsProvider.findCredentialById(
                                    credentialsId, StandardUsernameCredentials.class, run)
                            : null);
            invocation.setVaultCredentials(
                    StringUtils.isNotBlank(vaultCredentialsId)
                            ? CredentialsProvider.findCredentialById(vaultCredentialsId, StandardCredentials.class, run)
                            : null);
            invocation.setVaultTmpPath(
                    StringUtils.isNotBlank(vaultTmpPath)
                            ? new FilePath(computer.getChannel(), new File(vaultTmpPath).getAbsolutePath())
                            : null);
            invocation.setExtraVars(extraVars);
            invocation.setAdditionalParameters(additionalParameters);
            invocation.setDisableHostKeyCheck(disableHostKeyChecking);
            invocation.setUnbufferedOutput(unbufferedOutput);
            invocation.setColorizedOutput(colorizedOutput);
            if (!invocation.execute(runner)) {
                throw new AbortException("Ansible Ad-Hoc command execution failed");
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

    @Extension
    public static final class DescriptorImpl extends AbstractAnsibleBuilderDescriptor {

        public DescriptorImpl() {
            super("Invoke Ansible Ad-Hoc Command");
        }

        public FormValidation doCheckHostPattern(@QueryParameter String hostPattern) {
            return checkNotNullOrEmpty(hostPattern, "Host pattern must not be empty");
        }
    }
}
