package com.riancd.io.pulsar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PulsarApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulsarApiApplication.class, args);
	}

}