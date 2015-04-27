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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import hudson.FilePath;
import hudson.model.BuildListener;

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
    static File createSshKeyFile(File key, SSHUserPrivateKey credentials) throws IOException, InterruptedException {
        key = File.createTempFile("ssh", "key");
        PrintWriter w = new PrintWriter(key);
        List<String> privateKeys = credentials.getPrivateKeys();
        for (String s : privateKeys) {
            w.println(s);
        }
        w.close();
        new FilePath(key).chmod(0400);
        return key;
    }

    /**
     * Delete a temporary file. Print a warning in the log when deletion fails.
     *
     * @param tempFile the file to be removed
     * @param listener the build listener
     */
    static void deleteTempFile(File tempFile, BuildListener listener) {
        if (tempFile != null) {
            if (!tempFile.delete()) {
                if (tempFile.exists()) {
                    listener.getLogger().println("[WARNING] temp file " + tempFile + " not deleted");
                }
            }
        }
    }
}
