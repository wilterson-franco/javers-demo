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
public class SearchParameter {

	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	private boolean required;

	private Integer sourceEntityId;

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;

	public void reparent() {
		// do nothing
	}

	public void copyProperties(SearchParameter from) {
		required = from.isRequired();
		name = from.getName();
		sourceEntityId = from.getSourceEntityId();
	}
}
