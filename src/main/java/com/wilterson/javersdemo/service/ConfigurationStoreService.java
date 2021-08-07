package com.wilterson.javersdemo.service;


import com.wilterson.javersdemo.domain.Store;
import com.wilterson.javersdemo.domain.StoreWip;
import com.wilterson.javersdemo.repo.ConfigurationStoreRepository;
import com.wilterson.javersdemo.repo.ProductRepository;
import com.wilterson.javersdemo.repo.StoreRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.Random;

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

	public Store createStore(StoreWip storeWip) {
		storeWip.setStatus("CONFIGURATION");
		storeWip.setGuid(guid());
		storeWip.reparent();
		return configurationStoreRepository.save(storeWip);
	}

	public void checkIn(String guid) {

		StoreWip storeWip = configurationStoreRepository
				.findByLiveStoreGuid(guid)
				.orElseThrow(() -> new EntityNotFoundException("Store " + guid + " not found"));

		storeRepository.findByGuid(storeWip.getLiveStoreGuid())
				.ifPresent(store -> {
					BeanUtils.copyProperties(storeWip, store);
					store.setStatus("LIVE");
					storeRepository.save(store);
					configurationStoreRepository.delete(storeWip);
				});
	}

	protected String guid() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 16) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
//		return saltStr;
		return "ABCDEFGHIJ";
	}
}
