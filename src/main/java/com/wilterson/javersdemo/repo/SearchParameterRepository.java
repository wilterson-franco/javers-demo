package com.wilterson.javersdemo.repo;

import com.wilterson.javersdemo.domain.SearchParameter;
import org.javers.spring.annotation.JaversAuditable;
import org.springframework.data.repository.CrudRepository;

public interface SearchParameterRepository extends CrudRepository<SearchParameter, Integer> {
	@Override
	@JaversAuditable
	<S extends SearchParameter> S save(S s);
}
