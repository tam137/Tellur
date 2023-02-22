package com.selenium.tellur.service;

import com.selenium.tellur.TestcaseFailedException;
import com.selenium.tellur.model.ButtonElem;
import com.selenium.tellur.model.InputElem;
import com.selenium.tellur.model.LabelElem;
import com.selenium.tellur.model.Param;

import java.io.IOException;
import java.util.List;

public interface CmdService {

    void click(ButtonElem button) throws TestcaseFailedException;

    void sendKeys(InputElem field, String input) throws TestcaseFailedException;

    void navigateTo(String url) throws TestcaseFailedException;

    void printLabelText(LabelElem labelElem) throws TestcaseFailedException;

    void printString(String string) throws TestcaseFailedException;

    void replaceInFile(String content, String regex, String file) throws TestcaseFailedException;

    void refresh();

    String getText(LabelElem label) throws TestcaseFailedException;

    int add(String lh, String rh);

    void sleep(String seconds);

    String execShellCmd(String cmd) throws IOException, InterruptedException;

    String cat(List<Param> params);
}
