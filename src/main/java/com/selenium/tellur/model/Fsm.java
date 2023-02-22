package com.selenium.tellur.model;

import com.selenium.tellur.UnsupportedTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Fsm {

    private final Logger LOGGER = LoggerFactory.getLogger(Fsm.class);

    private final Map<State, Map<BuildInCmds, State>> stateMap = new HashMap<>();

    private State currentState = State.start;

    private State oldState = State.start;


    Fsm() {
        Map <BuildInCmds, State> start = new HashMap<>();
        start.put(BuildInCmds.includeButtons, State.start);
        start.put(BuildInCmds.includeInputs, State.start);
        start.put(BuildInCmds.includeLabels, State.start);
        start.put(BuildInCmds.imp, State.start);
        start.put(BuildInCmds.var, State.end);
        start.put(BuildInCmds.function, State.function);
        start.put(BuildInCmds.testcase, State.testcase);
        stateMap.put(State.start, start);

        Map <BuildInCmds, State> function = new HashMap<>();
        function.put(BuildInCmds.ret, State.end);
        function.put(BuildInCmds.call, State.function);
        function.put(BuildInCmds.click, State.function);
        function.put(BuildInCmds.navigateTo, State.function);
        function.put(BuildInCmds.send, State.function);
        function.put(BuildInCmds.verifyLabelContains, State.function);
        function.put(BuildInCmds.verifyPresenceOf, State.function);
        function.put(BuildInCmds.verifyUrlContains, State.function);
        function.put(BuildInCmds.verifyLabelContainsRex, State.function);
        function.put(BuildInCmds.print, State.function);
        function.put(BuildInCmds.refresh, State.function);
        function.put(BuildInCmds.set, State.function);
        function.put(BuildInCmds.replaceInFile, State.function);
        function.put(BuildInCmds.cat, State.function);
        stateMap.put(State.function, function);

        Map <BuildInCmds, State> testcase = new HashMap<>();
        testcase.put(BuildInCmds.end, State.end);
        testcase.put(BuildInCmds.call, State.testcase);
        testcase.put(BuildInCmds.click, State.testcase);
        testcase.put(BuildInCmds.navigateTo, State.testcase);
        testcase.put(BuildInCmds.send, State.testcase);
        testcase.put(BuildInCmds.verifyLabelContains, State.testcase);
        testcase.put(BuildInCmds.verifyPresenceOf, State.testcase);
        testcase.put(BuildInCmds.verifyUrlContains, State.testcase);
        testcase.put(BuildInCmds.verifyLabelContainsRex, State.testcase);
        testcase.put(BuildInCmds.print, State.testcase);
        testcase.put(BuildInCmds.refresh, State.testcase);
        testcase.put(BuildInCmds.set, State.testcase);
        testcase.put(BuildInCmds.replaceInFile, State.testcase);
        testcase.put(BuildInCmds.cat, State.testcase);
        stateMap.put(State.testcase, testcase);

        Map <BuildInCmds, State> end = new HashMap<>();
        end.put(BuildInCmds.testcase, State.testcase);
        end.put(BuildInCmds.function, State.function);
        end.put(BuildInCmds.var, State.end);
        stateMap.put(State.end, end);
    }

    public State getCurrentState() {
        return currentState;
    }

    public State getOldState() {
        return oldState;
    }

    public boolean stateIs(State state) {
        return state.equals(currentState);
    }

    public void changeState(BuildInCmds token) throws UnsupportedTokenException {
        State oldState = this.currentState;
        State newState = this.stateMap.get(currentState).get(token);
        LOGGER.debug("{} -> {} -> {}", oldState, token, newState);
        if (newState == null) {
            throw new UnsupportedTokenException();
        } else {
            this.currentState = newState;
            this.oldState = oldState;
        }
    }

}
