package com.wilterson.javersdemo.repo;

import com.wilterson.javersdemo.domain.Store;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@JaversSpringDataAuditable
public interface StoreRepository extends CrudRepository<Store, Integer> {

	Optional<Store> findByGuid(String guid);
}
