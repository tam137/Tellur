package com.selenium.tellur.service;


import com.selenium.tellur.TestcaseFailedException;
import com.selenium.tellur.model.BaseElem;
import com.selenium.tellur.model.LabelElem;

public interface AssertService {

    void labelContent(LabelElem labelElem, String content) throws TestcaseFailedException;

    void labelContentRex(LabelElem labelElem, String regex) throws TestcaseFailedException;

    void existenceOf(BaseElem baseElem) throws TestcaseFailedException;

    void checkUrl(String checkurl) throws TestcaseFailedException;

}
