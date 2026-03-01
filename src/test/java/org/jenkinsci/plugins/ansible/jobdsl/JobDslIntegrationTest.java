package org.jenkinsci.plugins.ansible.jobdsl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.io.Resources;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.util.Secret;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import javaposse.jobdsl.plugin.LookupStrategy;
import javaposse.jobdsl.plugin.RemovedJobAction;
import javaposse.jobdsl.plugin.RemovedViewAction;
import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.plugins.ansible.AnsibleAdHocCommandBuilder;
import org.jenkinsci.plugins.ansible.AnsiblePlaybookBuilder;
import org.jenkinsci.plugins.ansible.AnsibleVaultBuilder;
import org.jenkinsci.plugins.ansible.InventoryContent;
import org.jenkinsci.plugins.ansible.InventoryPath;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * @author lanwen (Merkushev Kirill)
 */
@WithJenkins
class JobDslIntegrationTest {
    private static final String JOB_NAME_IN_DSL_SCRIPT = "ansible";

    private static final String ANSIBLE_DSL_GROOVY_PLAYBOOK = "jobdsl/playbook.groovy";
    private static final String ANSIBLE_DSL_GROOVY_EXPANDER = "jobdsl/expander.groovy";
    private static final String ANSIBLE_DSL_GROOVY_CHECK_MODE = "jobdsl/checkMode.groovy";
    private static final String ANSIBLE_DSL_GROOVY_SECURITY_630 = "jobdsl/security630.groovy";
    private static final String ANSIBLE_DSL_GROOVY_PLAYBOOK_LEGACY = "jobdsl/legacyPlaybook.groovy";
    private static final String ANSIBLE_DSL_GROOVY_ADHOC = "jobdsl/adhoc.groovy";
    private static final String ANSIBLE_DSL_GROOVY_VAULT = "jobdsl/vault.groovy";
    private static final String ANSIBLE_DSL_GROOVY_PLAYBOOK_BUILDER = "jobdsl/playbookBuilder.groovy";

    @Test
    void shouldCreateJobSecurity630Dsl(JenkinsRule r) throws Exception {
        AnsiblePlaybookBuilder step = generateJob(r, ANSIBLE_DSL_GROOVY_SECURITY_630)
                .getBuildersList()
                .get(AnsiblePlaybookBuilder.class);
        assertThat("Should add playbook builder", step, notNullValue());
        assertThat("disableHostKeyChecking", step.disableHostKeyChecking, is(false));
    }

    @Test
    void shouldCreateJobWithPlaybookDsl(JenkinsRule r) throws Exception {
        AnsiblePlaybookBuilder step =
                generateJob(r, ANSIBLE_DSL_GROOVY_PLAYBOOK).getBuildersList().get(AnsiblePlaybookBuilder.class);
        assertThat("Should add playbook builder", step, notNullValue());

        assertThat("playbook", step.playbook, is("path/playbook.yml"));
        assertThat("inventory", step.inventory, isA(InventoryPath.class));
        assertThat("ansibleName", step.ansibleName, is("1.9.4"));
        assertThat("limit", step.limit, is("retry.limit"));
        assertThat("tags", step.tags, is("one,two"));
        assertThat("skippedTags", step.skippedTags, is("three"));
        assertThat("startAtTask", step.startAtTask, is("task"));
        assertThat("credentialsId", step.credentialsId, is("credsid"));
        assertThat("become", step.become, is(true));
        assertThat("becomeUser", step.becomeUser, is("user"));
        assertThat("checkMode", step.checkMode, is(false));
        assertThat("sudo", step.sudo, is(false));
        assertThat("sudoUser", step.sudoUser, is("root"));
        assertThat("forks", step.forks, is(6));
        assertThat("unbufferedOutput", step.unbufferedOutput, is(false));
        assertThat("colorizedOutput", step.colorizedOutput, is(true));
        assertThat("disableHostKeyChecking", step.disableHostKeyChecking, is(false));
        assertThat("additionalParameters", step.additionalParameters, is("params"));
        assertThat("extraVar.key", step.extraVars.get(0).getKey(), is("key"));
        assertThat("extraVar.value", step.extraVars.get(0).getSecretValue().getPlainText(), is("value"));
        assertThat("extraVar.hidden", step.extraVars.get(0).isHidden(), is(true));
        assertThat("extraVar.autoTypeInference", step.extraVars.get(0).isAutoTypeInference(), is(true));
    }

