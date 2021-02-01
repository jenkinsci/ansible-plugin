package org.jenkinsci.plugins.ansible.jobdsl;

import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import org.jenkinsci.plugins.ansible.AnsibleAdHocCommandBuilder;
import org.jenkinsci.plugins.ansible.AnsiblePlaybookBuilder;
import org.jenkinsci.plugins.ansible.AnsibleVaultBuilder;
import org.jenkinsci.plugins.ansible.jobdsl.context.AnsibleContext;

/**
 * @author lanwen (Merkushev Kirill)
 */
@Extension(optional = true)
public class AnsibleJobDslExtension extends ContextExtensionPoint {

    @DslExtensionMethod(context = StepContext.class)
    public Object ansibleAdHoc(String module, String command, Runnable closure) {
        AnsibleContext context = new AnsibleContext();
        executeInContext(closure, context);

        AnsibleAdHocCommandBuilder adhoc = new AnsibleAdHocCommandBuilder(
                context.getHostPattern(), context.getInventory(), module, command
        );

        adhoc.setAdditionalParameters(context.getAdditionalParameters());
        adhoc.setAnsibleName(context.getAnsibleName());
        adhoc.setCredentialsId(context.getCredentialsId());
        adhoc.setVaultCredentialsId(context.getVaultCredentialsId());
        adhoc.setColorizedOutput(context.isColorizedOutput());
        adhoc.setForks(context.getForks());
        adhoc.setDisableHostKeyChecking(context.isDisableHostKeyChecking());
        adhoc.setBecome(context.isBecome());
        adhoc.setBecomeUser(context.getBecomeUser());
        adhoc.setSudo(context.isSudo());
        adhoc.setSudoUser(context.getSudoUser());
        adhoc.setUnbufferedOutput(context.isUnbufferedOutput());
        adhoc.setExtraVars(context.getExtraVars());

        return adhoc;
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object ansiblePlaybook(String playbook, Runnable closure) {
        AnsibleContext context = new AnsibleContext();
        executeInContext(closure, context);

        AnsiblePlaybookBuilder plbook = new AnsiblePlaybookBuilder(playbook, context.getInventory());

        plbook.setAdditionalParameters(context.getAdditionalParameters());
        plbook.setAnsibleName(context.getAnsibleName());
        plbook.setCredentialsId(context.getCredentialsId());
        plbook.setVaultCredentialsId(context.getVaultCredentialsId());
        plbook.setColorizedOutput(context.isColorizedOutput());
        plbook.setForks(context.getForks());
        plbook.setDisableHostKeyChecking(context.isDisableHostKeyChecking());
        plbook.setBecome(context.isBecome());
        plbook.setBecomeUser(context.getBecomeUser());
        plbook.setSudo(context.isSudo());
        plbook.setSudoUser(context.getSudoUser());
        plbook.setUnbufferedOutput(context.isUnbufferedOutput());
        plbook.setLimit(context.getLimit());
        plbook.setTags(context.getTags());
        plbook.setSkippedTags(context.getSkippedTags());
        plbook.setStartAtTask(context.getStartAtTask());
        plbook.setExtraVars(context.getExtraVars());

        return plbook;
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object ansibleVault(Runnable closure) {
        AnsibleContext context = new AnsibleContext();
        executeInContext(closure, context);

        AnsibleVaultBuilder vault = new AnsibleVaultBuilder();

        vault.setAnsibleName(context.getAnsibleName());
        vault.setAction(context.getAction());
        vault.setVaultCredentialsId(context.getVaultCredentialsId());
        vault.setNewVaultCredentialsId(context.getNewVaultCredentialsId());
        vault.setContent(context.getContent());
        vault.setInput(context.getInput());
        vault.setOutput(context.getOutput());

        return vault;
    }
}
