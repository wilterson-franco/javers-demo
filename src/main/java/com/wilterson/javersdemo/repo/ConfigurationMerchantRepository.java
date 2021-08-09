package com.wilterson.javersdemo.repo;

import com.wilterson.javersdemo.domain.Merchant;
import org.springframework.data.repository.CrudRepository;

public interface ConfigurationMerchantRepository extends CrudRepository<Merchant, Integer> {
}
