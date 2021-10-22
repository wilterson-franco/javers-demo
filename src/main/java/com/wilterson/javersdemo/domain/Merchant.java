package com.wilterson.javersdemo.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.util.*;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Merchant {

	@DiffIgnore
	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	@Embedded
	private Address address;

	//	@DiffIgnore
	private String status;

	@DiffIgnore
	private String guid;

	@DiffIgnore
	private Integer sourceEntityId;

	@Convert(converter = ContentTypeAttributeConverter.class)
	private Set<ContentType> contentTypes;

	@JsonManagedReference
	@OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SearchParameter> searchParameters = new ArrayList<>();

	public void reparent() {
		if (!ObjectUtils.isEmpty(searchParameters)) {
			searchParameters.forEach(searchParameter -> {
				searchParameter.setMerchant(this);
				searchParameter.reparent();
			});
		}
	}

	public void copyProperties(Merchant from) {
		name = from.getName();
		status = from.getStatus();
		guid = from.getGuid();

		if (!ObjectUtils.isEmpty(from.getAddress())) {
			address.copyProperties(from.getAddress());
		} else {
			address = new Address();
		}

		if (!ObjectUtils.isEmpty(from.getSearchParameters())) {
			// Handles SearchParameters to be deleted (searchParameters present in the current list but not
			// provided in the updated Merchant object)
			Map<SearchParameter, Optional<SearchParameter>> deleteMap = getMappedSearchParameters(searchParameters, from.getSearchParameters());
			deleteMap.forEach((currProd, optionalFromProd) -> {
				if (!optionalFromProd.isPresent()) {
					searchParameters.remove(currProd);
				}
			});
			// Handles searchParameters to be updated and added (searchParameters provided with the updated
			// Merchant object that either is present or not in the current list of searchParameters)
			Map<SearchParameter, Optional<SearchParameter>> updateAndInsertMap = getMappedSearchParameters(from.getSearchParameters(), searchParameters);
			updateAndInsertMap.forEach((fromProd, optionalThisProd) -> {
				if (optionalThisProd.isPresent()) {
					SearchParameter thisProd = optionalThisProd.get();
					thisProd.copyProperties(fromProd);
				} else {
					SearchParameter newProd = new SearchParameter();
					newProd.copyProperties(fromProd);
					newProd.setSourceEntityId(fromProd.getId());
					searchParameters.add(newProd);
				}
			});
		} else {
			searchParameters = new ArrayList<>();
		}
	}

	private Map<SearchParameter, Optional<SearchParameter>> getMappedSearchParameters(List<SearchParameter> prodsToMap, List<SearchParameter> currSearchParameters) {
		Map<SearchParameter, Optional<SearchParameter>> map = new HashMap<>();
		for (SearchParameter prodToMap : prodsToMap) {
			Optional<SearchParameter> optionalThisProd = currSearchParameters
					.stream()
					.filter(thisProd ->
							(!ObjectUtils.isEmpty(thisProd.getId()) && thisProd.getId().equals(prodToMap.getId())) ||
									(!ObjectUtils.isEmpty(thisProd.getId()) && thisProd.getId().equals(prodToMap.getSourceEntityId())) ||
									(!ObjectUtils.isEmpty(prodToMap.getId()) && prodToMap.getId().equals(thisProd.getSourceEntityId())))
					.findFirst();
			map.put(prodToMap, optionalThisProd);
		}
		return map;
	}
}
