package com.wilterson.javersdemo.repo;

import com.wilterson.javersdemo.domain.Merchant;
import org.springframework.data.repository.CrudRepository;

//@JaversSpringDataAuditable
public interface MerchantRepository extends CrudRepository<Merchant, Integer> {
}
