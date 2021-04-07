/*
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

import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import com.cloudbees.plugins.credentials.common.StandardCredentials;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

/**
 * Invoke the ansible-vault command
 * 
 * @author Michael Cresswell
 */
public class AnsibleVaultInvocation extends AbstractAnsibleInvocation<AnsibleVaultInvocation> {

    private String action;
    private String content;
    private String input;
    private String output;
    private StandardCredentials newVaultCredentials;
    
    private FilePath newVaultPassword = null;
    
    private FilePath ws = null;

    protected AnsibleVaultInvocation(String exe, AbstractBuild<?, ?> build, BuildListener listener, EnvVars envVars)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        this(exe, build, build.getWorkspace(), listener, envVars);
    }

    public AnsibleVaultInvocation(String exe, Run<?, ?> build, FilePath ws, TaskListener listener, EnvVars envVars)
            throws IOException, InterruptedException, AnsibleInvocationException
    {
        super(exe, build, ws, listener, envVars);
        this.ws = ws;
    }

    public AnsibleVaultInvocation setAction(String action) {
        this.action = action;
        return this;
    }
    
    private ArgumentListBuilder appendAction(ArgumentListBuilder args) throws AbortException {
		if (!"edit".equals(action) && !"create".equals(action) && !"view".equals(action)) {
			args.add(action);
		} else {
			throw new AbortException(action + ": ansible-plugin does not support interactive vault actions such as create, edit, or view.");
		}
        return args;
    }

    public AnsibleVaultInvocation setContent(String content) {
        this.content = content;
        return this;
    }
    
    private ArgumentListBuilder appendContent(ArgumentListBuilder args) {
        if (content != null && !content.isEmpty()) {
            args.addMasked(content);
        }
        return args;
    }

    public AnsibleVaultInvocation setInput(String input) {
        this.input = input;
        return this;
    }
    
    private ArgumentListBuilder appendInput(ArgumentListBuilder args) {
        if (input != null && !input.isEmpty()) {
            args.add(input);
        }
        return args;
    }

    public AnsibleVaultInvocation setNewVaultCredentials(StandardCredentials newVaultCredentials) {
        this.newVaultCredentials = newVaultCredentials;
        return this;
    }

    protected ArgumentListBuilder appendNewVaultPasswordFile(ArgumentListBuilder args)
            throws IOException, InterruptedException
    {
        if(newVaultCredentials != null){
            if (newVaultCredentials instanceof FileCredentials) {
                FileCredentials secretFile = (FileCredentials)newVaultCredentials;
                newVaultPassword = Utils.createVaultPasswordFile(newVaultPassword, ws, secretFile);
                args.add("--new-vault-password-file").add(newVaultPassword.getRemote().replace("%", "%%"));
            } else if (newVaultCredentials instanceof StringCredentials) {
                StringCredentials secretText = (StringCredentials)newVaultCredentials;
                newVaultPassword = Utils.createVaultPasswordFile(newVaultPassword, ws, secretText);
                args.add("--new-vault-password-file").add(newVaultPassword.getRemote().replace("%", "%%"));
            }
        }
        return args;
    }

    public AnsibleVaultInvocation setOutput(String output) {
        this.output = output;
        return this;
    }
    
    private ArgumentListBuilder appendOutput(ArgumentListBuilder args) {
        if (output != null && !output.isEmpty()) {
            args.add("--output").add(output);
        }
        return args;
    }

    @Override
    protected ArgumentListBuilder buildCommandLine() throws InterruptedException, AnsibleInvocationException, IOException {
        ArgumentListBuilder args = new ArgumentListBuilder();
        prependPasswordCredentials(args);
        appendExecutable(args);
        appendAction(args);
        appendVaultPasswordFile(args);
        appendNewVaultPasswordFile(args);
        appendOutput(args);
        appendContent(args);
        appendInput(args);
        return args;
    }
}
