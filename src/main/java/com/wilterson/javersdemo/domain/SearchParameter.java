package com.wilterson.javersdemo.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.javers.core.metamodel.annotation.DiffIgnore;

import javax.persistence.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SearchParameter {

	@DiffIgnore
	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	private Boolean required;

	@DiffIgnore
	private Integer sourceEntityId;

	@DiffIgnore
	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;

	public void reparent() {
		// do nothing
	}

	public void copyProperties(SearchParameter from) {
		required = from.getRequired();
		name = from.getName();
		sourceEntityId = from.getSourceEntityId();
	}
}
