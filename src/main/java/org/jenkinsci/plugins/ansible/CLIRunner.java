package org.jenkinsci.plugins.ansible;

import java.io.IOException;
import java.util.Map;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ArgumentListBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: sirot
 * Date: 23/05/15
 * Time: 22:56
 * To change this template use File | Settings | File Templates.
 */
class CLIRunner
{
    private final Launcher launcher;
    private final AbstractBuild<?, ?> build;
    private final BuildListener listener;

    public CLIRunner(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        this.launcher = launcher;
        this.build = build;
        this.listener = listener;
    }

    public boolean execute(ArgumentListBuilder args, Map<String, String> environment)
            throws IOException, InterruptedException
    {
        return launcher.launch()
                .pwd(build.getWorkspace())
                .envs(environment)
                .cmds(args)
                .stdout(listener).join() == 0;
    }
}
