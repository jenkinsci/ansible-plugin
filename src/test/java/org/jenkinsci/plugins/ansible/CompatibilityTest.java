package org.jenkinsci.plugins.ansible;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

@WithJenkins
class CompatibilityTest {

    @LocalData
    @Test
    void test(JenkinsRule r) {
        FreeStyleProject p = (FreeStyleProject) r.jenkins.getItem("old");
        assertEquals(2, p.getBuilders().size());
        assertFalse(r.jenkins.getAdministrativeMonitor("OldData").isActivated());
    }
}
