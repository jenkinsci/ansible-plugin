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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
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

    public static String getExecutable(String name, AnsibleCommand command, Node node, TaskListener listener, EnvVars env) throws IOException, InterruptedException {
        if (name != null) {
            Jenkins j = Jenkins.getInstance();
            if (j != null) {
                for (AnsibleInstallation tool : j.getDescriptorByType(DescriptorImpl.class).getInstallations()) {
                    if (tool.getName().equals(name)) {
                        if (node != null) {
                            tool = tool.forNode(node, listener);
                        }
                        if (env != null) {
                            tool = tool.forEnvironment(env);
                        }
                        String home = Util.fixEmpty(tool.getHome());
                        if (home != null) {
                            if (node != null) {
                                FilePath homePath = node.createPath(home);
                                if (homePath != null) {
                                    return homePath.child(command.getName()).getRemote();
                                }
                            }
                            return home + "/" + command.getName();
                        }
                    }
                }
            }
        }
        return command.getName();
    }

    public static AnsibleInstallation[] allInstallations() {
        AnsibleInstallation.DescriptorImpl ansibleDescriptor = Jenkins.getActiveInstance().getDescriptorByType(AnsibleInstallation.DescriptorImpl.class);
        return ansibleDescriptor.getInstallations();
    }

    public static AnsibleInstallation getInstallation(String ansibleInstallation) throws IOException {
        AnsibleInstallation[] installations = allInstallations();
        if (ansibleInstallation == null) {
            if (installations.length == 0) {
                throw new IOException("Ansible not found");
            }
            return installations[0];
        } else {
            for (AnsibleInstallation installation: installations) {
                if (ansibleInstallation.equals(installation.getName())) {
                    return installation;
                }
            }
        }
        throw new IOException("Ansible not found");
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        String home = Util.fixEmpty(getHome());
        if (home != null) {
            env.put("PATH+ANSIBLE", home);
        }
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
