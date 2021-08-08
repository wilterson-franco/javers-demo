package com.wilterson.javersdemo.repo;

import com.wilterson.javersdemo.domain.Store;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ConfigurationStoreRepository extends CrudRepository<Store, Integer> {

	List<Store> findByGuid(String guid);
}
