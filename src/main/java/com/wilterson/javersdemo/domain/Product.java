package com.wilterson.javersdemo.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	private double price;

	private Integer liveProduct;

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private Store store;

	public void setNamePrefix(String prefix) {
		this.name = prefix + this.name;
	}

	public void reparent() {
		// do nothing
	}

	public void copyProperties(Product from) {
		price = from.getPrice();
		name = from.getName();
		liveProduct = from.getLiveProduct();
	}
}
