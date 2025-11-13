package com.example.ReadMark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.ReadMark")
@EnableScheduling
public class ReadMarkApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadMarkApplication.class, args);
	}

}
