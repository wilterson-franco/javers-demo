package com.wilterson.javersdemo.domain;

import lombok.*;

import javax.persistence.Embeddable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {

	private String address;
	private String postalCode;

	public void copyProperties(Address from) {
		address = from.getAddress();
		postalCode = from.getPostalCode();
	}
}
