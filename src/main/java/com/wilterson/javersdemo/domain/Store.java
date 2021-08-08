package com.wilterson.javersdemo.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.util.*;
import java.util.function.Predicate;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Store {

	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	@Embedded
	private Address address;

	private String status;

	private String guid;

	private Integer liveStoreId;

	@JsonManagedReference
	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Product> products = new ArrayList<>();

	public void addProduct(Product product) {
		product.setStore(this);
		this.products.add(product);
	}

	public void reparent() {
		if (!ObjectUtils.isEmpty(products)) {
			products.forEach(product -> {
				product.setStore(this);
				product.reparent();
			});
		}
	}

	public void copyProperties(Store from) {
		name = from.getName();
		status = from.getStatus();
		guid = from.getGuid();

		if (!ObjectUtils.isEmpty(from.getAddress())) {
			address.copyProperties(from.getAddress());
		} else {
			address = new Address();
		}

		if (!ObjectUtils.isEmpty(from.getProducts())) {
			// Handles Products to be deleted (products present in the current list but not
			// provided in the updated Store object)
			Map<Product, Optional<Product>> deleteMap = getMappedProducts(products, from.getProducts());
			deleteMap.forEach((currProd, optionalFromProd) -> {
				if (!optionalFromProd.isPresent()) {
					products.remove(currProd);
				}
			});
			// Handles products to be updated and added (products provided with the updated
			// Store object that either is present or not in the current list of products)
			Map<Product, Optional<Product>> updateAndInsertMap = getMappedProducts(from.getProducts(), products);
			updateAndInsertMap.forEach((fromProd, optionalThisProd) -> {
				if (optionalThisProd.isPresent()) {
					Product thisProd = optionalThisProd.get();
					thisProd.copyProperties(fromProd);
				} else {
					Product newProd = new Product();
					newProd.copyProperties(fromProd);
					newProd.setLiveProduct(fromProd.getId());
					products.add(newProd);
				}
			});
		} else {
			products = new ArrayList<>();
		}
	}

	private Map<Product, Optional<Product>> getMappedProducts(List<Product> prodsToMap, List<Product> currProducts) {
		Map<Product, Optional<Product>> map = new HashMap<>();
		for (Product prodToMap : prodsToMap) {
			Optional<Product> optionalThisProd = currProducts
					.stream()
					.filter(thisProd ->
							thisProd.getId().equals(prodToMap.getId()) ||
									thisProd.getId().equals(prodToMap.getLiveProduct()) ||
									prodToMap.getId().equals(thisProd.getLiveProduct()))
					.findFirst();
			map.put(prodToMap, optionalThisProd);
		}
		return map;
	}
}
