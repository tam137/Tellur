package com.selenium.tellur.service;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
public class ConfigService {

    @Value("${maxFunctionDepth}") private Integer maxFunctionDepth;

    @Value("${timeOut}") private int timeOut;

    @Value("${testplanDir}") private String testplanDir;

    @Value("${testplanName}") private String testplanName;

    @Value("${libDir}") private String libDir;

    @Value("${regex.var_unresolved}") private String regexVarUnresolved;

    @Value("${regex.var_name}") private String regexVarName;

    @Value("${regex.webelem_name}") private String regexWebelemName;

    @Value("${regex.function_signature}") private String regexFunctionSignature;

    @Value("${shellUtil}") private String shellUtil;

    public HtmlUnitDriver driver = null;

    public WebDriverWait wait = null;


    @Autowired public void Config() {
        reloadConfig();
    }

    public void reloadConfig() {
        HtmlUnitDriver webDriver = new HtmlUnitDriver();
        this.driver = webDriver;
        this.wait = new WebDriverWait(webDriver, Duration.of(timeOut, ChronoUnit.SECONDS));
        this.driver.manage().deleteAllCookies();
        this.driver.getWebClient().getOptions().setJavaScriptEnabled(true);
    }

    public Integer getMaxFunctionDepth() {
        return maxFunctionDepth;
    }

    public String getTestplanDir() {
        return testplanDir;
    }

    public String getLibDir() {
        return libDir;
    }

    public String getRegexVarUnresolved() {
        return regexVarUnresolved;
    }

    public String getRegexVarName() {
        return regexVarName;
    }

    public String getRegexWebelemName() {
        return regexWebelemName;
    }

    public String getRegexFunctionSignature() {
        return regexFunctionSignature;
    }

    public String getShell() {
        return shellUtil;
    }

    public String getTestplanName() {
        return testplanName;
    }

    public void setTestplanName(String testplanName) {
        this.testplanName = testplanName;
    }
}
