package com.selenium.tellur;

public class TestcaseFailedException extends Exception {

    private String msg;

    public TestcaseFailedException() {

    }

    public TestcaseFailedException(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

}
