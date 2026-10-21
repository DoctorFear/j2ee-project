package com.futabus.transport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransportServiceApplication.class, args);
        System.out.println("transport-service running...");
	}

}
