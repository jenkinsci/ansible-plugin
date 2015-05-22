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
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Invoke the ansible command
 */
public class AnsibleAdHocCommandInvocation extends AbstractAnsibleInvocation<AnsibleAdHocCommandInvocation> {

    private String module;
    private String hostPattern;
    private String command;

    protected AnsibleAdHocCommandInvocation(String ansibleInstallation, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        super(ansibleInstallation, AnsibleCommand.ANSIBLE, build, launcher, listener);
    }

    public AnsibleAdHocCommandInvocation setHostPattern(String hostPattern) {
        this.hostPattern = hostPattern;
        return this;
    }

    private ArgumentListBuilder appendHostPattern(ArgumentListBuilder args) {
        args.add(envVars.expand(hostPattern));
        return args;
    }

    public AnsibleAdHocCommandInvocation setModule(String module) {
        this.module = module;
        return this;
    }

    private ArgumentListBuilder appendHModule(ArgumentListBuilder args) {
        if (StringUtils.isNotBlank(module)) {
            args.add("-m").add(module);
        }
        return args;
    }

    public AnsibleAdHocCommandInvocation setModuleCommand(String command) {
        this.command = command;
        return this;
    }

    public ArgumentListBuilder appendModuleCommand(ArgumentListBuilder args) {
        if (StringUtils.isNotBlank(command)) {
            args.add("-a").add(command);
        }
        return args;
    }

    @Override
    protected ArgumentListBuilder buildCommandLine()
            throws InterruptedException, AnsibleInvocationException, IOException
    {
        ArgumentListBuilder args = new ArgumentListBuilder();
        prependPasswordCredentials(args);
        appendExecutable(args);
        appendHostPattern(args);
        appendInventory(args);
        appendHModule(args);
        appendModuleCommand(args);
        appendSudo(args);
        appendForks(args);
        appendCredentials(args);
        appendAdditionalParameters(args);
        return args;
    }

}