    @Test
    void shouldCreateJobWithCheckMode(JenkinsRule r) throws Exception {
        AnsiblePlaybookBuilder step =
                generateJob(r, ANSIBLE_DSL_GROOVY_CHECK_MODE).getBuildersList().get(AnsiblePlaybookBuilder.class);
        assertThat("Should add playbook builder", step, notNullValue());

        assertThat("playbook", step.playbook, is("path/playbook.yml"));
        assertThat("inventory", step.inventory, isA(InventoryPath.class));
        assertThat("ansibleName", step.ansibleName, is("1.9.4"));
        assertThat("checkMode", step.checkMode, is(true));
    }

    @Test
    void shouldCreateJobWithVarExpander(JenkinsRule r) throws Exception {

        assumeFalse(SystemUtils.IS_OS_WINDOWS);

        // Add credentials
        StringCredentials vaultCredentials = new StringCredentialsImpl(
                CredentialsScope.GLOBAL,
                "vaultCredentialsString",
                "test username password",
                Secret.fromString("test-secret"));
        StringCredentials credentials = new StringCredentialsImpl(
                CredentialsScope.GLOBAL, "credentialsString", "test credentials", Secret.fromString("test"));
        CredentialsStore store =
                CredentialsProvider.lookupStores(r.jenkins).iterator().next();
        store.addCredentials(Domain.global(), vaultCredentials);
        store.addCredentials(Domain.global(), credentials);

        // Create job via jobdsl with var expander
        AnsiblePlaybookBuilder step =
                generateJob(r, ANSIBLE_DSL_GROOVY_EXPANDER).getBuildersList().get(AnsiblePlaybookBuilder.class);
        assertThat("Should add playbook builder", step, notNullValue());
        assertThat("playbook", step.playbook, is("playbook.yml"));
        assertThat("inventory", step.inventory, isA(InventoryPath.class));
        assertThat("vaultCredentialsId", step.vaultCredentialsId, is("${vault_credentials_id}"));
        assertThat("credentialsId", step.credentialsId, is("${credentials_id}"));

        List<ParameterValue> parameters = new ArrayList<>();
        parameters.add(new StringParameterValue("inventory_repository", "/ansible"));
        parameters.add(new StringParameterValue("vault_credentials_id", "vaultCredentialsString"));
        parameters.add(new StringParameterValue("credentials_id", "credentialsString"));
        ParametersAction parametersAction = new ParametersAction(parameters);

        FreeStyleProject freeStyleProject = r.getInstance().getItemByFullName("ansible", FreeStyleProject.class);
        FreeStyleBuild build =
                freeStyleProject.scheduleBuild2(0, parametersAction).get();
        assertThat(
                build.getLog(),
                allOf(containsString(
                        "ansible-playbook playbook.yml -i /ansible/inventory.yml -f 5 --vault-password-file ")));
    }

    @Test
    void shouldCreateJobWithLegacyPlaybookDsl(JenkinsRule r) throws Exception {
        AnsiblePlaybookBuilder step = generateJob(r, ANSIBLE_DSL_GROOVY_PLAYBOOK_LEGACY)
                .getBuildersList()
                .get(AnsiblePlaybookBuilder.class);
        assertThat("Should add playbook builder", step, notNullValue());

        assertThat("playbook", step.playbook, is("path/playbook.yml"));
        assertThat("inventory", step.inventory, isA(InventoryPath.class));
        assertThat("ansibleName", step.ansibleName, is("1.9.4"));
        assertThat("limit", step.limit, is("retry.limit"));
        assertThat("tags", step.tags, is("one,two"));
        assertThat("skippedTags", step.skippedTags, is("three"));
        assertThat("startAtTask", step.startAtTask, is("task"));
        assertThat("credentialsId", step.credentialsId, is("credsid"));
        assertThat("become", step.become, is(false));
        assertThat("becomeUser", step.becomeUser, is("root"));
        assertThat("sudo", step.sudo, is(true));
        assertThat("sudoUser", step.sudoUser, is("user"));
        assertThat("forks", step.forks, is(6));
        assertThat("unbufferedOutput", step.unbufferedOutput, is(false));
        assertThat("colorizedOutput", step.colorizedOutput, is(true));
        assertThat("disableHostKeyChecking", step.disableHostKeyChecking, is(true));
        assertThat("additionalParameters", step.additionalParameters, is("params"));
        assertThat("extraVar.key", step.extraVars.get(0).getKey(), is("key"));
        assertThat("extraVar.value", step.extraVars.get(0).getSecretValue().getPlainText(), is("value"));
        assertThat("extraVar.hidden", step.extraVars.get(0).isHidden(), is(true));
        assertThat("extraVar.autoTypeInference", step.extraVars.get(0).isAutoTypeInference(), is(true));
    }

