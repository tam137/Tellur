package com.selenium.tellur.service;

import com.selenium.tellur.*;
import com.selenium.tellur.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.stream.Stream;

public class RunServiceImpl {

    private final Logger LOGGER = LoggerFactory.getLogger(RunServiceImpl.class);

    private final CmdService cmdService = BeanUtil.getBean(CmdServiceImpl.class);

    private final AssertService assertService = BeanUtil.getBean(AssertServiceImpl.class);

    private final List<InputElem> inputElemList;

    private final List<ButtonElem> buttonElemList;

    private final List<LabelElem> labelElemList;

    private final List<Testcase> testcaseList;

    private final Map<String, List<CommandElem>> functionMap;

    private final Map<String, Param> varMap;

    private Stack<FunctionReference> funcRefStack = new Stack<>();

    private final Stack<Param> returnValueStack = new Stack<>();

    private final List<String> args;

    static int functionLevel = 0;

    public RunServiceImpl(Parser parser, List<String> args) {
        inputElemList = parser.getInputElemList();
        buttonElemList = parser.getButtonElemList();
        labelElemList = parser.getLabelElemList();
        testcaseList = parser.getTestcaseList();
        functionMap = parser.getFunctionMap();
        varMap = parser.getVarMap();
        this.args = args;
    }

    public List<Testcase> playCmds() {
        resolveArgsToVars();
        for (Testcase testcase : this.testcaseList) {
            LOGGER.info("Run Testcase " + testcase.getName());
            try {
                runCommandList(testcase.getCommandLineList());
                LOGGER.info("Testcase " + testcase.getName() + " passed");
                testcase.setPassed();
            } catch (TestcaseFailedException e) {
                LOGGER.error("Testcase " + testcase.getName() + " failed: ");
                testcase.setMsg(e.getMsg());
                testcase.setFailed();
            } catch (NullPointerException e) {
                LOGGER.error("Fatal Error, exit", e);
                testcase.setMsg(e.getMessage());
                testcase.setFailed();
            } catch (VarNotFoundException e) {
                LOGGER.error("Var not found, exit", e);
                testcase.setMsg(e.getMessage());
                testcase.setFailed();
            }
            functionLevel = 0;
            this.funcRefStack = new Stack<>();
        }
        return this.testcaseList;
    }

    private void runCommandList(List<CommandElem> commandElems)
        throws TestcaseFailedException, NullPointerException, VarNotFoundException {
        for (CommandElem cmd : commandElems) {
            replaceVariables(cmd);
            runCommandElement(cmd);
        }
    }

    private <T extends Param> void runFunctionReference(T func) throws VarNotFoundException, TestcaseFailedException {
        if (func instanceof FunctionReference) {
            FunctionReference funcCopy = new FunctionReference((FunctionReference) func);
            replaceVariables(funcCopy);
            this.funcRefStack.push(funcCopy);
            String functionName = ((FunctionReference) func).getTargetFunctionName();
            LOGGER.info("Call function {}", functionName);
            if (this.functionMap.containsKey(functionName)) {
                List<CommandElem> cmdList = this.functionMap.get(functionName);
                runCommandList(cmdList);
            } else {
                ((CommandElem) func).setCmd(((FunctionReference) func).getTargetFunctionName());
                runCommandElement((CommandElem) func);
            }

        } else {
            throw new RuntimeException();
        }
    }

    private void resolveParameterFunctionCalls(CommandElem cmd) throws TestcaseFailedException, VarNotFoundException {
        for (int i = 0; i < cmd.getParams().size(); i++) {
            Param param = cmd.getParams().get(i);
            if (param.isFunctionReference()) {
                runFunctionReference((CommandElem) param);
                Param returnParam = returnValueStack.pop();
                cmd.getParams().set(i, returnParam);
            } else {
                // go ahead
            }
        }
    }

