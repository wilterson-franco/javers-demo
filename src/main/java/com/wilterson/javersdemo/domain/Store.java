package com.wilterson.javersdemo.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

		if (!ObjectUtils.isEmpty(address)) {
			address.copyProperties(from.getAddress());
		}

		if (!ObjectUtils.isEmpty(products)) {
			products.forEach(p ->
					p.copyProperties(from.getProducts()
							.stream()
							.filter(prod -> prod.getId().equals(p.getId()))
							.findFirst().orElseThrow(() -> new EntityNotFoundException("Product not found"))));
		}
	}
}
