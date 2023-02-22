package com.selenium.tellur.service;

import com.selenium.tellur.ShellService;
import com.selenium.tellur.TestcaseFailedException;
import com.selenium.tellur.model.ButtonElem;
import com.selenium.tellur.model.InputElem;
import com.selenium.tellur.model.LabelElem;
import com.selenium.tellur.model.Param;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CmdServiceImpl extends BaseService implements CmdService {

    ConfigService configService;

    AssertService assertService;

    ShellService shellService;

    CmdServiceImpl(ConfigService configService, AssertService assertService, ShellService shellService) {
        this.configService = configService;
        this.assertService = assertService;
        this.shellService = shellService;
    }

    static Logger logger = LoggerFactory.getLogger(CmdService.class);

    public void click(ButtonElem button) throws TestcaseFailedException {
        WebElement webElement = findWebElement(button);
        clickWebelement(webElement, button);
    }

    public void sendKeys(InputElem inputElem, String input) throws TestcaseFailedException {
        WebElement webElement = findWebElement(inputElem);
        webElement.sendKeys(input);
    }

    public void navigateTo(String url) throws TestcaseFailedException {
        configService.driver.navigate().to(url);
        assertService.checkUrl(url);
    }

    @Override
    public void printLabelText(LabelElem labelElem) throws TestcaseFailedException {
        WebElement webElement = findWebElement(labelElem);
        System.out.println(webElement.getText());
    }

    @Override
    public void printString(String string) throws TestcaseFailedException {
        System.out.println(string);
    }

    @Override public void replaceInFile(String content, String regex, String file) throws TestcaseFailedException {
        Path path = Paths.get(file);
        File textFile = path.toFile();
        try {
            String data = FileUtils.readFileToString(textFile, "ISO-8859-1");
            data = data.replaceAll(regex, content);
            FileUtils.writeStringToFile(textFile, data, "ISO-8859-1");
        } catch (IOException e) {
            throw new TestcaseFailedException("Not able to find file " + file);
        }
    }

    @Override
    public void refresh() {
        configService.driver.navigate().refresh();
    }

    @Override
    public String getText(LabelElem label) throws TestcaseFailedException {
        WebElement webElement = findWebElement(label);
        return webElement.getText();
    }

    @Override
    public int add(String lh, String rh) {
        return Integer.parseInt(lh) + Integer.parseInt(rh);
    }

    @Override public void sleep(String seconds) {
        try {
            Thread.sleep(Integer.parseInt(seconds) * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);        }
    }

    @Override public String execShellCmd(String cmd) throws IOException, InterruptedException {
        return shellService.exec(cmd, configService.getTestplanDir());
    }

    @Override public String cat(List<Param> params) {
        return params.stream().map(Param::getValue).collect(Collectors.joining());
    }

}
