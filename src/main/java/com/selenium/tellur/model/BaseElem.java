package com.selenium.tellur.model;

public class BaseElem { // TODO name it BaseWebElem

    String name;

    AccessMethode accessMethode;

    String value;

    public BaseElem(String name, AccessMethode accessMethode, String value) {
        this.name = name;
        this.accessMethode = accessMethode;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + name + " " + accessMethode + " " + value;
    }

    public String getName() {
        return name;
    }

    public AccessMethode getAccessMethode() {
        return accessMethode;
    }

    public String getValue() {
        return value;
    }
}
