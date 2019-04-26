package org.jenkinsci.plugins.ansible;

import java.io.IOException;
import java.util.Map;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: sirot
 * Date: 23/05/15
 * Time: 22:56
 * To change this template use File | Settings | File Templates.
 */
public class CLIRunner
{
    private final Launcher launcher;
    private final Run<?, ?> build;
    private final TaskListener listener;
    private final FilePath ws;

    public CLIRunner(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        this.launcher = launcher;
        this.build = build;
        this.listener = listener;
        this.ws = build.getWorkspace();
    }

    public CLIRunner(Run<?, ?> build, FilePath ws, Launcher launcher, TaskListener listener) {
        this.launcher = launcher;
        this.build = build;
        this.listener = listener;
        this.ws = ws;
    }

    public boolean execute(ArgumentListBuilder args, Map<String, String> environment)
            throws IOException, InterruptedException
    {
        try {
            return launcher.launch()
                    .pwd(ws)
                    .envs(environment)
                    .cmds(args)
                    .stdout(listener).join() == 0;
        }
        finally {
            listener.getLogger().flush();
        }
    }
}
