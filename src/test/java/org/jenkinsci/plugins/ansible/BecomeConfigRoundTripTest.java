package org.jenkinsci.plugins.ansible;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BecomeConfigRoundTripTest {

    @Test
    void playbookBuilderKeepsBecome(JenkinsRule r) throws Exception {
        FreeStyleProject project = r.createFreeStyleProject();
        AnsiblePlaybookBuilder builder = new AnsiblePlaybookBuilder("playbook.yml", new InventoryPath("hosts"));
        builder.setBecome(true);
        builder.setBecomeUser("deploy");
        project.getBuildersList().add(builder);

        r.configRoundtrip(project);

        AnsiblePlaybookBuilder saved = project.getBuildersList().get(AnsiblePlaybookBuilder.class);
        assertTrue(saved.become);
        assertEquals("deploy", saved.becomeUser);
    }

    @Test
    void adHocCommandBuilderKeepsBecome(JenkinsRule r) throws Exception {
        FreeStyleProject project = r.createFreeStyleProject();
        AnsibleAdHocCommandBuilder builder =
                new AnsibleAdHocCommandBuilder("all", new InventoryPath("hosts"), "ping", "");
        builder.setBecome(true);
        builder.setBecomeUser("deploy");
        project.getBuildersList().add(builder);

        r.configRoundtrip(project);

        AnsibleAdHocCommandBuilder saved = project.getBuildersList().get(AnsibleAdHocCommandBuilder.class);
        assertTrue(saved.become);
        assertEquals("deploy", saved.becomeUser);
    }
}
