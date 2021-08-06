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
}
