package com.wilterson.javersdemo.repo;

import com.wilterson.javersdemo.domain.StoreWip;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConfigurationStoreRepository extends CrudRepository<StoreWip, Integer> {

	Optional<StoreWip> findByLiveStoreGuid(String liveStoreGuid);
}
