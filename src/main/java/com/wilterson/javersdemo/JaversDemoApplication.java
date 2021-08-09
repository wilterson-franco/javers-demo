package com.wilterson.javersdemo;

import com.wilterson.javersdemo.repo.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JaversDemoApplication {

	@Autowired
	MerchantRepository merchantRepository;

	public static void main(String[] args) {
		SpringApplication.run(JaversDemoApplication.class, args);
	}
}
