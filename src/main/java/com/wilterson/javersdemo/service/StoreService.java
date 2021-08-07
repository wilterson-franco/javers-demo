package com.wilterson.javersdemo.service;


import com.wilterson.javersdemo.domain.Product;
import com.wilterson.javersdemo.domain.Store;
import com.wilterson.javersdemo.repo.ProductRepository;
import com.wilterson.javersdemo.repo.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class StoreService {

	private final ProductRepository productRepository;
	private final StoreRepository storeRepository;

	public StoreService(ProductRepository productRepository, StoreRepository storeRepository) {
		this.productRepository = productRepository;
		this.storeRepository = storeRepository;
	}

	public void updateProductPrice(Integer productId, Double price) {
		Optional<Product> productOpt = productRepository.findById(productId);
		productOpt.ifPresent(product -> {
			product.setPrice(price);
			productRepository.save(product);
		});
	}

	public void rebrandStore(int storeId, String updatedName) {
		Optional<Store> storeOpt = storeRepository.findById(storeId);
		storeOpt.ifPresent(store -> {
			store.setName(updatedName);
			store.getProducts().forEach(product -> {
				product.setNamePrefix(updatedName);
			});
			storeRepository.save(store);
		});
	}

	public void createRandomProduct(Integer storeId) {
		Optional<Store> storeOpt = this.storeRepository.findById(storeId);
		storeOpt.ifPresent(store -> {
			Random random = new Random();
			Product product = Product.builder().name("Product#" + random.nextInt()).price(random.nextDouble() * 100).build();
			store.addProduct(product);
			storeRepository.save(store);
		});
	}

	public Store findStoreById(int storeId) {
		return storeRepository.findById(storeId).get();
	}

	public Product findProductById(int id) {
		return this.productRepository.findById(id).get();
	}
}
