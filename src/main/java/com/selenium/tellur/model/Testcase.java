package com.selenium.tellur.model;

import java.util.ArrayList;
import java.util.List;

public class Testcase {

    String name;

    List<CommandElem> commandElemList = new ArrayList<>();

    String msg;

    TestResult passed = TestResult.none;

    private Testcase() { }

    public Testcase(String name) {
        this.name = name;
    }

    public String getName() {
        if (name.length() > 15) return name.substring(0, 15);
        else return name;
    }

    public List<CommandElem> getCommandLineList() {
        return commandElemList;
    }

    public void addCommand(CommandElem commandLine) {
        this.commandElemList.add(commandLine);
    }

    public void setPassed() {
        this.passed = TestResult.passed;
    }

    public void setFailed() {
        this.passed = TestResult.failed;
    }

    public TestResult getPassedStatus() {
        return this.passed;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg == null ? "" : this.msg;
    }

}

