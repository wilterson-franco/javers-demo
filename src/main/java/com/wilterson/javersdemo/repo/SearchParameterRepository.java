package com.wilterson.javersdemo.repo;

import com.wilterson.javersdemo.domain.SearchParameter;
import org.springframework.data.repository.CrudRepository;

//@JaversSpringDataAuditable
public interface SearchParameterRepository extends CrudRepository<SearchParameter, Integer> {
}
