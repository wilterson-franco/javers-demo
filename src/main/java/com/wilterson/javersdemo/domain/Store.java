package com.wilterson.javersdemo.domain;

import lombok.*;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Store {

	@Id
	@GeneratedValue
	private int id;

	private String name;

	@Embedded
	private Address address;

	private String status;

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Product> products = new ArrayList<>();

	public void addProduct(Product product) {
		product.setStore(this);
		this.products.add(product);
	}
}
