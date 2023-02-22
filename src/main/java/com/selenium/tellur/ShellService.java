package com.selenium.tellur;

import com.selenium.tellur.service.ConfigService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class ShellService {

    private final ConfigService configService;

    ShellService(ConfigService configService) {
        this.configService = configService;
    }

    public String exec(String cmd, String testPlanDir) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[]{configService.getShell() , "-c", "cd " + testPlanDir +
            " && " + cmd});
        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder ret = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            ret.append(line).append('\n');
        }
        return ret.toString();
    }

}
