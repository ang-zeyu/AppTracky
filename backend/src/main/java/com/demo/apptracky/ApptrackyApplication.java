package com.demo.apptracky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
		SpringApplication.run(ApptrackyApplication.class, args);
	}
}
