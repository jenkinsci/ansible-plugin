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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ExtraVar extends AbstractDescribableImpl<ExtraVar> {

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public String key;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public transient String value;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public Secret secretValue;

    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE", justification = "Preserve API compatibility.")
    public boolean hidden = true;

    @DataBoundConstructor
    public ExtraVar() {}

    protected Object readResolve() {
        if (value != null) {
            this.setSecretValue(Secret.fromString(value));
        }
        return this;
    }

    @DataBoundSetter
    public void setKey(String key) {
        this.key = key;
    }

    @DataBoundSetter
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @DataBoundSetter
    public void setSecretValue(Secret value) {
        this.secretValue = value;
    }

    public String getKey() {
        return key;
    }

    public Secret getSecretValue() {
        return this.secretValue;
    }

    public boolean isHidden() {
        return hidden;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ExtraVar> {

        public String getDisplayName() {
            return "";
        }
    }
}
