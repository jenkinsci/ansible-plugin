package org.jenkinsci.plugins.ansible;

import static org.junit.Assert.assertEquals;

import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class CompatibilityTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @LocalData
    @Test
    public void test() throws Exception {
        FreeStyleProject p = (FreeStyleProject) r.jenkins.getItem("old");
        assertEquals(2, p.getBuilders().size());
        assertEquals(false, r.jenkins.getAdministrativeMonitor("OldData").isActivated());
    }
}
