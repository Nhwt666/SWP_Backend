package com.group2.ADN;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdnApplication {
	public static void main(String[] args) {
		SpringApplication.run(AdnApplication.class, args);
	}
}