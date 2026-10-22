package com.futabus.transport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TransportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransportServiceApplication.class, args);
        System.out.println("transport-service running...");
	}

}
