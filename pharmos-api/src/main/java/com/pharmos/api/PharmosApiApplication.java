package com.pharmos.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.pharmos")
public class PharmosApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PharmosApiApplication.class, args);
	}

}
