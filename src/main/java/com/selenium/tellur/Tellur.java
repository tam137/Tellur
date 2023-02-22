package com.selenium.tellur;

import com.selenium.tellur.model.FileType;
import com.selenium.tellur.model.Testcase;
import com.selenium.tellur.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class Tellur {

	public static void main(String[] args) {

		final Logger LOGGER = LoggerFactory.getLogger(Tellur.class);

		try {
			SpringApplication.run(Tellur.class, args);
			Parser parser = new Parser().findAndReadTestcaseFile(FileType.Main);
			List<Testcase> testcaseList = new RunServiceImpl(parser, List.of(args)).playCmds();
			new ReportBuilderImpl(testcaseList).printReport();
			System.exit(0);
		} catch (RuntimeException e) {
			LOGGER.error("Fatal error", e);
			System.exit(99);
		} catch (Exception e) {
			LOGGER.error("Fatal error", e);
			System.exit(1);
		}
	}

}
