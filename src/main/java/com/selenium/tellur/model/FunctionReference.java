package com.selenium.tellur.model;

public class FunctionReference extends CommandElem {

    private final String targetFunctionName;

    private Param returnValue;

    public FunctionReference(CommandElem commandElem, String funcRefName) {
        super(commandElem.getLineNr(), BuildInCmds.call, commandElem.getParams());
        this.targetFunctionName = funcRefName;
    }

    public FunctionReference(FunctionReference funcRef) {
        super(funcRef);
        this.targetFunctionName = funcRef.getTargetFunctionName();
    }

    public String getTargetFunctionName() {
        return targetFunctionName;
    }

    public String toString() {
            return targetFunctionName + getParams().toString();
    }

    public Boolean equals(FunctionReference functionReference) {
        return this.toString().equals(functionReference.toString());
    }

    public void setReturnValue(Param param) {
        this.returnValue = param;
    }

}
