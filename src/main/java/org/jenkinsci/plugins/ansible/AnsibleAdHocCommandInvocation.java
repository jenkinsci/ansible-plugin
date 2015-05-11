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

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.apache.commons.lang.StringUtils;

/**
 * Invoke the ansible command
 */
public class AnsibleAdHocCommandInvocation extends AbstractAnsibleInvocation<AnsibleAdHocCommandInvocation> {

    protected AnsibleAdHocCommandInvocation(String ansibleInstallation, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        super(ansibleInstallation, AnsibleCommand.ANSIBLE, build, launcher, listener);
    }

    public AnsibleAdHocCommandInvocation setHostPattern(String hostPattern) {
        args.add(envVars.expand(hostPattern));
        return this;
    }

    public AnsibleAdHocCommandInvocation setModule(String module) {
        if (StringUtils.isNotBlank(module)) {
            args.add("-m").add(module);
        }
        return this;
    }

    public AnsibleAdHocCommandInvocation setModuleCommand(String command) {
        if (StringUtils.isNotBlank(command)) {
            args.add("-a").add(command);
        }
        return this;
    }
}
