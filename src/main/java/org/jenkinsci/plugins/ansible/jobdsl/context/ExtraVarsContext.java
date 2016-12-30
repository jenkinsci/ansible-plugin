package org.jenkinsci.plugins.ansible.jobdsl.context;

import java.util.ArrayList;
import java.util.List;

import javaposse.jobdsl.dsl.Context;
import org.jenkinsci.plugins.ansible.ExtraVar;

/**
 * @author pawbur (Pawel Burchard)
 */
public class ExtraVarsContext implements Context {
    private List<ExtraVar> extraVars = new ArrayList<ExtraVar>();

    public void extraVar(String key, String value, boolean hidden) {
        ExtraVar extraVar = new ExtraVar();
        extraVar.setKey(key);
        extraVar.setValue(value);
        extraVar.setHidden(hidden);
        this.extraVars.add(extraVar);
    }

    public List<ExtraVar> getExtraVars() {
        return extraVars;
    }
}
