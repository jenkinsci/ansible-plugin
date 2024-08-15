package org.jenkinsci.plugins.ansible;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy;
import hudson.slaves.DumbSlave;
import hudson.util.Secret;
import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers(disabledWithoutDocker = true)
public class PipelineTest {

    // Test and support only supported ansible version (https://endoflife.date/ansible-core)
    private static Stream<String> ansibleVersions() {
        return Stream.of("2.15.12", "2.16.10", "2.17.3");
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testMinimalPipeline(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/minimal.groovy"), StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(run1.getLog(), allOf(containsString("ansible-playbook /ansible/playbook.yml")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testMinimalCheckModePipeline(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/minimalCheckMode.groovy"),
                    StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(run1.getLog(), allOf(containsString("ansible-playbook /ansible/playbook.yml --check")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testExtraVarsHiddenString(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/extraVarsHiddenString.groovy"),
                    StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(run1.getLog(), allOf(containsString("ansible-playbook /ansible/playbook.yml -e ********")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testExtraVarsMap(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/extraVarsMap.groovy"), StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(
                    run1.getLog(),
                    allOf(
                            containsString("ansible-playbook /ansible/playbook.yml -e foo1=bar1"),
                            containsString("ansible-playbook /ansible/playbook.yml -e ********")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testExtraVarsBoolean(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/extraVarsBoolean.groovy"),
                    StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(
                    run1.getLog(),
                    allOf(containsString("ansible-playbook /ansible/playbook.yml -e ******** -e ********")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testExtraVarsNumeric(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/extraVarsNumeric.groovy"),
                    StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(run1.getLog(), allOf(containsString("ansible-playbook /ansible/playbook.yml -e ********")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testAnsiblePlaybookSshPass(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            UsernamePasswordCredentialsImpl usernamePassword = new UsernamePasswordCredentialsImpl(
                    CredentialsScope.GLOBAL,
                    "usernamePasswordCredentialsId",
                    "test username password",
                    "username",
                    "password");
            SystemCredentialsProvider.getInstance()
                    .getDomainCredentialsMap()
                    .put(Domain.global(), Collections.singletonList(usernamePassword));

            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/ansiblePlaybookSshPass.groovy"),
                    StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(
                    run1.getLog(),
                    allOf(containsString("sshpass ******** ansible-playbook /ansible/playbook.yml -u username -k")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testVaultCredentialsFile(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            FileCredentials vaultCredentials = new FileCredentialsImpl(
                    CredentialsScope.GLOBAL,
                    "vaultCredentialsFile",
                    "test username password",
                    "vault-pass.txt",
                    SecretBytes.fromString("text-secret"));
            SystemCredentialsProvider.getInstance()
                    .getDomainCredentialsMap()
                    .put(Domain.global(), Collections.singletonList(vaultCredentials));

            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/vaultCredentialsFile.groovy"),
                    StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            // assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(
                    run1.getLog(),
                    allOf(containsString("ansible-playbook /ansible/playbook.yml --vault-password-file ")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testVaultCredentialsString(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            StringCredentials vaultCredentials = new StringCredentialsImpl(
                    CredentialsScope.GLOBAL,
                    "vaultCredentialsString",
                    "test username password",
                    Secret.fromString("test-secret"));
            SystemCredentialsProvider.getInstance()
                    .getDomainCredentialsMap()
                    .put(Domain.global(), Collections.singletonList(vaultCredentials));

            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/vaultCredentialsString.groovy"),
                    StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(
                    run1.getLog(),
                    allOf(containsString("ansible-playbook /ansible/playbook.yml --vault-password-file")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testVaultCredentialsFileViaExtras(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            FileCredentials vaultCredentials = new FileCredentialsImpl(
                    CredentialsScope.GLOBAL,
                    "vaultCredentialsFileViaExtras",
                    "test username password",
                    "vault-pass.txt",
                    SecretBytes.fromString("text-secret"));
            SystemCredentialsProvider.getInstance()
                    .getDomainCredentialsMap()
                    .put(Domain.global(), Collections.singletonList(vaultCredentials));

            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/vaultCredentialsFileViaExtras.groovy"),
                    StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(
                    run1.getLog(),
                    allOf(containsString("ansible-playbook /ansible/playbook.yml --vault-password-file ")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testAdhocCommand(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/adhocCommand.groovy"), StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, true));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(
                    run1.getLog(),
                    allOf(containsString(
                            "ansible 127.0.0.1 -i /ansible/inventory.yml -a " + "\"" + "echo something" + "\"")));
        }
    }

    @ParameterizedTest
    @MethodSource("ansibleVersions")
    public void testVaultTmpPathString(String ansibleVersion, JenkinsRule jenkins) throws Exception {
        try (AnsibleAgent agent = setupAnsibleAgent(ansibleVersion, jenkins)) {
            FileCredentials vaultCredentials = new FileCredentialsImpl(
                    CredentialsScope.GLOBAL,
                    "vaultCredentialsFile",
                    "test username password",
                    "vault-pass.txt",
                    SecretBytes.fromString("text-secret"));
            SystemCredentialsProvider.getInstance()
                    .getDomainCredentialsMap()
                    .put(Domain.global(), Collections.singletonList(vaultCredentials));

            String pipeline = IOUtils.toString(
                    PipelineTest.class.getResourceAsStream("/pipelines/vaultTmpPath.groovy"), StandardCharsets.UTF_8);
            WorkflowJob workflowJob = jenkins.createProject(WorkflowJob.class);
            workflowJob.setDefinition(new CpsFlowDefinition(pipeline, false));
            WorkflowRun run1 = workflowJob.scheduleBuild2(0).waitForStart();
            jenkins.waitForCompletion(run1);
            // assertThat(run1.getResult(), equalTo(hudson.model.Result.SUCCESS));
            assertThat(
                    run1.getLog(),
                    allOf(containsString("ansible-playbook /ansible/playbook.yml --vault-password-file /ansible/tmp")));
        }
    }

    @SuppressWarnings("resource")
    private AnsibleAgent setupAnsibleAgent(String ansibleVersion, JenkinsRule jenkins) throws Exception {

        // Start container
        GenericContainer<?> container = new GenericContainer<>(
                        new ImageFromDockerfile("ansible-" + ansibleVersion, false)
                                .withFileFromClasspath("Dockerfile", "docker/Dockerfile")
                                .withBuildArg("ANSIBLE_CORE_VERSION", ansibleVersion))
                .withExposedPorts(22);
        container.start();

        // Setup credentials to connect to container
        StandardUsernameCredentials credentials = new UsernamePasswordCredentialsImpl(
                CredentialsScope.SYSTEM, "test-credentials", "", "root", "password");
        SystemCredentialsProvider.getInstance()
                .getDomainCredentialsMap()
                .put(Domain.global(), Collections.singletonList(credentials));

        // Create agent and connect to it
        final SSHLauncher launcher =
                new SSHLauncher(container.getHost(), container.getMappedPort(22), "test-credentials");
        launcher.setSshHostKeyVerificationStrategy(new NonVerifyingKeyVerificationStrategy());
        DumbSlave dumbAgent = new DumbSlave("test-node", "/home/jenkins/agent", launcher);
        dumbAgent.setNodeName("test-agent");
        dumbAgent.setNumExecutors(1);
        dumbAgent.setLabelString("test-agent");
        jenkins.jenkins.addNode(dumbAgent);
        jenkins.waitOnline(dumbAgent);

        return new AnsibleAgent(container, dumbAgent, jenkins);
    }

    public static class AnsibleAgent implements Closeable {

        private GenericContainer<?> container;
        private DumbSlave agent;
        private JenkinsRule rule;

        public AnsibleAgent(GenericContainer<?> container, DumbSlave agent, JenkinsRule rule) {
            this.container = container;
            this.agent = agent;
            this.rule = rule;
        }

        @Override
        public void close() {
            if (agent != null) {
                try {
                    rule.disconnectSlave(agent);
                } catch (Exception e) {
                    // ignore
                }
            }
            if (container != null) {
                container.stop();
            }
        }
    }
}
