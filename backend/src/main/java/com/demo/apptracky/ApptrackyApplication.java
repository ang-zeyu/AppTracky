package com.demo.apptracky;

import com.demo.apptracky.config.SsmParameterStoreConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

/*
 Authentication
 - login / logout / signup

 Entities:
 - Application
 -
 */

@SpringBootApplication
@EnableAsync
public class ApptrackyApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(ApptrackyApplication.class)
				.listeners(new SsmParameterStoreConfig())
				.application()
				.run(args);
	}
}
