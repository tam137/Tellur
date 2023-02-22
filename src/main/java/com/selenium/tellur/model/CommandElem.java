package com.selenium.tellur.model;

import java.util.ArrayList;
import java.util.List;

public class CommandElem extends Param {

    private Integer lineNr;

    private BuildInCmds cmd;

    private List<Param> params;

    private Param returnValue = null;

    public CommandElem(Integer lineNr, BuildInCmds cmd, List<Param> params) {
        super(null, Type.functionReference);
        this.lineNr = lineNr;
        this.cmd = cmd;
        this.params = params;
    }

    public CommandElem(CommandElem commandElem) {
        super(commandElem);
        this.lineNr = commandElem.lineNr;
        this.cmd = commandElem.cmd;
        this.params = new ArrayList<>(commandElem.getParams());
    }

    public Integer getLineNr() {
        return lineNr;
    }

    public BuildInCmds getCmd() {
        return cmd;
    }

    public List<Param> getParams() {
        return params;
    }

    public boolean hasConsealedParams() {
        return this.params.stream().anyMatch(Param::isConsealed);
    }

    @Override
    public String toString() {
        return lineNr + ": " + cmd + params;
    }

    public Param getReturnValue() {
        if (this.cmd.equals(BuildInCmds.ret) &&
            !this.params.isEmpty() &&
            this.params.get(0).getType() != Type.functionReference
        ) {
            return this.params.get(0);
        }
        return null;
    }

    public void setReturnValue(Param param) {
        this.returnValue = param;
    }

    public Boolean containsOneOrMoreFunctionParams() {
        return this.getParams().stream().anyMatch(Param::isFunctionReference);
    }

    public void setCmd(String cmd) {
        this.cmd = BuildInCmds.valueOf(cmd);
    }
}
