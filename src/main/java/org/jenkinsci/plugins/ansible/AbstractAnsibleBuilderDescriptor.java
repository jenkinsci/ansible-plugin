package org.jenkinsci.plugins.ansible;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf;

import java.util.List;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ansible.Inventory.InventoryDescriptor;
import org.kohsuke.stapler.AncestorInPath;

/**
 * Common descriptor for Ansible build steps
 */
public abstract class AbstractAnsibleBuilderDescriptor extends BuildStepDescriptor<Builder>
{
    private final String displayName;

    protected AbstractAnsibleBuilderDescriptor(String displayName) {
        this.displayName = displayName;
        load();
    }

    protected FormValidation checkNotNullOrEmpty(String parameter, String errorMessage) {
        if (StringUtils.isNotBlank(parameter)) {
            return FormValidation.ok();
        } else {
            return FormValidation.error(errorMessage);
        }
    }

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Project project) {
        return new StandardListBoxModel()
                .withEmptySelection()
                .withMatching(anyOf(
                            instanceOf(SSHUserPrivateKey.class),
                            instanceOf(UsernamePasswordCredentials.class)),
                        CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, project));
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

    public List<InventoryDescriptor> getInventories() {
        return Jenkins.getActiveInstance().getDescriptorList(Inventory.class);
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> klass) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public AnsibleInstallation[] getInstallations() {
        return AnsibleInstallation.allInstallations();
    }
}
