package org.jenkinsci.plugins.ansible;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import hudson.FilePath;
import hudson.util.Secret;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisabledOnOs(OS.WINDOWS)
class UtilsTest {

    @TempDir
    private File tmp;

    @ParameterizedTest
    @ValueSource(
            strings = {
                "simple",
                "with space",
                "double\"quote",
                "single'quote",
                "dollar$HOME",
                "pa$$word",
                "back`tick",
                "back\\slash",
                "-n",
                "7D%TgX`EnfgHkfv$H9qhfg84sZSQwT&LjH~ZEfghjkzn@^sYpR"
            })
    void askPassScriptShouldPrintPassphraseVerbatim(String passphrase) throws Exception {
        SSHUserPrivateKey credentials = mock(SSHUserPrivateKey.class);
        when(credentials.getPassphrase()).thenReturn(Secret.fromString(passphrase));

        FilePath script = Utils.createSshAskPassFile(null, new FilePath(tmp), credentials, false);

        Process process = new ProcessBuilder(script.getRemote())
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        process.waitFor();

        assertThat(output, is(passphrase + "\n"));
    }
}
