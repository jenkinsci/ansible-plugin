package org.jenkinsci.plugins.ansible.jobdsl;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import javaposse.jobdsl.plugin.LookupStrategy;
import javaposse.jobdsl.plugin.RemovedJobAction;
import javaposse.jobdsl.plugin.RemovedViewAction;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class DslJobRule implements TestRule {

    public static final String JOB_NAME_IN_DSL_SCRIPT = "ansible";

    private JenkinsRule jRule;

    private FreeStyleProject generated;

    public DslJobRule(JenkinsRule jRule) {
        this.jRule = jRule;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before(description);
                base.evaluate();
            }
        };
    }

    private void before(Description description) throws Exception {
        FreeStyleProject job = jRule.createFreeStyleProject();
        String script = description.getAnnotation(WithJobDsl.class).value();
        String scriptText = Resources.toString(Resources.getResource(script), Charsets.UTF_8);

        job.getBuildersList().add(
                new ExecuteDslScripts(
                        new ExecuteDslScripts.ScriptLocation(
                                null, null,
                                scriptText
                        ),
                        false,
                        RemovedJobAction.DELETE,
                        RemovedViewAction.DELETE,
                        LookupStrategy.JENKINS_ROOT
                )
        );

        jRule.buildAndAssertSuccess(job);

        assertThat(jRule.getInstance().getJobNames(), hasItem(is(JOB_NAME_IN_DSL_SCRIPT)));

        generated = jRule.getInstance().getItemByFullName(JOB_NAME_IN_DSL_SCRIPT, FreeStyleProject.class);
    }

    public FreeStyleProject getGeneratedJob() {
        return generated;
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface WithJobDsl {
        String value();
    }
}

