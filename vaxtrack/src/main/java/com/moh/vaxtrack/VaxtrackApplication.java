package com.moh.vaxtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class VaxtrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(VaxtrackApplication.class, args);
	}


}