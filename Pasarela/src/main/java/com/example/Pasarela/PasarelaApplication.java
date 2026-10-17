package com.example.Pasarela;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PasarelaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PasarelaApplication.class, args);
	}

}
