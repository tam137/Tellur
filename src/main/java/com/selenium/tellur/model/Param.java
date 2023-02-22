package com.selenium.tellur.model;


public class Param {

    public enum Type {
        string, number, var_unresolved, var_name, webelem, functionReference;
    }

    String value;

    Type type;

    Boolean consealed;

    public Param(String value, Type type) {
        this.value = value;
        this.type = type;
        this.consealed = false;
    }

    public Param(String value, Type type, Boolean consealed) {
        this.value = value;
        this.type = type;
        this.consealed = consealed;
    }

    public Param(Param param) {
        this.value = param.value;
        this.type = param.type;
        this.consealed = param.consealed;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        if (this.consealed)
            return "(" + type + " : ***** )";
        else if (this.type.equals(Type.string))
            return "(" + type + " : \"" + value + "\")";
        else
            return "(" + type + " : " + value + ")";
    }

    public Boolean isVariable() {
        return !this.getType().equals(Type.functionReference);
    }

    // TODO anlyse
    public Boolean isFunctionReference() {
        return !isVariable();
    }

    public Boolean isConsealed() {
        return this.consealed;
    }
}
