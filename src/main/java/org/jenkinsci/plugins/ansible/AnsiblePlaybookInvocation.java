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
 * Invoke the ansible-playbook command
 */
public class AnsiblePlaybookInvocation extends AbstractAnsibleInvocation<AnsiblePlaybookInvocation> {

    protected AnsiblePlaybookInvocation(String ansibleInstallation, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException, AnsibleNotFoundException
    {
        super(ansibleInstallation, AnsibleCommand.ANSIBLE_PLAYBOOK, build, launcher, listener);
    }

    public AnsiblePlaybookInvocation setHostPattern(String playbook) {
        args.add(envVars.expand(playbook));
        return this;
    }

    public AnsiblePlaybookInvocation setLimit(String limit) {
        if (StringUtils.isNotBlank(limit)) {
            args.add("-l").add(envVars.expand(limit));
        }
        return this;
    }

    public AnsiblePlaybookInvocation setTags(String tags) {
        if (StringUtils.isNotBlank(tags)) {
            args.add("-t").add(envVars.expand(tags));
        }
        return this;
    }

    public AnsiblePlaybookInvocation setSkippedTags(String skippedTags) {
        if (StringUtils.isNotBlank(skippedTags)) {
            args.addKeyValuePair("", "--skip-tags", envVars.expand(skippedTags), false);
        }
        return this;
    }

    public AnsiblePlaybookInvocation setStartTask(String startAtTask) {
        if (StringUtils.isNotBlank(startAtTask)) {
            args.addKeyValuePair("", "--start-at-task", envVars.expand(startAtTask), false);
        }
        return this;
    }
}
