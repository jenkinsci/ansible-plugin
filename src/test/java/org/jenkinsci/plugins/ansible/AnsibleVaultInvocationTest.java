package org.jenkinsci.plugins.ansible;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

/**
 * Test Vault invocations
 * 
 * @author Michael Cresswell
 */
public class AnsibleVaultInvocationTest {

	private final String exe = "ansible-vault";

	@Test
	public void shouldGenerateEncryptString() throws Exception {
		CLIRunner runner = mock(CLIRunner.class);
		AnsibleVaultInvocation invocation = getInvocation();
		
		invocation.setAction("encrypt_string");
		invocation.setContent("Test Content");
		// When
		invocation.execute(runner);
		// Then
		ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
		verify(runner).execute(argument.capture(), anyMap());
		assertThat(argument.getValue().toString()).isEqualTo("ansible-vault encrypt_string ******");
	}

	@Test
	public void shouldGenerateEncrypt() throws Exception {
		CLIRunner runner = mock(CLIRunner.class);
		AnsibleVaultInvocation invocation = getInvocation();
		
		invocation.setAction("encrypt");
		invocation.setInput("/tmp/my_var_file.yml");
		// When
		invocation.execute(runner);
		// Then
		ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
		verify(runner).execute(argument.capture(), anyMap());
		assertThat(argument.getValue().toString()).isEqualTo("ansible-vault encrypt /tmp/my_var_file.yml");
	}

	@Test
	public void shouldGenerateDecrypt() throws Exception {
		CLIRunner runner = mock(CLIRunner.class);
		AnsibleVaultInvocation invocation = getInvocation();
		
		invocation.setAction("decrypt");
		invocation.setInput("/tmp/my_var_file.yml");
		// When
		invocation.execute(runner);
		// Then
		ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
		verify(runner).execute(argument.capture(), anyMap());
		assertThat(argument.getValue().toString()).isEqualTo("ansible-vault decrypt /tmp/my_var_file.yml");
	}

	@Test
	public void shouldGenerateRekey() throws Exception {
		CLIRunner runner = mock(CLIRunner.class);
		AnsibleVaultInvocation invocation = getInvocation();
		
		invocation.setAction("rekey");
		invocation.setInput("/tmp/my_var_file.yml");
		// When
		invocation.execute(runner);
		// Then
		ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
		verify(runner).execute(argument.capture(), anyMap());
		assertThat(argument.getValue().toString()).isEqualTo("ansible-vault rekey /tmp/my_var_file.yml");
	}

	@Test(expected = AbortException.class)
	public void shouldNotGenerateView() throws Exception {
		CLIRunner runner = mock(CLIRunner.class);
		AnsibleVaultInvocation invocation = getInvocation();
		
		invocation.setAction("view");
		invocation.setInput("/tmp/my_var_file.yml");
		// When
		invocation.execute(runner);
		// Then
		ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
		verify(runner).execute(argument.capture(), anyMap());
	}

	@Test(expected = AbortException.class)
	public void shouldNotGenerateEdit() throws Exception {
		CLIRunner runner = mock(CLIRunner.class);
		AnsibleVaultInvocation invocation = getInvocation();
		
		invocation.setAction("edit");
		invocation.setInput("/tmp/my_var_file.yml");
		// When
		invocation.execute(runner);
		// Then
		ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
		verify(runner).execute(argument.capture(), anyMap());
	}

	@Test(expected = AbortException.class)
	public void shouldNotGenerateCreate() throws Exception {
		CLIRunner runner = mock(CLIRunner.class);
		AnsibleVaultInvocation invocation = getInvocation();
		
		invocation.setAction("edit");
		invocation.setInput("/tmp/my_var_file.yml");
		// When
		invocation.execute(runner);
		// Then
		ArgumentCaptor<ArgumentListBuilder> argument = ArgumentCaptor.forClass(ArgumentListBuilder.class);
		verify(runner).execute(argument.capture(), anyMap());
	}
	
	private AnsibleVaultInvocation getInvocation() throws IOException, InterruptedException, AnsibleInvocationException {
		// Given
		BuildListener listener = mock(BuildListener.class);
		AbstractBuild<?, ?> build = mock(AbstractBuild.class);
		when(build.getEnvironment(any(TaskListener.class))).thenReturn(new EnvVars());
		return new AnsibleVaultInvocation(exe, build, listener, new EnvVars());
	}


}
