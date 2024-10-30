package com.heejuk.tuddyfuddy.contextservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.*;

@SpringBootApplication
@EnableDiscoveryClient
public class ContextServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContextServiceApplication.class, args);
	}

}
