package org.jenkinsci.plugins.ansible;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.Secret;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Created with IntelliJ IDEA.
 * User: jcsirot
 * Date: 22/05/15
 * Time: 19:30
 * To change this template use File | Settings | File Templates.
 */
public class AnsibleAdHocCommandInvocationTest {

    @Test
    public void should_generate_simple_invocation() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?,?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation = new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setForks(5);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString())
                .isEqualTo("/usr/local/bin/ansible localhost -i /tmp/hosts -m ping -f 5");
    }

    @Test
    public void should_generate_no_forks() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?,?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation = new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setForks(0);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString())
                .isEqualTo("/usr/local/bin/ansible localhost -i /tmp/hosts -m ping");
    }

    @Test
    public void should_generate_simple_invocation_with_env() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?,?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation = new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
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
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(runner).execute(any(ArgumentListBuilder.class), argument.capture());
        assertThat((Map<String, String>)argument.getValue())
                .containsEntry("PYTHONUNBUFFERED", "1")
                .containsEntry("ANSIBLE_FORCE_COLOR", "true")
                .containsEntry("ANSIBLE_HOST_KEY_CHECKING", "False");
    }

    @Test
    public void secure_by_default_SEC_630() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?,?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation = new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setForks(5);
        invocation.setColorizedOutput(true);
        //invocation.setDisableHostKeyCheck(true);
        invocation.setUnbufferedOutput(true);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(runner).execute(any(ArgumentListBuilder.class), argument.capture());
        assertThat((Map<String, String>)argument.getValue())
                .containsEntry("PYTHONUNBUFFERED", "1")
                .containsEntry("ANSIBLE_FORCE_COLOR", "true")
                .doesNotContainEntry("ANSIBLE_HOST_KEY_CHECKING", "False");
    }


    @Test
    @Ignore("build.getWorkspace() cannot be mocked")
    public void should_handle_private_key_credentials() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        SSHUserPrivateKey pkey = mock(SSHUserPrivateKey.class);
        when(pkey.getUsername()).thenReturn("mylogin");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?,?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation = new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setCredentials(pkey);
        invocation.setForks(5);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString())
                .matches("/usr/local/bin/ansible localhost -i /tmp/hosts -m ping -f 5 --private-key .+ -u mylogin");
    }

    @Test
    @Ignore("Secret can neither be instanced nor mocked")
    public void should_handle_password_credentials() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        StandardUsernamePasswordCredentials password = mock(StandardUsernamePasswordCredentials.class);
        when(password.getUsername()).thenReturn("mylogin");
        when(password.getPassword()).thenReturn(Secret.fromString("aStrongSecretPassword"));
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?,?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        AnsibleAdHocCommandInvocation invocation = new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("ping");
        invocation.setCredentials(password);
        invocation.setForks(5);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString())
                .isEqualTo("sshpass ****** /usr/local/bin/ansible localhost -i /tmp/hosts -m ping -f 5 " +
                        "-u" +
                        " mylogin -k");
    }

    @Test
    public void should_handle_variables() throws Exception {
        // Given
        Inventory inventory = new InventoryPath("/tmp/hosts");
        BuildListener listener = mock(BuildListener.class);
        CLIRunner runner = mock(CLIRunner.class);
        AbstractBuild<?,?> build = mock(AbstractBuild.class);
        EnvVars vars = new EnvVars();
        vars.put("MODULE", "ping");
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(vars);
        AnsibleAdHocCommandInvocation invocation = new AnsibleAdHocCommandInvocation("/usr/local/bin/ansible", build, listener);
        invocation.setHostPattern("localhost");
        invocation.setInventory(inventory);
        invocation.setModule("${MODULE}");
        invocation.setForks(5);
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString())
                .isEqualTo("/usr/local/bin/ansible localhost -i /tmp/hosts -m ping -f 5");
    }
}
