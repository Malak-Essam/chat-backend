package com.malak.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChatappBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatappBackendApplication.class, args);
	}

}
