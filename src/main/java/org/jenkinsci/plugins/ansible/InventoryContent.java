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

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Inline content for Ansible inventory. Inventory may be dynamic or not.
 */
public class InventoryContent extends Inventory
{
    public final String content;
    public final boolean dynamic;

    private transient FilePath inventory = null;

    @DataBoundConstructor
    public InventoryContent(String content, boolean dynamic) {
        this.content = content;
        this.dynamic = dynamic;
    }

    @Override
    protected InventoryHandler getHandler()
    {
        return new InventoryHandler() {
            public void addArgument(ArgumentListBuilder args, FilePath workspace, EnvVars envVars, TaskListener listener)
                    throws InterruptedException, IOException
            {
                inventory = createInventoryFile(inventory, workspace, envVars.expand(content));
                args.add("-i").add(inventory);
            }

            public void tearDown(TaskListener listener) throws InterruptedException, IOException {
                Utils.deleteTempFile(inventory, listener);
            }

            private FilePath createInventoryFile(FilePath inventory, FilePath workspace, String content) throws IOException, InterruptedException {
                inventory = workspace.createTextTempFile("inventory", ".ini", content, false);
                inventory.chmod(dynamic ? 0500 :  0400);
                return inventory;
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends InventoryDescriptor {

        @Override
        public String getDisplayName() {
            return "Inline content";
        }
    }

}
