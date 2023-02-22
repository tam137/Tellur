package com.selenium.tellur.service;

import com.selenium.tellur.model.Testcase;

import java.util.List;

public class ReportBuilderImpl {

    private final List<Testcase> testcaseList;

    public ReportBuilderImpl(List<Testcase> testcaseList) {
        this.testcaseList = testcaseList;
    }

    public void printReport() {
        this.testcaseList.forEach(testCase -> {
            System.out.println(testCase.getName() +
                (testCase.getName().length() < 12 ? "\t\t" : "\t") +
                testCase.getPassedStatus() + "\t" + testCase.getMsg());
        });
    }

}
