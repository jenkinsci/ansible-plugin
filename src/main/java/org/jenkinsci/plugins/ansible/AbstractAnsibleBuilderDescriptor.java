package org.jenkinsci.plugins.ansible;

import java.util.List;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
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

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Project project) {
        return new StandardListBoxModel()
                .withEmptySelection()
                .withMatching(CredentialsMatchers.instanceOf(SSHUserPrivateKey.class),
                        CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, project));
    }

    public List<InventoryDescriptor> getInventories() {
        return Jenkins.getInstance().getDescriptorList(Inventory.class);
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
