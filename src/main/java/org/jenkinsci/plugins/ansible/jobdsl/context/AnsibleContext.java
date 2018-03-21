package org.jenkinsci.plugins.ansible.jobdsl.context;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import org.jenkinsci.plugins.ansible.ExtraVar;
import org.jenkinsci.plugins.ansible.Inventory;
import org.jenkinsci.plugins.ansible.InventoryContent;
import org.jenkinsci.plugins.ansible.InventoryPath;


/**
 * @author lanwen (Merkushev Kirill)
 */
public class AnsibleContext implements Context {
    private Inventory inventory;
    private String ansibleName;
    private String action;
    private String credentialsId;
    private String vaultCredentialsId;
    private String newVaultCredentialsId;
    private String content;
    private String input;
    private String output;
    private boolean become = false;
    private String becomeUser = "root";
    private boolean sudo = false;
    private String sudoUser = "root";
    private int forks = 5;
    private boolean unbufferedOutput = true;
    private boolean colorizedOutput = false;
    private boolean disableHostKeyChecking = false;
    @Deprecated
    @SuppressWarnings("unused")
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private transient boolean hostKeyChecking = true;
    private String additionalParameters;
    ExtraVarsContext extraVarsContext = new ExtraVarsContext();

    /* adhoc-only */

    private String hostPattern;

    /* playbook-only */

    public String limit;
    public String tags;
    public String skippedTags;
    public String startAtTask;

    public void inventoryContent(String content, boolean dynamic) {
        this.inventory = new InventoryContent(content, dynamic);
    }

    public void inventoryContent(String content) {
        this.inventory = new InventoryContent(content, false);
    }

    public void inventoryPath(String path) {
        this.inventory = new InventoryPath(path);
    }

    public void ansibleName(String ansibleName) {
        this.ansibleName = ansibleName;
    }

    public void action(String action) {
        this.action = action;
    }

    public void credentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void vaultCredentialsId(String vaultCredentialsId) {
        this.vaultCredentialsId = vaultCredentialsId;
    }

    public void newVaultCredentialsId(String newVaultCredentialsId) {
        this.newVaultCredentialsId = newVaultCredentialsId;
    }

    public void content(String content) {
        this.content = content;
    }

    public void input(String input) {
        this.input = input;
    }

    public void output(String output) {
        this.output = output;
    }
    
    public void become(boolean become) {
        this.become = become;
    }

    public void becomeUser(String becomeUser) {
        this.becomeUser = becomeUser;
    }

    public void sudo(boolean sudo) {
        this.sudo = sudo;
    }

    public void sudoUser(String sudoUser) {
        this.sudoUser = sudoUser;
    }

    public void forks(int forks) {
        this.forks = forks;
    }

    public void unbufferedOutput(boolean unbufferedOutput) {
        this.unbufferedOutput = unbufferedOutput;
    }

    public void colorizedOutput(boolean colorizedOutput) {
        this.colorizedOutput = colorizedOutput;
    }

    public void disableHostKeyChecking(boolean disableHostKeyChecking) {
        this.disableHostKeyChecking = disableHostKeyChecking;
    }

    public void additionalParameters(String additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public void hostPattern(String hostPattern) {
        this.hostPattern = hostPattern;
    }

    public void limit(String limit) {
        this.limit = limit;
    }

    public void tags(String tags) {
        this.tags = tags;
    }

    public void skippedTags(String skippedTags) {
        this.skippedTags = skippedTags;
    }

    public void startAtTask(String startAtTask) {
        this.startAtTask = startAtTask;
    }

    public void extraVars(Runnable closure) {
        ContextExtensionPoint.executeInContext(closure, extraVarsContext);
    }

    public String getAction() {
        return action;
    }

    public String getAnsibleName() {
        return ansibleName;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getVaultCredentialsId() {
        return vaultCredentialsId;
    }

    public String getNewVaultCredentialsId() {
        return newVaultCredentialsId;
    }

    public String getContent() {
        return content;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isBecome() {
        return become;
    }

    public String getBecomeUser() {
        return becomeUser;
    }

    public boolean isSudo() {
        return sudo;
    }

    public String getSudoUser() {
        return sudoUser;
    }

    public int getForks() {
        return forks;
    }

    public boolean isUnbufferedOutput() {
        return unbufferedOutput;
    }

    public boolean isColorizedOutput() {
        return colorizedOutput;
    }

    public boolean isDisableHostKeyChecking() {
        return disableHostKeyChecking;
    }

    public String getAdditionalParameters() {
        return additionalParameters;
    }

    public String getHostPattern() {
        return hostPattern;
    }

    public String getLimit() {
        return limit;
    }

    public String getTags() {
        return tags;
    }

    public String getSkippedTags() {
        return skippedTags;
    }

    public String getStartAtTask() {
        return startAtTask;
    }

    public List<ExtraVar> getExtraVars() {
        return extraVarsContext.getExtraVars();
    }

    @Deprecated
    public void hostKeyChecking(boolean hostKeyChecking) {
    }
    @Deprecated
    public boolean isHostKeyChecking() {
        return true;
    }
}
