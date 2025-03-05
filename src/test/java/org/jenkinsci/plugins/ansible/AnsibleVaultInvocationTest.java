package org.jenkinsci.plugins.ansible;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Test Vault invocations
 *
 * @author Michael Cresswell
 */
class AnsibleVaultInvocationTest {

    private static final String EXE = "ansible-vault";

    @Test
    void shouldGenerateEncryptString() throws Exception {
        CLIRunner runner = mock(CLIRunner.class);
        AnsibleVaultInvocation invocation = getInvocation();

        invocation.setAction("encrypt_string");
        invocation.setContent("Test Content");
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());

        assertThat(argument.getValue().toString(), is("ansible-vault encrypt_string ******"));
    }

    @Test
    void shouldGenerateEncrypt() throws Exception {
        CLIRunner runner = mock(CLIRunner.class);
        AnsibleVaultInvocation invocation = getInvocation();

        invocation.setAction("encrypt");
        invocation.setInput("/tmp/my_var_file.yml");
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString(), is("ansible-vault encrypt /tmp/my_var_file.yml"));
    }

    @Test
    void shouldGenerateDecrypt() throws Exception {
        CLIRunner runner = mock(CLIRunner.class);
        AnsibleVaultInvocation invocation = getInvocation();

        invocation.setAction("decrypt");
        invocation.setInput("/tmp/my_var_file.yml");
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString(), is("ansible-vault decrypt /tmp/my_var_file.yml"));
    }

    @Test
    void shouldGenerateRekey() throws Exception {
        CLIRunner runner = mock(CLIRunner.class);
        AnsibleVaultInvocation invocation = getInvocation();

        invocation.setAction("rekey");
        invocation.setInput("/tmp/my_var_file.yml");
        // When
        invocation.execute(runner);
        // Then
        ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
        verify(runner).execute(argument.capture(), anyMap());
        assertThat(argument.getValue().toString(), is("ansible-vault rekey /tmp/my_var_file.yml"));
    }

    @Test
    void shouldNotGenerateView() throws Exception {
        CLIRunner runner = mock(CLIRunner.class);
        AnsibleVaultInvocation invocation = getInvocation();

        invocation.setAction("view");
        invocation.setInput("/tmp/my_var_file.yml");
        // When
        assertThrows(AbortException.class, () -> invocation.execute(runner));
        // Then
        verifyNoInteractions(runner);
    }

    @Test
    void shouldNotGenerateEdit() throws Exception {
        CLIRunner runner = mock(CLIRunner.class);
        AnsibleVaultInvocation invocation = getInvocation();

        invocation.setAction("edit");
        invocation.setInput("/tmp/my_var_file.yml");
        // When
        assertThrows(AbortException.class, () -> invocation.execute(runner));
        // Then
        verifyNoInteractions(runner);
    }

    @Test
    void shouldNotGenerateCreate() throws Exception {
        CLIRunner runner = mock(CLIRunner.class);
        AnsibleVaultInvocation invocation = getInvocation();

        invocation.setAction("edit");
        invocation.setInput("/tmp/my_var_file.yml");
        // When
        assertThrows(AbortException.class, () -> invocation.execute(runner));
        // Then
        verifyNoInteractions(runner);
    }

    private AnsibleVaultInvocation getInvocation() throws Exception {
        // Given
        BuildListener listener = mock(BuildListener.class);
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
        return new AnsibleVaultInvocation(EXE, build, listener, new EnvVars());
    }
}
