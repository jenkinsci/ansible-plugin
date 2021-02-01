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

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Path to a file containing an Ansible inventory
 */
public class InventoryPath extends Inventory
{
    public final String path;

    @DataBoundConstructor
    public InventoryPath(String path) {
        this.path = path;
    }

    @Override
    protected InventoryHandler getHandler()
    {
        return new InventoryHandler()
        {
            public void addArgument(ArgumentListBuilder args, FilePath workspace, EnvVars envVars, TaskListener listener)
            {
                String expandedPath = envVars.expand(InventoryPath.this.path);
                if (StringUtils.isNotEmpty(expandedPath)) {
                    args.add("-i").add(expandedPath);
                }
            }

            public void tearDown(TaskListener listener)
            {
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends InventoryDescriptor {

        @Override
        public String getDisplayName() {
            return "File or host list";
        }
    }

}
