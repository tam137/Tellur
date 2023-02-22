package com.selenium.tellur;

import com.selenium.tellur.model.*;
import com.selenium.tellur.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {

    private final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private final ConfigService configService = BeanUtil.getBean(ConfigService.class);

    private final Fsm stateMachine = BeanUtil.getBean(Fsm.class);

    private final List<InputElem> inputElemList = new ArrayList<>();

    private final List<ButtonElem> buttonElemList = new ArrayList<>();

    private final List<LabelElem> labelElemList = new ArrayList<>();

    private final List<Testcase> testcaseList = new ArrayList<>();

    private final Map<String, List<CommandElem>> functionMap = new HashMap<>();

    private final Map<String, Param> varMap = new HashMap<>();

    private final Map<FileType,List<String>> importFilesMap = new HashMap<>();

    private final Map<Path, Integer> lineNrMap = new HashMap<>();

    private Boolean flagReadWebDefFiles = false;

    private String line;

    private int lineNr = 0;

    private Stack<String> currentFileToParse = new Stack<>();

    public List<InputElem> getInputElemList() {
        return inputElemList;
    }

    public List<ButtonElem> getButtonElemList() {
        return buttonElemList;
    }

    public List<LabelElem> getLabelElemList() {
        return labelElemList;
    }

    public Map<String, List<CommandElem>> getFunctionMap() {
        return functionMap;
    }

    public Map<String, Param> getVarMap() {
        return varMap;
    }

    public List<Testcase> getTestcaseList() {
        return testcaseList;
    }

    public Parser() {
        this.importFilesMap.put(FileType.LabelFile, new ArrayList<>());
        this.importFilesMap.put(FileType.ButtonFile, new ArrayList<>());
        this.importFilesMap.put(FileType.InputFile, new ArrayList<>());
    }

    void findAndReadInputFieldsFile(String path) throws IOException {
        Files.readAllLines(Paths.get(path)).forEach(line -> {
            if (line.startsWith("#")) {
                // is comment, ignore..
            } else if (line.isEmpty()) {
                // empty, ignore..
            } else {
                addBaseElemToList(new InputElem(
                    line.split("\\.")[0],
                    AccessMethode.valueOf(line.split("\\.")[1].split("=")[0]),
                    line.substring(line.replaceAll("\"", "\\\"").indexOf('=')+1)));
            }
        });
    }

    void findAndReadButtonFile(String path) throws IOException {
        Files.readAllLines(Paths.get(path)).forEach(line -> {
            if (line.startsWith("#")) {
                // is comment, ignore..
            } else if (line.isEmpty()) {
                // empty, ignore..
            } else {
                addBaseElemToList(new ButtonElem(
                    line.split("\\.")[0],
                    AccessMethode.valueOf(line.split("\\.")[1].split("=")[0]),
                    line.substring(line.replaceAll("\"", "\\\"").indexOf('=')+1)));
            }
        });
    }

    void findAndReadLabelFile(String path) throws IOException {
        try {
            Files.readAllLines(Paths.get(path)).forEach(line -> {
                if (line.startsWith("#")) {
                    // is comment, ignore..
                } else if (line.isEmpty()) {
                    // empty, ignore..
                } else {
                    addBaseElemToList(new LabelElem(line.split("\\.")[0], AccessMethode.valueOf(line.split("\\.")[1].split("=")[0]),
                        line.substring(line.replaceAll("\"", "\\\"").indexOf('=') + 1)));
                }
            });
        } catch (NoSuchFileException e) {
            throw new NoSuchFileException("Webdefinition file " + path + " not found");
        }
    }

    public Parser findAndReadTestcaseFile(FileType fileType) throws IOException {
        return findAndReadTestcaseFile(configService.getTestplanName(), fileType);
    }

    public Parser findAndReadTestcaseFile(String uri, FileType fileType) throws IOException {
        String currentFunctionName = null;
        Testcase testcase = null;

        String fileDir = fileType == FileType.Main ? configService.getTestplanDir() : configService.getLibDir();
        Path path = Path.of(fileDir + "/" + Paths.get(uri));
        lineNrMap.putIfAbsent(path.getFileName(), 0);
        this.lineNr = lineNrMap.get(path.getFileName());
        this.currentFileToParse.push(String.valueOf(path.getFileName()));

        if (!Files.isRegularFile(path)) {
            LOGGER.error("No such file {}", fileDir + "/" + path.getFileName());
            throw new RuntimeException();
        }

        Files.readAllLines(path).forEach( line -> {
            if (line.startsWith("function")) {
                String functionName = line.split(" ")[1].trim();
                if (functionMap.containsKey(functionName))
                    throwUnexpectedTokenError(functionName +  " already defined");
                this.functionMap.put(functionName, new ArrayList<>());
            }
        });

        for (String l : Files.readAllLines(path)) {
            this.lineNr++;
            this.lineNrMap.put(path.getFileName(), this.lineNr);
            final String buf = l.trim();
            if (buf.contains("#")) {
                line = buf.substring(0, buf.indexOf("#"));
            } else {
                line = buf;
            }
            if (line.isEmpty()) continue;
            line = line.trim();
            CommandElem  cmdElem = null;

            if (stateMachine.getOldState().equals(State.start) &&
                !stateMachine.getCurrentState().equals(State.start) && !this.flagReadWebDefFiles) {
                    LOGGER.info("Read Webdefinition-Files");
                    findAndReadWebDefintionFiles();
                    this.flagReadWebDefFiles = true;
            }

            try {
                if (test(BuildInCmds.testcase, State.all)) {
                    testcase = generateTestcase(line);
                } else if (test(BuildInCmds.includeButtons, State.all) && fileType.equals(FileType.Main)) {
                    generateIncludeStatement(line);
                } else if (test(BuildInCmds.includeInputs, State.all) && fileType.equals(FileType.Main)) {
                    generateIncludeStatement(line);
                } else if (test(BuildInCmds.includeLabels, State.all) && fileType.equals(FileType.Main)) {
                    generateIncludeStatement(line);
                } else if (test(BuildInCmds.end, State.all)) {
                    this.testcaseList.add(testcase);
                } else if (test(BuildInCmds.ret, State.all)) {
                    // return cmd pops function stack and set return value
                    cmdElem = generateCommandElem(line);
                    this.functionMap.get(currentFunctionName).add(cmdElem);
                } else if (test(BuildInCmds.function, State.all)) {
                    currentFunctionName = line.split(" ")[1];
                } else if (test(BuildInCmds.var, State.all)) {
                    generateGlobalVariable(line);
                } else if (test(BuildInCmds.imp, State.all)) {
                    cmdElem = generateCommandElem(line);
                    findAndReadTestcaseFile(cmdElem.getParams().get(0).getValue() + ".wtl", FileType.Libary);
                    this.lineNr = lineNrMap.get(path.getFileName());
                    this.currentFileToParse.pop();
                    // check for build in function call
                } else if (Arrays.stream(BuildInCmds.values())
                            .map(Enum::toString)
                            .anyMatch(v -> v.equals(line.split("\\(")[0].trim()))) {
                    cmdElem = generateCommandElem(line);
                } else if (test(BuildInCmds.call, State.all)) { // is a self defined function command
                    cmdElem = generateFunctionReference(line);
                } else {
                    throwUnexpectedTokenError("<" + line.split("\\(")[0] + "> not known");
                }
                    // is created cmdElem for function or Testcase?
                if (cmdElem != null) {
                    if (stateMachine.getCurrentState().equals(State.function)) {
                        this.functionMap.get(currentFunctionName).add(cmdElem);
                    } else if (stateMachine.getCurrentState().equals(State.testcase)) {
                        assert testcase != null;
                        testcase.addCommand(cmdElem);
                    }
                }
            } catch (UnsupportedTokenException e) {
                throwUnexpectedTokenError(null);
            }
        }
        return this;
    }

    private CommandElem generateCommandElem(String line) {
        List<Param> paramList = getParameterList(line);
        // try to build Cmd Token. If not possible its function call -> so BuildInCmds.call
        BuildInCmds buildInCmds;
        try {
            buildInCmds = BuildInCmds.valueOf(line.split("\\(")[0].trim());
        } catch (IllegalArgumentException e) {
            return new CommandElem(this.lineNr, BuildInCmds.call, paramList);
        }
        return new CommandElem(this.lineNr, buildInCmds, paramList);
    }

    private List<Param> getParameterList(String line) {
        String paramString = null;
        try {
            paramString = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')')).trim();
        } catch (StringIndexOutOfBoundsException e) {
            throwUnexpectedTokenError("param list wrong or missing");
        }
        List<Integer> splitterPositions = new ArrayList<>();
        boolean withinParam = false;
        int openBracket = 0;
        for (int i = 0; i < Objects.requireNonNull(paramString).length(); i++) {
            if (paramString.charAt(i) == ',' && !withinParam && openBracket == 0) {
                splitterPositions.add(i);
            }
            if (paramString.charAt(i) == '"') {
                withinParam = !withinParam;
            }
            if (paramString.charAt(i) == '(') ++openBracket;
            if (paramString.charAt(i) == ')') --openBracket;
        }
        List<String> rawResultList = new ArrayList<>();
        int lastSplitPosition = 0;
        for (int splitPos : splitterPositions) {
            rawResultList.add(paramString.substring(lastSplitPosition, splitPos).trim());
            lastSplitPosition = splitPos + 1;
        }
        if (!paramString.isEmpty()) {
            rawResultList.add(paramString.substring(lastSplitPosition).trim());
        }

        List<Param> result = new ArrayList<>();
        for (String rawResult : rawResultList) {
            if (rawResult.matches("^\".*\\s*\"")) {
                result.add(new Param(rawResult.replaceAll("\"", ""), Param.Type.string));
            } else if (rawResult.matches("\\d+")) {
                result.add(new Param(rawResult, Param.Type.number));
            } else if (rawResult.matches(configService.getRegexWebelemName())) {
                if (!webElemExists(rawResult)) throwUnexpectedTokenError("Webelem <" + rawResult + "> not defined");
                result.add(new Param(rawResult.trim(), Param.Type.webelem));
            } else if (rawResult.matches(configService.getRegexVarUnresolved())) {
                result.add(new Param(rawResult.trim(), Param.Type.var_unresolved));
            } else if (rawResult.matches(configService.getRegexVarName())) {
                if (!variableExists(rawResult.trim())) throwUnexpectedTokenError("Variable <" + rawResult + "> not defined");
                result.add(new Param(rawResult, Param.Type.var_name));
            } else if (rawResult.matches(configService.getRegexFunctionSignature())) {
                result.add(generateFunctionReference(rawResult));
            } else {
                throwUnexpectedTokenError("Param <" + rawResult + "> type can not be determinated");
            }
        }
        return result;
    }

    private Param generateVarValueFromLine(String line) {
        String varValueRaw = line.substring(line.indexOf("=") + 1).trim();
        if (varValueRaw.matches("\\d+")) {
            return new Param(varValueRaw, Param.Type.number);
        } else if (varValueRaw.matches("^\\\"[\\w+|\\s+|/+|:+|\\-+|\\d|.+|!+|?+]+\\\"$")) {
            return new Param(varValueRaw.replace("\"", ""), Param.Type.string);
        } else {
            throwUnexpectedTokenError("Var Type needs to be String or Number");
        }
        return null;
    }

    private FunctionReference generateFunctionReference(String line) {
        String funcRefName = line.split("\\(")[0].trim();
        CommandElem commandElem = generateCommandElem(line);
        return new FunctionReference(commandElem, funcRefName);
    }

    private boolean test(BuildInCmds token, State state) throws UnsupportedTokenException {
        String tokenString = String.valueOf(token);
        if (token.equals(BuildInCmds.call)) {
            if (line.matches(configService.getRegexFunctionSignature())) {
                if (!this.functionMap.containsKey(line.split("\\(")[0].trim())) {
                    throwUnexpectedTokenError("Function not defined");
                }
                return true;
            }
        } else if (token.equals(BuildInCmds.imp)) {
            if (line.startsWith("import"))  {
                tokenString = "import";
            }
        }
        if (line.startsWith(tokenString) && (state == State.all || state == stateMachine.getCurrentState())) {
            stateMachine.changeState(token);
            return true;
        } else {
            return false;
        }
    }

    private void generateGlobalVariable(String line) {
        // test if var is assigned
        if (line.contains("=")) {
            String varName = line.split("=")[0].trim().split("var")[1].trim();
            if (varMap.get(varName) == null) {
                this.varMap.put(varName, generateVarValueFromLine(line));
            } else {
                throwUnexpectedTokenError(varName + " already used");
            }
        } else {
            String varName = line.substring(line.indexOf("var") + 3 ).trim();
            if (varMap.get(varName) == null) {
                this.varMap.put(varName, null);
            } else {
                throwUnexpectedTokenError(varName + " already used");
            }
        }

    }

    private Testcase generateTestcase(String line) {
        String regex = "testcase\\s+.+$";
        if (!line.matches(regex)) throwUnexpectedTokenError(regex + "not match");
        String testcaseName = line.split(" ")[1].trim();
        if (this.testcaseList.stream().map(Testcase::getName).anyMatch(name -> name.equals(testcaseName))) {
            throwUnexpectedTokenError("Name " + testcaseName + " already in use");
        }
        return new Testcase(testcaseName);
    }

    private void findAndReadWebDefintionFiles() throws IOException {
        String testPlanDir = configService.getTestplanDir() + "/";
        try {
            for (String labelFile : this.importFilesMap.get(FileType.LabelFile)) {
                findAndReadLabelFile(testPlanDir + labelFile);
            }
            for (String buttonFile : this.importFilesMap.get(FileType.ButtonFile)) {
                findAndReadButtonFile(testPlanDir + buttonFile);
            }
            for (String inputFile : this.importFilesMap.get(FileType.InputFile)) {
                findAndReadInputFieldsFile(testPlanDir + inputFile);
            }
        } catch (NoSuchFileException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException();
        }

    }

    private void generateIncludeStatement(String line) {
        if (line.matches("includeLabels\\s\\\"[\\w|\\.\\_]+\\\"")) {
            if (this.importFilesMap.get(FileType.LabelFile).contains(line.split("\"")[1])) {
                throwUnexpectedTokenError(line.split("\"")[1] + " already imported"); }
            this.importFilesMap.get(FileType.LabelFile).add(line.split("\"")[1]);
        } else if (line.matches("includeButtons\\s\\\"[\\w|\\.\\_]+\\\"")) {
            if (this.importFilesMap.get(FileType.ButtonFile).contains(line.split("\"")[1])) {
                throwUnexpectedTokenError(line.split("\"")[1] + " already imported"); }
            this.importFilesMap.get(FileType.ButtonFile).add(line.split("\"")[1]);
        } else if (line.matches("includeInputs\\s\\\"[\\w|\\.\\_]+\\\"")) {
            if (this.importFilesMap.get(FileType.InputFile).contains(line.split("\"")[1])) {
                throwUnexpectedTokenError(line.split("\"")[1] + " already imported"); }
            this.importFilesMap.get(FileType.InputFile).add(line.split("\"")[1]);
        } else {
            throwUnexpectedTokenError(null);
        }
    }

    private void addBaseElemToList(BaseElem elem) throws UnsupportedOperationException {
        if (elem instanceof InputElem) {
            if (inputElemList.stream().map(BaseElem::getName).noneMatch(e -> e.equals(elem.getName()))) {
                inputElemList.add((InputElem) elem);
            } else throwUnexpectedTokenError("already defined");
        } else if (elem instanceof LabelElem) {
            if (labelElemList.stream().map(BaseElem::getName).noneMatch(e -> e.equals(elem.getName()))) {
                labelElemList.add((LabelElem) elem);
            } else throwUnexpectedTokenError("already defined");
        } else if (elem instanceof ButtonElem) {
            if (buttonElemList.stream().map(BaseElem::getName).noneMatch(e -> e.equals(elem.getName()))) {
                buttonElemList.add((ButtonElem) elem);
            } else throwUnexpectedTokenError("already defined");
        } else {
            throw new UnsupportedOperationException();
        }
        LOGGER.info("Added Element [" + elem + "]");
    }

    private boolean webElemExists(String name) {
        Stream<BaseElem> webElems = Stream.concat(this.inputElemList.stream(), this.labelElemList.stream());
        List<String> webElemsNameList = Stream.concat(webElems, this.buttonElemList.stream())
            .map(BaseElem::getName)
            .collect(Collectors.toList());
        return webElemsNameList.contains(name);
    }

    private boolean variableExists(String name) {
        return this.varMap.keySet().stream().anyMatch(k -> k.equals(name));
    }

    private void throwUnexpectedTokenError(String msg) {
        if (true) {
            LOGGER.error("Unexpected Token <{}> in File <{}> line <{}> {}",
                this.line,
                this.currentFileToParse.peek(),
                this.lineNr,
                msg);
        } else {
            LOGGER.error(msg);
        }
        System.exit(0);
    }

}
