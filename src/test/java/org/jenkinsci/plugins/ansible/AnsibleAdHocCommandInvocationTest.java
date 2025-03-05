package org.jenkinsci.plugins.ansible;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.*;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Created with IntelliJ IDEA.
 * User: jcsirot
 * Date: 22/05/15
 * Time: 19:30
 * To change this template use File | Settings | File Templates.
 */
class AnsibleAdHocCommandInvocationTest {

    @Test
    void should_generate_simple_invocation() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation =
                new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setForks(5);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString(), is("/usr/local/bin/ansible localhost -i /tmp/hosts -m ping -f 5"));
    }

    @Test
    void should_generate_no_forks() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation =
                new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setForks(0);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString(), is("/usr/local/bin/ansible localhost -i /tmp/hosts -m ping"));
    }

    @Test
    void should_generate_simple_invocation_with_env() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation =
                new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setForks(5);
        invocation.setColorizedOutput(true);
        invocation.setDisableHostKeyCheck(true);
        invocation.setUnbufferedOutput(true);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<Map<String, String>> argument = ArgumentCaptor.forClass(Map.class);
        verify(runner).execute(any(ArgumentListBuilder.class), argument.capture());

        assertThat(argument.getValue(), hasEntry("PYTHONUNBUFFERED", "1"));
        assertThat(argument.getValue(), hasEntry("ANSIBLE_FORCE_COLOR", "true"));
        assertThat(argument.getValue(), hasEntry("ANSIBLE_HOST_KEY_CHECKING", "False"));
    }

    @Test
    void secure_by_default_SEC_630() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation =
                new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setForks(5);
        invocation.setColorizedOutput(true);
        // invocation.setDisableHostKeyCheck(true);
        invocation.setUnbufferedOutput(true);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<Map<String, String>> argument = ArgumentCaptor.forClass(Map.class);
        verify(runner).execute(any(ArgumentListBuilder.class), argument.capture());

        assertThat(argument.getValue(), aMapWithSize(2));
        assertThat(argument.getValue(), hasEntry("PYTHONUNBUFFERED", "1"));
        assertThat(argument.getValue(), hasEntry("ANSIBLE_FORCE_COLOR", "true"));
    }

    @Test
    void should_handle_variables() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        EnvVars vars = new EnvVars();
        vars.put("MODULE", "ping");
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(vars);
        AnsibleAdHocCommandInvocation invocation =
                new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("${MODULE}");
        invocation.setForks(5);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());

        assertThat(argument.getValue().toString(), is("/usr/local/bin/ansible localhost -i /tmp/hosts -m ping -f 5"));
    }
}
