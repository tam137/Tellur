package com.selenium.tellur.service;

import com.selenium.tellur.TestcaseFailedException;
import com.selenium.tellur.model.AccessMethode;
import com.selenium.tellur.model.BaseElem;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseService {

    static Logger logger = LoggerFactory.getLogger(BaseService.class);

    @Autowired
    ConfigService configService;

    protected WebElement findWebElement(BaseElem baseElem) throws TestcaseFailedException {
        WebElement webElement;
        try {
            if (baseElem.getAccessMethode().equals(AccessMethode.id)) {
                By by = new By.ById(baseElem.getValue());
                configService.wait.until(ExpectedConditions.presenceOfElementLocated(by));
                webElement = configService.driver.findElement(by);
            } else if (baseElem.getAccessMethode().equals(AccessMethode.xpath)) {
                By by = new By.ByXPath(baseElem.getValue());
                configService.wait.until(ExpectedConditions.presenceOfElementLocated(by));
                webElement = configService.driver.findElement(by);
            } else {
                throw new UnsupportedOperationException("Access Method not Available");
            }

        } catch (NoSuchElementException exception) {
            String errMsg = "Error at url: " + configService.driver.getCurrentUrl();
            logger.error(errMsg);
            throw new TestcaseFailedException(errMsg);
        } catch (TimeoutException exception) {
            String errMsg = "Not able to find "+ baseElem + " at: " + configService.driver.getCurrentUrl();
            logger.error(errMsg);
            throw new TestcaseFailedException(errMsg);
        }
        return webElement;
    }

    protected void clickWebelement(WebElement webElement, BaseElem inputElem) {
        webElement.click();
        logger.debug("Click Webelement " + (inputElem != null ? inputElem.getName() : " ") + webElement);
    }

}
