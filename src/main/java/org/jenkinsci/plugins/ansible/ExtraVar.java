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

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ExtraVar extends AbstractDescribableImpl<ExtraVar> {

    public String key;

    public String value;

    public boolean hidden;

    @DataBoundConstructor
    public ExtraVar() {
    }

    @DataBoundSetter
    public void setKey(String key) {
        this.key = key;
    }

    @DataBoundSetter
    public void setValue(String value) {
        this.value = value;
    }

    @DataBoundSetter
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isHidden() {
        return hidden;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ExtraVar> {

        public String getDisplayName() { return ""; }
    }

}
