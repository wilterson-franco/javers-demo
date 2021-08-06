package com.wilterson.javersdemo;

import com.wilterson.javersdemo.domain.Address;
import com.wilterson.javersdemo.domain.Product;
import com.wilterson.javersdemo.domain.Store;
import com.wilterson.javersdemo.repo.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;

@SpringBootApplication
public class JaversDemoApplication {

	@Autowired
	StoreRepository storeRepository;

	public static void main(String[] args) {
		SpringApplication.run(JaversDemoApplication.class, args);
	}

	@EventListener
	public void appReady(ApplicationReadyEvent event) {

		Store store = Store
				.builder()
				.name("Wilterson store")
				.address(Address
						.builder()
						.address("Some street")
						.postalCode("22222")
						.build())
				.status("CONFIGURATION")
				.products(new ArrayList<>())
				.build();

		store.addProduct(Product
				.builder()
				.name("Product #100")
				.price(100)
				.store(store)
				.build());

		storeRepository.save(store);
	}
}
