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
public class AuditReportService {

	private final Javers javers;

	public AuditReportService(Javers javers) {
		this.javers = javers;
	}

	public List<AuditReport> auditReport( int entityId, String typeName) {
		List<AuditReport> auditReportItems = new ArrayList<>();

		QueryBuilder jqlQuery = QueryBuilder.byInstanceId(entityId, typeName).withChildValueObjects();
		List<ChangesByCommit> changesByCommit = javers.findChanges(jqlQuery.build()).groupByCommit();

		for (ChangesByCommit byCommit : changesByCommit) {

			AuditReport auditReport = new AuditReport();

			if (!CollectionUtils.isEmpty(byCommit.get())) {
				Change change = byCommit.get().get(0);
				setMetadata(auditReport, change);
				setEntityRef(auditReport, change);
				setChangeType(auditReport, byCommit.get());
			}

			for (Change change : byCommit.get()) {
				generateAuditReport(auditReport, change);
			}

			auditReportItems.add(auditReport);

			for (PropertyChange change : auditReport.getPropertyChanges()) {
				for (AuditReport elementChange : change.getElementChanges()) {
					auditReportItems.addAll(auditReport(elementChange.getEntityRef().getEntityId(), elementChange.getEntityRef().getEntity()));
				}
			}
		}
		return auditReportItems;
	}

	public void generateAuditReport(AuditReport auditReport, Change change) {
		if (isValueChange(change) || isInitialValueChange(change)) {
			handleValueChange(auditReport, (ValueChange) change);
		} else if (isMapChange(change)) {
			// TODO:
			System.out.println("something");
		} else if (isContainerChange(change)) {
			ContainerChange containerChange = (ContainerChange) change;
			auditReport.addPropertyChange(PropertyChange
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

	private void setChangeType(AuditReport auditReport, List<Change> changes) {
		if (changes.stream().anyMatch(change -> change instanceof NewObject || change instanceof InitialValueChange)) {
			auditReport.setChangeType(ChangeType.NewObject);
			return;
		}
		if (changes.stream().anyMatch(change -> change instanceof ObjectRemoved)) {
			auditReport.setChangeType(ChangeType.DeletedObject);
			return;
		}
		if (changes.stream().anyMatch(change -> change instanceof org.javers.core.diff.changetype.PropertyChange)) {
			auditReport.setChangeType(ChangeType.ValueChange);
			return;
		}
	}

	private List<AuditReport> handleContainerChange(ContainerChange containerChange) {

		List<AuditReport> auditReportItems = new ArrayList<>();
		AuditReport auditReport = new AuditReport();

		for (ContainerElementChange change : containerChange.getChanges()) {
			if (isValueAdded(change)) {
				handleValueAdded(auditReport, (ValueAdded) change);
			} else if (isValueRemoved(change)) {
				// TODO
			}
			auditReportItems.add(auditReport);
		}

		return auditReportItems;
	}

	private void handleValueAdded(AuditReport auditReport, ValueAdded valueAdded) {
		Object value = valueAdded.getAddedValue();
		if (isInstanceId(value)) {
			InstanceId instanceId = (InstanceId) value;
			auditReport.setChangeType(ChangeType.NewObject);
			auditReport.setEntityRef(EntityRef
					.builder()
					.entity(instanceId.getTypeName())
					.entityId((Integer) instanceId.getCdoId())
					.build());
		}
	}

	private void setMetadata(AuditReport auditReport, Change change) {

		Metadata metadata = new Metadata();

		change.getCommitMetadata().ifPresent(val -> {
			metadata.setAuthor(val.getAuthor());
			metadata.setCommitId(val.getId().toString());
			metadata.setCommitDatetime(val.getCommitDateInstant());
		});

		auditReport.setMetadata(metadata);
	}

	private void setEntityRef(AuditReport auditReport, Change change) {
		EntityRef entityRef = new EntityRef();
		if (isInstanceId(change.getAffectedGlobalId())) {
			entityRef.setEntity(change.getAffectedGlobalId().getTypeName());
			entityRef.setEntityId((Integer) ((InstanceId) change.getAffectedGlobalId()).getCdoId());
		} else if (isValueObjectId(change.getAffectedGlobalId())) {
			GlobalId ownerId = ((ValueObjectId) change.getAffectedGlobalId()).getOwnerId();
			entityRef.setEntity(ownerId.getTypeName());
			entityRef.setEntityId((Integer) ((InstanceId) ownerId).getCdoId());
		}
		auditReport.setEntityRef(entityRef);
	}

	private void handleValueChange(AuditReport auditReport, ValueChange valueChange) {

		auditReport.addPropertyChange(PropertyChange
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
