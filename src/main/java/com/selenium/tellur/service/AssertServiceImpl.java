package com.selenium.tellur.service;

import com.selenium.tellur.TestcaseFailedException;
import com.selenium.tellur.model.BaseElem;
import com.selenium.tellur.model.LabelElem;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AssertServiceImpl extends BaseService implements AssertService {

    static Logger logger = LoggerFactory.getLogger(AssertService.class);

    @Override
    public void labelContent(LabelElem labelElem, String content) throws TestcaseFailedException {
        WebElement webElement = this.findWebElement(labelElem);
        if (webElement.getText().contains(content)) {
            logger.info("Test <" + content + "> on " + labelElem);
        } else {
            String errMsg = "Expected <" + content + "> but found <" + webElement.getText() + "> on " + labelElem;
            logger.error(errMsg);
            throw new TestcaseFailedException(errMsg);
        }
    }

    @Override
    public void labelContentRex(LabelElem labelElem, String regex) throws TestcaseFailedException {
        WebElement webElement = this.findWebElement(labelElem);
        if (webElement.getText().matches(regex)) {
            logger.info("Test <" + regex + "> on " + labelElem);
        } else {
            String errMsg = "Expected regex <" + regex + "> but found <" + webElement.getText() + "> on " + labelElem;
            logger.error(errMsg);
            throw new TestcaseFailedException(errMsg);
        }
    }

    @Override
    public void existenceOf(BaseElem baseElem) throws TestcaseFailedException {
        findWebElement(baseElem);
    }

    @Override
    public void checkUrl(String checkurl) throws TestcaseFailedException {
        try {
            configService.wait.until(ExpectedConditions.urlContains(checkurl));
        } catch (TimeoutException e) {
            String errMsg = "URL not match - Expected: <" + checkurl + "> but was " + configService.driver.getCurrentUrl();
            logger.error(errMsg);
            throw new TestcaseFailedException(errMsg);
        }
        logger.debug("Url check ok");
    }

}
