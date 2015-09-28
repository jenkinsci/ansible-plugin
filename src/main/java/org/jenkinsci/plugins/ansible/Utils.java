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
import java.util.List;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.util.Secret;

class Utils
{
    /**
     * Copy the SSH private key into a temporary file.
     *
     * @param key the destination file
     * @param credentials the SSH key
     * @return the file
     * @throws IOException
     * @throws InterruptedException
     */
    static FilePath createSshKeyFile(FilePath key, FilePath workspace, SSHUserPrivateKey credentials) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        List<String> privateKeys = credentials.getPrivateKeys();
        for (String s : privateKeys) {
            sb.append(s);
        }
        key = workspace.createTextTempFile("ssh", ".key", sb.toString(), false);
        key.chmod(0400);
        return key;
    }

    static FilePath createSshAskPassFile(FilePath script, FilePath workspace, SSHUserPrivateKey credentials) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append("#! /bin/sh\n").append("/bin/echo \"" + Secret.toString(credentials.getPassphrase()) + "\"");
        script = workspace.createTextTempFile("ssh", ".sh", sb.toString(), false);
        script.chmod(0700);
        return script;
    }

    /**
     * Delete a temporary file. Print a warning in the log when deletion fails.
     *
     * @param tempFile the file to be removed
     * @param listener the build listener
     */
    static void deleteTempFile(FilePath tempFile, BuildListener listener) throws IOException, InterruptedException {
        if (tempFile != null) {
            try {
                tempFile.delete();
            } catch (IOException ioe) {
                if (tempFile.exists()) {
                    listener.getLogger().println("[WARNING] temp file " + tempFile + " not deleted");
                }
            }
        }
    }
}
