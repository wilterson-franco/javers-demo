package com.wilterson.javersdemo.service;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntityRef {
	private String entity;
	private Integer entityId;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntityRef) {
			EntityRef that = (EntityRef) obj;
			return this.entity.equals(that.getEntity()) && this.entityId.equals(that.getEntityId());
		}
		return false;
	}

	@Override
	public String toString() {
		return "EntityRef{" +
				"entity='" + entity + '\'' +
				", entityId=" + entityId +
				'}';
	}
}
