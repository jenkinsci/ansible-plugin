package org.jenkinsci.plugins.ansible.jobdsl;

import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import org.jenkinsci.plugins.ansible.AnsibleAdHocCommandBuilder;
import org.jenkinsci.plugins.ansible.AnsiblePlaybookBuilder;
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
        adhoc.setColorizedOutput(context.isColorizedOutput());
        adhoc.setForks(context.getForks());
        adhoc.setHostKeyChecking(context.isHostKeyChecking());
        adhoc.setSudo(context.isSudo());
        adhoc.setSudoUser(context.getSudoUser());
        adhoc.setUnbufferedOutput(context.isUnbufferedOutput());

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
        plbook.setColorizedOutput(context.isColorizedOutput());
        plbook.setForks(context.getForks());
        plbook.setHostKeyChecking(context.isHostKeyChecking());
        plbook.setSudo(context.isSudo());
        plbook.setSudoUser(context.getSudoUser());
        plbook.setUnbufferedOutput(context.isUnbufferedOutput());
        
        plbook.setLimit(context.getLimit());
        plbook.setTags(context.getTags());
        plbook.setSkippedTags(context.getSkippedTags());
        plbook.setStartAtTask(context.getStartAtTask());
        
        return plbook;
    }
}
