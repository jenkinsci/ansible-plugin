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
import hudson.FilePath;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import jenkins.model.Jenkins;

/**
 * Common Ansible inventory.
 */
public abstract class Inventory implements Describable<Inventory>
{
    /**
     * @see hudson.model.Describable#getDescriptor()
     */
    @SuppressWarnings("unchecked")
    public Descriptor<Inventory> getDescriptor() {
        return Jenkins.getActiveInstance().getDescriptorOrDie(getClass());
    }

    protected abstract InventoryHandler getHandler();

    public void addArgument(ArgumentListBuilder args, FilePath workspace, EnvVars envVars, TaskListener listener)
            throws InterruptedException, IOException
    {
        getHandler().addArgument(args, workspace, envVars, listener);
    }

    public void tearDown(TaskListener listener) throws InterruptedException, IOException {
        getHandler().tearDown(listener);
    }

    public abstract static class InventoryDescriptor extends Descriptor<Inventory> { }

    protected static interface InventoryHandler {

        void addArgument(ArgumentListBuilder args, FilePath workspace, EnvVars envVars, TaskListener listener)
                throws InterruptedException, IOException;

        void tearDown(TaskListener listener) throws InterruptedException, IOException;
    }

}
