package com.wilterson.javersdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@PrimaryKeyJoinColumn(name = "live_store_id")
public class StoreWip extends Store {

	private String liveStoreGuid;

	@Override
	public void reparent() {
		liveStoreGuid = super.getGuid();
		super.reparent();
	}
}
