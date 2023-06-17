package org.jenkinsci.plugins.ansible;

import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Label;
import hudson.slaves.DumbSlave;
import hudson.util.Secret;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PipelineTest {

    @ClassRule
    public static JenkinsRule jenkins = new JenkinsRule();

    private static DumbSlave agent;

    @BeforeClass
    public static void startAgent() throws Exception {
        agent = jenkins.createSlave(Label.get("test-agent"));
    }

    @Test
    public void testMinimalPipeline() throws Exception {
        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/minimal.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible-playbook playbook.yml")
        ));
    }

    @Test
    public void testExtraVarsHiddenString() throws Exception {
        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/extraVarsHiddenString.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible-playbook playbook.yml -e ********")
        ));
    }

    @Test
    public void testExtraVarsMap() throws Exception {
        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/extraVarsMap.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible-playbook playbook.yml -e foo1=bar1"),
                containsString("ansible-playbook playbook.yml -e ********")
        ));
    }

    @Test
    public void testExtraVarsBoolean() throws Exception {
        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/extraVarsBoolean.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible-playbook playbook.yml -e ******** -e ********")
        ));
    }

    @Test
    public void testExtraVarsNumeric() throws Exception {
        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/extraVarsNumeric.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible-playbook playbook.yml -e ********")
        ));
    }

    @Test
    public void testAnsiblePlaybookSshPass() throws Exception {

        UsernamePasswordCredentialsImpl usernamePassword = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "usernamePasswordCredentialsId", "test username password", "username", "password");
        CredentialsStore store = CredentialsProvider.lookupStores(jenkins.jenkins).iterator().next();
        store.addCredentials(Domain.global(), usernamePassword);


        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/ansiblePlaybookSshPass.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("sshpass ******** ansible-playbook playbook.yml -u username -k")
        ));
    }

    @Test
    public void testVaultCredentialsFile() throws Exception {
        
        FileCredentials vaultCredentials = new FileCredentialsImpl(CredentialsScope.GLOBAL, "vaultCredentialsFile", "test username password", "vault-pass.txt", SecretBytes.fromString("text-secret"));
        CredentialsStore store = CredentialsProvider.lookupStores(jenkins.jenkins).iterator().next();
        store.addCredentials(Domain.global(), vaultCredentials);

        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/vaultCredentialsFile.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible-playbook playbook.yml --vault-password-file ")
        ));
    }

    @Test
    public void testVaultCredentialsString() throws Exception {
        
        StringCredentials vaultCredentials = new StringCredentialsImpl(CredentialsScope.GLOBAL, "vaultCredentialsString", "test username password", Secret.fromString("test-secret"));
        CredentialsStore store = CredentialsProvider.lookupStores(jenkins.jenkins).iterator().next();
        store.addCredentials(Domain.global(), vaultCredentials);

        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/vaultCredentialsString.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible-playbook playbook.yml --vault-password-file ")
        ));
    }

    @Test
    public void testVaultCredentialsFileViaExtras() throws Exception {
        
        FileCredentials vaultCredentials = new FileCredentialsImpl(CredentialsScope.GLOBAL, "vaultCredentialsFileViaExtras", "test username password", "vault-pass.txt", SecretBytes.fromString("text-secret"));
        CredentialsStore store = CredentialsProvider.lookupStores(jenkins.jenkins).iterator().next();
        store.addCredentials(Domain.global(), vaultCredentials);

        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/vaultCredentialsFileViaExtras.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible-playbook playbook.yml --vault-password-file ")
        ));
    }

    @Test
    public void testAdhocCommand() throws Exception {
        String pipeline = IOUtils.toString(PipelineTest.class.getResourceAsStream("/pipelines/adhocCommand.groovy"), StandardCharsets.UTF_8);
        WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
        workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
        WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run1);
        assertThat(run1.getLog(), allOf(
                containsString("ansible 127.0.0.1 -i inventory -a " + "\"" + "echo something" + "\"")
        ));
    }
}
