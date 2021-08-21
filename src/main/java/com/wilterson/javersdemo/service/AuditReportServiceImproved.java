package com.wilterson.javersdemo.service;

import org.javers.core.ChangesByCommit;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.*;
import org.javers.core.diff.changetype.container.ContainerChange;
import org.javers.core.diff.changetype.container.ContainerElementChange;
import org.javers.core.diff.changetype.container.ValueAdded;
import org.javers.core.diff.changetype.container.ValueRemoved;
import org.javers.core.diff.changetype.map.MapChange;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.core.metamodel.object.ValueObjectId;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AuditReportServiceImproved {

	private final Javers javers;

	public AuditReportServiceImproved(Javers javers) {
		this.javers = javers;
	}

	public void auditReport(List<AuditReportImproved> auditReportItems, int entityId, String typeName) {
		QueryBuilder jqlQuery = QueryBuilder.byInstanceId(entityId, typeName).withChildValueObjects();
		List<ChangesByCommit> changesByCommit = javers.findChanges(jqlQuery.build()).groupByCommit();

		for (ChangesByCommit byCommit : changesByCommit) {

			AuditReportImproved auditReportImproved = new AuditReportImproved();

			if (!CollectionUtils.isEmpty(byCommit.get())) {
				Change change = byCommit.get().get(0);
				setMetadata(auditReportImproved, change);
				setEntityRef(auditReportImproved, change);
				setChangeType(auditReportImproved, byCommit.get());
			}

			for (Change change : byCommit.get()) {
				generateAuditReport(auditReportImproved, change);
			}

			auditReportItems.add(auditReportImproved);

			for (PropertyChange change : auditReportImproved.getPropertyChanges()) {
				for (AuditReportImproved elementChange : change.getElementChanges()) {
					auditReport(auditReportItems, (Integer) elementChange.getEntityRef().getEntityId(), elementChange.getEntityRef().getEntity());
				}
			}
		}
	}

	public void generateAuditReport(AuditReportImproved auditReportImproved, Change change) {
		if (isValueChange(change) || isInitialValueChange(change)) {
			handleValueChange(auditReportImproved, (ValueChange) change);
		} else if (isMapChange(change)) {
			// TODO:
			System.out.println("something");
		} else if (isContainerChange(change)) {
			ContainerChange containerChange = (ContainerChange) change;
			auditReportImproved.addPropertyChange(PropertyChange
					.builder()
					.type(containerChange.getChangeType())
					.property(containerChange.getPropertyName())
					.elementChanges(handleContainerChange(containerChange))
					.build());
		} else if (isReferenceChange(change)) {
			// TODO:
			System.out.println("something");
		} else if (isObjectRemoved(change)) {
			// TODO: to be implemented
			System.out.println("something");
		}
	}

	private void setChangeType(AuditReportImproved auditReportImproved, List<Change> changes) {
		if (changes.stream().anyMatch(change -> change instanceof NewObject || change instanceof InitialValueChange)) {
			auditReportImproved.setChangeType(ChangeType.NewObject);
			return;
		}
		if (changes.stream().anyMatch(change -> change instanceof ObjectRemoved)) {
			auditReportImproved.setChangeType(ChangeType.DeletedObject);
			return;
		}
		if (changes.stream().anyMatch(change -> change instanceof org.javers.core.diff.changetype.PropertyChange)) {
			auditReportImproved.setChangeType(ChangeType.ValueChange);
			return;
		}
	}

	private List<AuditReportImproved> handleContainerChange(ContainerChange containerChange) {

		List<AuditReportImproved> auditReportImprovedItems = new ArrayList<>();
		AuditReportImproved auditReportImproved = new AuditReportImproved();

		for (ContainerElementChange change : containerChange.getChanges()) {
			if (isValueAdded(change)) {
				handleValueAdded(auditReportImproved, (ValueAdded) change);
			} else if (isValueRemoved(change)) {
				// TODO
			}
			auditReportImprovedItems.add(auditReportImproved);
		}

		return auditReportImprovedItems;
	}

	private void handleValueAdded(AuditReportImproved auditReportImproved, ValueAdded valueAdded) {
		Object value = valueAdded.getAddedValue();
		if (isInstanceId(value)) {
			InstanceId instanceId = (InstanceId) value;
			auditReportImproved.setChangeType(ChangeType.NewObject);
			auditReportImproved.setEntityRef(EntityRef
					.builder()
					.entity(instanceId.getTypeName())
					.entityId((Integer) instanceId.getCdoId())
					.build());
		}
	}

	private void setMetadata(AuditReportImproved auditReportImproved, Change change) {

		Metadata metadata = new Metadata();

		change.getCommitMetadata().ifPresent(val -> {
			metadata.setAuthor(val.getAuthor());
			metadata.setCommitId(val.getId().toString());
			metadata.setCommitDatetime(val.getCommitDateInstant());
		});

		auditReportImproved.setMetadata(metadata);
	}

	private void setEntityRef(AuditReportImproved auditReportImproved, Change change) {
		EntityRef entityRef = new EntityRef();
		if (isInstanceId(change.getAffectedGlobalId())) {
			entityRef.setEntity(change.getAffectedGlobalId().getTypeName());
			entityRef.setEntityId((Integer) ((InstanceId) change.getAffectedGlobalId()).getCdoId());
		} else if (isValueObjectId(change.getAffectedGlobalId())) {
			GlobalId ownerId = ((ValueObjectId) change.getAffectedGlobalId()).getOwnerId();
			entityRef.setEntity(ownerId.getTypeName());
			entityRef.setEntityId((Integer) ((InstanceId) ownerId).getCdoId());
		}
		auditReportImproved.setEntityRef(entityRef);
	}

	private void handleValueChange(AuditReportImproved auditReportImproved, ValueChange valueChange) {

		auditReportImproved.addPropertyChange(PropertyChange
				.builder()
				.type(valueChange.getChangeType())
				.property(valueChange.getPropertyName())
				.left(valueChange.getLeft())
				.right(valueChange.getRight())
				.elementChanges(Collections.emptyList())
				.build());
	}

	private boolean isValueChange(Change change) {
		return change instanceof ValueChange;
	}

	private boolean isNewObject(Change change) {
		return change instanceof NewObject;
	}

	private boolean isInitialValueChange(Change change) {
		return change instanceof InitialValueChange;
	}

	private boolean isContainerChange(Change change) {
		return change instanceof ContainerChange;
	}

	private boolean isObjectRemoved(Change change) {
		return change instanceof ObjectRemoved;
	}

	private boolean isMapChange(Change change) {
		return change instanceof MapChange;
	}

	private boolean isReferenceChange(Change change) {
		return change instanceof ReferenceChange;
	}

	private boolean isValueAdded(ContainerElementChange containerElementChange) {
		return containerElementChange instanceof ValueAdded;
	}

	private boolean isValueRemoved(ContainerElementChange containerElementChange) {
		return containerElementChange instanceof ValueRemoved;
	}

	private boolean isInstanceId(Object object) {
		return object instanceof InstanceId;
	}

	private boolean isValueObjectId(Object object) {
		return object instanceof ValueObjectId;
	}
}
