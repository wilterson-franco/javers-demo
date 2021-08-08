package com.wilterson.javersdemo.service;


import com.wilterson.javersdemo.domain.Address;
import com.wilterson.javersdemo.domain.Product;
import com.wilterson.javersdemo.domain.Store;
import com.wilterson.javersdemo.repo.ConfigurationStoreRepository;
import com.wilterson.javersdemo.repo.ProductRepository;
import com.wilterson.javersdemo.repo.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ConfigurationStoreService {
	private final ProductRepository productRepository;
	private final ConfigurationStoreRepository configurationStoreRepository;
	private final StoreRepository storeRepository;

	public ConfigurationStoreService(ProductRepository productRepository, ConfigurationStoreRepository configurationStoreRepository, StoreRepository storeRepository) {
		this.productRepository = productRepository;
		this.configurationStoreRepository = configurationStoreRepository;
		this.storeRepository = storeRepository;
	}

	public Store createStore(Store storeWip) {
		storeWip.setStatus("CONFIGURATION");
		storeWip.setGuid(guid());
		storeWip.reparent();
		return configurationStoreRepository.save(storeWip);
	}

	public void update(Integer storeId, Store updatedStore) {
		Store wip = configurationStoreRepository.findById(storeId)
				.orElseThrow(() -> new EntityNotFoundException("Store not found"));

		wip.copyProperties(updatedStore);
		wip.setStatus("CONFIGURATION");
		wip.reparent();

		configurationStoreRepository.save(wip);
	}

	public void checkIn(Integer storeId) {
		Store wip = configurationStoreRepository.findById(storeId)
				.orElseThrow(() -> new EntityNotFoundException("Store not found"));

		if (!ObjectUtils.isEmpty(wip.getLiveStoreId())) {
			Store live = configurationStoreRepository.findById(wip.getLiveStoreId())
					.orElseThrow(() -> new EntityNotFoundException("Live store not found"));

			// TODO: check if wip got in fact changed before changing the live version

			live.copyProperties(wip);
			live.setStatus("LIVE");
			live.setLiveStoreId(null);
			live.getProducts().forEach(liveProd -> liveProd.setLiveProduct(null));
			live.reparent();

			storeRepository.save(live);
			configurationStoreRepository.delete(wip);
		} else {
			wip.setStatus("LIVE");
			storeRepository.save(wip);
		}
	}

	public Store checkOut(Integer storeId) {
		Store live = configurationStoreRepository.findById(storeId)
				.orElseThrow(() -> new EntityNotFoundException("Live store not found"));

		Store wip = Store
				.builder()
				.address(new Address())
				.products(new ArrayList<>())
				.build();

		wip.copyProperties(live);
		wip.setLiveStoreId(live.getId());
		wip.setStatus("CONFIGURATION");
		wip.reparent();

		return configurationStoreRepository.save(wip);
	}

	protected String guid() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 16) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		return salt.toString();
	}
}
