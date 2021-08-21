package com.wilterson.javersdemo.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditReport {
	private ChangeType changeType;
	private Metadata metadata;
	private EntityRef entityRef;
	private List<PropertyChange> propertyChanges;

	public void addPropertyChange(PropertyChange propertyChange) {
		if (ObjectUtils.isEmpty(propertyChanges)) {
			propertyChanges = new ArrayList<>();
		}
		propertyChanges.add(propertyChange);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AuditReport) {
			AuditReport that = (AuditReport) obj;
			return this.changeType == that.getChangeType()
					&& isMetadataEqualsTo(that.getMetadata())
					&& isEntityRefEqualsTo(that.getEntityRef())
					&& propertyChangesContainsAll(that.getPropertyChanges());
		}
		return false;
	}

	@Override
	public String toString() {
		return "AuditReport{" +
				"changeType=" + changeType +
				", metadata=" + metadata +
				", entityRef=" + entityRef +
				", propertyChanges=" + propertyChanges +
				'}';
	}

	private boolean isMetadataEqualsTo(Metadata that) {
		if (!ObjectUtils.isEmpty(this.metadata)) {
			return this.metadata.equals(that);
		} else if (ObjectUtils.isEmpty(that)) {
			return true;
		}
		return false;
	}

	private boolean isEntityRefEqualsTo(EntityRef that) {
		if (!ObjectUtils.isEmpty(this.entityRef)) {
			return this.entityRef.equals(that);
		} else if (ObjectUtils.isEmpty(that)) {
			return true;
		}
		return false;
	}

	private boolean propertyChangesContainsAll(List<PropertyChange> thatList) {
		if (!CollectionUtils.isEmpty(this.propertyChanges)) {
			return this.propertyChanges.containsAll(thatList);
		} else if (CollectionUtils.isEmpty(thatList)) {
			return true;
		}
		return false;
	}

}

