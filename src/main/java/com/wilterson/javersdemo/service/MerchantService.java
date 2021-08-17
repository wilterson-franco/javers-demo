package com.wilterson.javersdemo.service;


import com.wilterson.javersdemo.domain.Address;
import com.wilterson.javersdemo.domain.Merchant;
import com.wilterson.javersdemo.domain.SearchParameter;
import com.wilterson.javersdemo.repo.MerchantRepository;
import com.wilterson.javersdemo.repo.SearchParameterRepository;
import org.javers.core.Javers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Random;

@Service
public class MerchantService {
	private final SearchParameterRepository SearchParameterRepository;
	private final MerchantRepository merchantRepository;
	private final Javers javers;

	public MerchantService(SearchParameterRepository SearchParameterRepository, MerchantRepository merchantRepository, Javers javers) {
		this.SearchParameterRepository = SearchParameterRepository;
		this.merchantRepository = merchantRepository;
		this.javers = javers;
	}

	public Merchant createMerchant(Merchant merchantWip) {
		merchantWip.setStatus("CONFIGURATION");
		merchantWip.setGuid(guid());
		merchantWip.reparent();
		return merchantRepository.save(merchantWip);
	}

	@Transactional
	public Merchant update(Integer merchantId, Merchant updatedMerchant) {
		Merchant wip = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new EntityNotFoundException("Merchant not found"));

		wip.copyProperties(updatedMerchant);
		wip.setStatus("CONFIGURATION");
		wip.reparent();

		return merchantRepository.save(wip);
	}

	@Transactional
	public Merchant checkIn(Integer merchantId) {
		Merchant wip = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new EntityNotFoundException("Merchant not found"));

		Merchant liveMerchant;

		if (!ObjectUtils.isEmpty(wip.getSourceEntityId())) {
			Merchant live = merchantRepository.findById(wip.getSourceEntityId())
					.orElseThrow(() -> new EntityNotFoundException("Live merchant not found"));

			// TODO: check if wip got in fact changed before changing the live version

			live.copyProperties(wip);
			live.setStatus("LIVE");
			live.setSourceEntityId(null);
			live.getSearchParameters().forEach(liveProd -> liveProd.setSourceEntityId(null));
			live.reparent();

			liveMerchant = merchantRepository.save(live);
			merchantRepository.delete(wip);
			javers.commit("Wilterson Test", live);
		} else {
			wip.setStatus("LIVE");
			liveMerchant = merchantRepository.save(wip);
//			javers.commit("Wilterson Test", wip);
			javers.commit("Wilterson Test", liveMerchant);
		}

		return liveMerchant;
	}

	@Transactional
	public Merchant checkOut(Integer merchantId) {
		Merchant live = merchantRepository.findById(merchantId)
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

		return merchantRepository.save(wip);
	}

	public Merchant findMerchantById(int merchantId) {
		return merchantRepository.findById(merchantId).get();
	}

	public SearchParameter findSearchParameterById(int id) {
		return this.SearchParameterRepository.findById(id).get();
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
