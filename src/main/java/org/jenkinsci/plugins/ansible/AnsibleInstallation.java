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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * {@code ToolInstallation} for Ansible
 */
public class AnsibleInstallation extends ToolInstallation
        implements EnvironmentSpecific<AnsibleInstallation>, NodeSpecific<AnsibleInstallation>, Serializable {

    @DataBoundConstructor
    public AnsibleInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    public AnsibleInstallation forEnvironment(EnvVars environment) {
        return new AnsibleInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    public AnsibleInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new AnsibleInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

    public String getExecutable(final AnsibleCommand command, Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new Callable<String, IOException>() {
            public String call() throws IOException {
                File exe = new File(getHome(), command.getName());
                if (exe.exists()) {
                    return exe.getPath();
                }
                return null;
            }
        });
    }

    public static AnsibleInstallation[] allInstallations() {
        Jenkins jenkins = Jenkins.getInstance();
        AnsibleInstallation.DescriptorImpl ansibleDescriptor = Jenkins.getInstance().getDescriptorByType(AnsibleInstallation.DescriptorImpl.class);
        return ansibleDescriptor.getInstallations();
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<AnsibleInstallation> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            super.configure(req, json);
            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Ansible";
        }
    }
}
