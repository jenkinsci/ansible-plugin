/*
 *     Copyright 2015-2016 Jean-Christophe Sirot <sirot@chelonix.com>
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
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

/**
 * Invoke the ansible-playbook command
 */
public class AnsiblePlaybookInvocation extends AbstractAnsibleInvocation<AnsiblePlaybookInvocation> {

    private String playbook;
    private String limit;
    private String tags;
    private String skippedTags;
    private String startAtTask;

    protected AnsiblePlaybookInvocation(String exe, AbstractBuild<?, ?> build, BuildListener listener, EnvVars envVars)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        this(exe, build, build.getWorkspace(), listener, envVars);
    }

    public AnsiblePlaybookInvocation(String exe, Run<?, ?> build, FilePath ws, TaskListener listener, EnvVars envVars)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        super(exe, build, ws, listener, envVars);
    }

    public AnsiblePlaybookInvocation setPlaybook(String playbook) {
        this.playbook = playbook;
        return this;
    }

    private ArgumentListBuilder appendPlaybook(ArgumentListBuilder args) {
        args.add(envVars.expand(playbook));
        return args;
    }

    public AnsiblePlaybookInvocation setLimit(String limit) {
        this.limit = limit;
        return this;
    }

    private ArgumentListBuilder appendLimit(ArgumentListBuilder args) {
        addOptionAndValue(args, "-l", limit);
        return args;
    }

    public AnsiblePlaybookInvocation setTags(String tags) {
        this.tags = tags;
        return this;
    }

    private ArgumentListBuilder appendTags(ArgumentListBuilder args) {
        addOptionAndValue(args, "-t", tags);
        return args;
    }

    public AnsiblePlaybookInvocation setSkippedTags(String skippedTags) {
        this.skippedTags = skippedTags;
        return this;
    }

    private ArgumentListBuilder appendSkippedTags(ArgumentListBuilder args) {
        addKeyValuePair(args, "--skip-tags", skippedTags);
        return args;
    }

    public AnsiblePlaybookInvocation setStartTask(String startAtTask) {
        this.startAtTask = startAtTask;
        return this;
    }

    private ArgumentListBuilder appendStartTask(ArgumentListBuilder args) {
        addKeyValuePair(args, "--start-at-task", startAtTask);
        return args;
    }

    @Override
    protected ArgumentListBuilder buildCommandLine() throws InterruptedException, AnsibleInvocationException, IOException {
        ArgumentListBuilder args = new ArgumentListBuilder();
        prependPasswordCredentials(args);
        appendExecutable(args);
        appendPlaybook(args);
        appendInventory(args);
        appendLimit(args);
        appendTags(args);
        appendSkippedTags(args);
        appendStartTask(args);
        appendBecome(args);
        appendSudo(args);
        appendForks(args);
        appendCredentials(args);
        appendVaultPasswordFile(args);
        appendExtraVars(args);
        appendAdditionalParameters(args);
        return args;
    }
}
