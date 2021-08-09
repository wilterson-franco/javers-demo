package com.wilterson.javersdemo.service;


import com.wilterson.javersdemo.domain.Merchant;
import com.wilterson.javersdemo.domain.SearchParameter;
import com.wilterson.javersdemo.repo.MerchantRepository;
import com.wilterson.javersdemo.repo.SearchParameterRepository;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

	private final SearchParameterRepository SearchParameterRepository;
	private final MerchantRepository merchantRepository;

	public MerchantService(SearchParameterRepository SearchParameterRepository, MerchantRepository merchantRepository) {
		this.SearchParameterRepository = SearchParameterRepository;
		this.merchantRepository = merchantRepository;
	}

	public Merchant findMerchantById(int merchantId) {
		return merchantRepository.findById(merchantId).get();
	}

	public SearchParameter findSearchParameterById(int id) {
		return this.SearchParameterRepository.findById(id).get();
	}
}
