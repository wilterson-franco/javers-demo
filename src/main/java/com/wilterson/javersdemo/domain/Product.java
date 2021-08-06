package com.wilterson.javersdemo.domain;

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
	private int id;

	private String name;

	private double price;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private Store store;

	public void setNamePrefix(String prefix) {
		this.name = prefix + this.name;
	}

	public void reparent() {
		// do nothing
	}
}
