package org.jenkinsci.plugins.ansible.jobdsl.context;

import hudson.util.Secret;
import java.util.ArrayList;
import java.util.List;
import javaposse.jobdsl.dsl.Context;
import org.jenkinsci.plugins.ansible.ExtraVar;

/**
 * @author pawbur (Pawel Burchard)
 */
public class ExtraVarsContext implements Context {
    private List<ExtraVar> extraVars = new ArrayList<ExtraVar>();

    public void extraVar(String key, String value, boolean hidden, boolean autoTypeInference) {
        ExtraVar extraVar = new ExtraVar();
        extraVar.setKey(key);
        extraVar.setSecretValue(Secret.fromString(value));
        extraVar.setHidden(hidden);
        extraVar.setAutoTypeInference(autoTypeInference);
        this.extraVars.add(extraVar);
    }

    public void extraVar(String key, String value, boolean hidden) {
        extraVar(key, value, hidden, true);
    }

    public List<ExtraVar> getExtraVars() {
        return extraVars;
    }
}
