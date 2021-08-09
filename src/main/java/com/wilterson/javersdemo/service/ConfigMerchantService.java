package com.wilterson.javersdemo.service;


import com.wilterson.javersdemo.domain.Address;
import com.wilterson.javersdemo.domain.Merchant;
import com.wilterson.javersdemo.repo.ConfigurationMerchantRepository;
import com.wilterson.javersdemo.repo.MerchantRepository;
import com.wilterson.javersdemo.repo.SearchParameterRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Random;

@Service
public class ConfigMerchantService {
	private final SearchParameterRepository SearchParameterRepository;
	private final ConfigurationMerchantRepository configurationMerchantRepository;
	private final MerchantRepository merchantRepository;

	public ConfigMerchantService(SearchParameterRepository SearchParameterRepository, ConfigurationMerchantRepository configurationMerchantRepository, MerchantRepository merchantRepository) {
		this.SearchParameterRepository = SearchParameterRepository;
		this.configurationMerchantRepository = configurationMerchantRepository;
		this.merchantRepository = merchantRepository;
	}

	public Merchant createMerchant(Merchant merchantWip) {
		merchantWip.setStatus("CONFIGURATION");
		merchantWip.setGuid(guid());
		merchantWip.reparent();
		return configurationMerchantRepository.save(merchantWip);
	}

	public void update(Integer merchantId, Merchant updatedMerchant) {
		Merchant wip = configurationMerchantRepository.findById(merchantId)
				.orElseThrow(() -> new EntityNotFoundException("Merchant not found"));

		wip.copyProperties(updatedMerchant);
		wip.setStatus("CONFIGURATION");
		wip.reparent();

		configurationMerchantRepository.save(wip);
	}

	public void checkIn(Integer merchantId) {
		Merchant wip = configurationMerchantRepository.findById(merchantId)
				.orElseThrow(() -> new EntityNotFoundException("Merchant not found"));

		if (!ObjectUtils.isEmpty(wip.getSourceEntityId())) {
			Merchant live = configurationMerchantRepository.findById(wip.getSourceEntityId())
					.orElseThrow(() -> new EntityNotFoundException("Live merchant not found"));

			// TODO: check if wip got in fact changed before changing the live version

			live.copyProperties(wip);
			live.setStatus("LIVE");
			live.setSourceEntityId(null);
			live.getSearchParameters().forEach(liveProd -> liveProd.setSourceEntityId(null));
			live.reparent();

			merchantRepository.save(live);
			configurationMerchantRepository.delete(wip);
		} else {
			wip.setStatus("LIVE");
			merchantRepository.save(wip);
		}
	}

	public Merchant checkOut(Integer merchantId) {
		Merchant live = configurationMerchantRepository.findById(merchantId)
				.orElseThrow(() -> new EntityNotFoundException("Live merchant not found"));

		Merchant wip = Merchant
				.builder()
				.address(new Address())
				.searchParameters(new ArrayList<>())
				.build();

		wip.copyProperties(live);
		wip.setSourceEntityId(live.getId());
		wip.setStatus("CONFIGURATION");
		wip.reparent();

		return configurationMerchantRepository.save(wip);
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
