package com.nss.pibblest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic(systemName = "Pibblest", sharedModules = "shared")
@SpringBootApplication
public class PibblestApplication {

	public static void main(String[] args) {
		SpringApplication.run(PibblestApplication.class, args);
	}

}
