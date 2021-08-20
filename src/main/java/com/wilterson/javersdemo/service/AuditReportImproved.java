package com.wilterson.javersdemo.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.javers.core.diff.changetype.PropertyChangeType;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditReportImproved {
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
}

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Metadata {
	private String author;
	private String commitId;
	private Instant commitDatetime;
}

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class EntityRef {
	private String entity;
	private Object entityId;
}

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PropertyChange {
	private PropertyChangeType type;
	private String property;
	private Object left;
	private Object right;
	private List<AuditReportImproved> elementChanges;
}