    @Test
    void shouldCreateJobAdhocDsl(JenkinsRule r) throws Exception {
        AnsibleAdHocCommandBuilder step =
                generateJob(r, ANSIBLE_DSL_GROOVY_ADHOC).getBuildersList().get(AnsibleAdHocCommandBuilder.class);
        assertThat("Should add adhoc builder", step, notNullValue());

        assertThat("module", step.module, is("module"));
        assertThat("inventory", step.inventory, isA(InventoryContent.class));
        assertThat("ansibleName", step.ansibleName, is("1.9.1"));

        assertThat("credentialsId", step.credentialsId, is("credsid"));
        assertThat("hostPattern", step.hostPattern, is("pattern"));
        assertThat("become", step.become, is(false));
        assertThat("becomeUser", step.becomeUser, is("root"));
        assertThat("forks", step.forks, is(5));
        assertThat("unbufferedOutput", step.unbufferedOutput, is(true));
        assertThat("colorizedOutput", step.colorizedOutput, is(false));
        assertThat("disableHostKeyChecking", step.disableHostKeyChecking, is(false));
    }

    @Test
    void shouldCreateJobWithVaultDsl(JenkinsRule r) throws Exception {
        AnsibleVaultBuilder step =
                generateJob(r, ANSIBLE_DSL_GROOVY_VAULT).getBuildersList().get(AnsibleVaultBuilder.class);
        assertThat("Should add playbook builder", step, notNullValue());

        assertThat("action", step.action, is("encrypt_string"));
        assertThat("content", step.content, is("my_secret"));
        assertThat("vaultCredentialsId", step.vaultCredentialsId, is("ansible_vault_credentials"));
    }

    @Test
    void shouldCreateJobWithPlaybookBuilderDsl(JenkinsRule r) throws Exception {
        AnsiblePlaybookBuilder step = generateJob(r, ANSIBLE_DSL_GROOVY_PLAYBOOK_BUILDER)
                .getBuildersList()
                .get(AnsiblePlaybookBuilder.class);
        assertThat("Should add playbook builder", step, notNullValue());

        assertThat("playbook", step.playbook, is("path/playbook.yml"));
        assertThat("extraVar.key", step.extraVars.get(0).getKey(), is("key"));
        assertThat("extraVar.value", step.extraVars.get(0).getSecretValue().getPlainText(), is("value"));
        assertThat("extraVar.hidden", step.extraVars.get(0).isHidden(), is(true));
        assertThat("extraVar.autoTypeInference", step.extraVars.get(0).isAutoTypeInference(), is(true));
    }

    private static FreeStyleProject generateJob(JenkinsRule rule, String script) throws Exception {
        FreeStyleProject job = rule.createFreeStyleProject();
        String scriptText = Resources.toString(Resources.getResource(script), StandardCharsets.UTF_8);

        ExecuteDslScripts builder = new ExecuteDslScripts();
        builder.setScriptText(scriptText);
        builder.setRemovedJobAction(RemovedJobAction.DELETE);
        builder.setRemovedViewAction(RemovedViewAction.DELETE);
        builder.setLookupStrategy(LookupStrategy.JENKINS_ROOT);
        job.getBuildersList().add(builder);

        rule.buildAndAssertSuccess(job);

        assertThat(rule.getInstance().getJobNames(), hasItem(is(JOB_NAME_IN_DSL_SCRIPT)));

        return rule.getInstance().getItemByFullName(JOB_NAME_IN_DSL_SCRIPT, FreeStyleProject.class);
    }
}
