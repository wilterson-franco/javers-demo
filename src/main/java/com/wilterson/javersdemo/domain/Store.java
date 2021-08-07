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
@Inheritance(strategy = InheritanceType.JOINED)
public class Store {

	@Id
	@GeneratedValue
	private int id;

	private String name;

	@Embedded
	private Address address;

	private String status;

	private String guid;

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
}