    private void runCommandElement(CommandElem cmd)
        throws TestcaseFailedException, NullPointerException, VarNotFoundException {
        if (cmd.containsOneOrMoreFunctionParams()) {
            resolveParameterFunctionCalls(cmd);
        }
        replaceVariables(cmd); // TODO not sure why this is here necessary
        LOGGER.info("Run CMD: {}", cmd);
        try {
            switch (cmd.getCmd()) {
                case click:
                    cmdService.click(getButton(cmd.getParams().get(0).getValue()));
                    break;
                case send:
                    cmdService.sendKeys(getInput(cmd.getParams().get(0).getValue()),
                        cmd.getParams().get(1).getValue());
                    break;
                case navigateTo:
                    cmdService.navigateTo(cmd.getParams().get(0).getValue());
                    break;
                case verifyLabelContains:
                    assertService.labelContent(getLabel(cmd.getParams().get(0).getValue()),
                        cmd.getParams().get(1).getValue());
                    break;
                case verifyLabelContainsRex:
                    assertService.labelContentRex(getLabel(cmd.getParams().get(0).getValue()),
                        cmd.getParams().get(1).getValue());
                    break;
                case verifyPresenceOf:
                    assertService.existenceOf(getBaseElem(cmd.getParams().get(0).getValue()));
                    break;
                case verifyUrlContains:
                    assertService.checkUrl(cmd.getParams().get(0).getValue());
                    break;
                case print:
                    if (cmd.getParams().get(0).getType().equals(Param.Type.string) ||
                        cmd.getParams().get(0).getType().equals(Param.Type.number)) {
                        cmdService.printString(cmd.getParams().get(0).getValue().replaceAll("\"", ""));
                    } else if (cmd.getParams().get(0).getType().equals(Param.Type.webelem)) {
                        cmdService.printLabelText(getLabel(cmd.getParams().get(0).getValue()));
                    } else {
                        throw new WrongParamTypeException("Expected: [string|number|label]");
                    }
                    break;
                case refresh:
                    cmdService.refresh();
                    break;
                case replaceInFile:
                    cmdService.replaceInFile(cmd.getParams().get(0).getValue(),
                        cmd.getParams().get(1).getValue(),
                        cmd.getParams().get(2).getValue());
                    break;
                case set:
                    if (cmd.getParams().get(0).getType().equals(Param.Type.var_name) &&
                        (cmd.getParams().get(1).getType().equals(Param.Type.number) ||
                            cmd.getParams().get(1).getType().equals(Param.Type.string))) {
                        this.varMap.put(cmd.getParams().get(0).getValue(), cmd.getParams().get(1));
                    } else {
                        throw new WrongParamTypeException();
                    }
                    break;
                case add:
                    int result = cmdService.add(cmd.getParams().get(0).getValue(), cmd.getParams().get(1).getValue());
                    returnValueStack.push(new Param(Integer.toString(result), Param.Type.number));
                    break;
                case sleep:
                    if (cmd.getParams().get(0).getType().equals(Param.Type.number)) {
                        cmdService.sleep(cmd.getParams().get(0).getValue());
                    } else {
                        throw new WrongParamTypeException();
                    }
                    break;
                case ret:
                    returnValueStack.push(cmd.getReturnValue());
                    break;
                case call:
                    runFunctionReference(cmd);
                    break;
                case sh:
                    String shellResult = cmdService.execShellCmd(cmd.getParams().get(0).getValue());
                    returnValueStack.push(new Param(shellResult, Param.Type.string));
                    break;
                case cat:
                    String catReturn = cmdService.cat(cmd.getParams());
                    returnValueStack.push(new Param(catReturn, Param.Type.string));
                    break;
                default:
                    throw new UnsupportedOperationException("No such Command");
            }
        } catch (NoSuchElementException e) {
            String msg = "WebElem <" + cmd.getParams().get(0).getValue() + "> not defined";
            LOGGER.error(msg);
            throw new TestcaseFailedException(msg);
        } catch (WrongParamTypeException e) {
            String msg = "Type mismatch for " + cmd.getParams().toString() + " in line " + cmd.getLineNr();
            LOGGER.error(msg);
            LOGGER.error(e.getMessage());
            throw new TestcaseFailedException(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<BaseElem> getBaseElemList() {
        return Stream.concat(Stream.concat(inputElemList.stream(), buttonElemList.stream()), labelElemList.stream());
    }

    private void replaceVariables(CommandElem commandElem) throws TestcaseFailedException {
        for (int i = 0; i < commandElem.getParams().size(); i++) {
            if (commandElem.getParams().get(i).getType().equals(Param.Type.var_unresolved)) {
                String varName = commandElem.getParams().get(i).getValue().substring(2);
                if (varName.matches("\\d+")) { // function param eg $$0, $$1
                    Param value;
                    try {
                        value = funcRefStack.peek().getParams().get(Integer.parseInt(varName));
                    } catch (IndexOutOfBoundsException e) {
                        LOGGER.warn("function Param not available");
                        return;
                    }
                    commandElem.getParams().set(i, value);
                } else { // unresolved eg. global vars like $$varName
                    Param value = varMap.get(varName);
                    if (value != null) {
                        commandElem.getParams().set(i, value);
                    } else { // assign WebElem-LabelText as var Value
                        try {
                            commandElem.getParams().
                                set(i, new Param(cmdService.getText(getLabel(varName)), Param.Type.string));
                        } catch (NoSuchElementException e) {
                            LOGGER.error("{} not defined (Command line params are $$__arg_0)", varName);
                            throw new TestcaseFailedException(varName + " not defined");
                        }
                    }
                }
            }
        }
    }

    private ButtonElem getButton(String name) {
        return this.buttonElemList.stream().filter(elem -> elem.getName().equals(name)).findFirst().orElseThrow();
    }

    private LabelElem getLabel(String name) {
        return this.labelElemList.stream().filter(elem -> elem.getName().equals(name)).findFirst().orElseThrow();
    }

    private InputElem getInput(String name) {
        return this.inputElemList.stream().filter(elem -> elem.getName().equals(name)).findFirst().orElseThrow();
    }

    private BaseElem getBaseElem(String name) {
        return Stream.concat
                (Stream.concat(this.inputElemList.stream(), this.buttonElemList.stream()), this.labelElemList.stream())
            .filter(elem -> elem.getName().equals(name)).findFirst().orElseThrow();
    }

    private void resolveArgsToVars() {
        for (int i = 0; i < this.args.size() && i < 9; i++) {
            String varName = "__arg_" + i;
            this.varMap.put(varName, new Param(this.args.get(i), Param.Type.string, true));
        }
    }

}
