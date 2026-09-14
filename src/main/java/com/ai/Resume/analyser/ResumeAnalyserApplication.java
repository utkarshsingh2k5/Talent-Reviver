package com.ai.Resume.analyser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ResumeAnalyserApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResumeAnalyserApplication.class, args);
	}

}

