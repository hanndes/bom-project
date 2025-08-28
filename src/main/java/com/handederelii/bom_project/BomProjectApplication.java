package com.handederelii.bom_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.handederelii.bom_project.repositories")
@EntityScan(basePackages = "com.handederelii.bom_project.entity")
public class BomProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BomProjectApplication.class, args);
	}

}
