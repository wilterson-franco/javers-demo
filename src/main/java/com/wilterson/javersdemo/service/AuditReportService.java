package com.wilterson.javersdemo.service;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.*;
import org.javers.core.diff.changetype.map.MapChange;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditReportService {

	private final Javers javers;

	public AuditReportService(Javers javers) {
		this.javers = javers;
	}

	public List<AuditReport> auditReport(int entityId, String typeName) {

		QueryBuilder jqlQuery = QueryBuilder.byInstanceId(entityId, typeName);
		Changes changes = javers.findChanges(jqlQuery.build());

		return generateAuditReport(changes);
	}

	public List<AuditReport> generateAuditReport(Changes changes) {

		class AuditReportComparator implements Comparator<AuditReport> {
			public int compare(AuditReport ar1, AuditReport ar2) {
				return ar1.getCommitDatetime().compareTo(ar2.getCommitDatetime());
			}
		}

		List<AuditReport> auditReportItems = new ArrayList<>();

		for (Change change : changes) {
			if (isNewObject(change)) {
				AuditReport auditReport = handleNewObject((NewObject) change);
				auditReportItems.add(auditReport);
			} else if (isValueChange(change)) {
				AuditReport auditReport = handleValueChange((ValueChange) change);
				auditReportItems.add(auditReport);
			} else if (isMapChange(change)) {

				// TODO:
				System.out.println("something");

			} else if (isContainerChange(change)) {

				auditReportItems.addAll(handleContainerChange((ContainerChange) change));

			} else if (isReferenceChange(change)) {

				// TODO:
				System.out.println("something");

			} else if (isObjectRemoved(change)) {

				// TODO: to be implemented
				System.out.println("something");

			}
		}
		return auditReportItems.stream().sorted(new AuditReportComparator()).collect(Collectors.toList());
	}

	private List<AuditReport> handleContainerChange(ContainerChange containerChange) {

		for (ContainerElementChange change : containerChange.getChanges()) {
			if (isValueAdded(change)) {
				return handleValueAdded((ValueAdded) change);
			} else if (isValueRemoved(change)) {
				// TODO
			}
		}

		return Collections.emptyList();
	}

	private List<AuditReport> handleValueAdded(ValueAdded valueAdded) {

		Object value = valueAdded.getAddedValue();
		if (isInstanceId(value)) {
			InstanceId instanceId = (InstanceId) value;
			return auditReport(((Integer) instanceId.getCdoId()), instanceId.getTypeName());
		}
		return Collections.emptyList();
	}

	private AuditReport handleNewObject(NewObject newObject) {
		AuditReport auditReport = AuditReport
				.builder()
				.changeType(ChangeType.NEW_ENTITY)
				.entity(newObject.getAffectedGlobalId().getTypeName())
				.build();
		newObject.getCommitMetadata().ifPresent(val -> {
			auditReport.setAuthor(val.getAuthor());
			auditReport.setCommitDatetime(val.getCommitDateInstant());
		});
		return auditReport;
	}

	private AuditReport handleValueChange(ValueChange valueChange) {
		AuditReport auditReport = AuditReport
				.builder()
				.propertyName(valueChange.getPropertyName())
				.oldPropertyValue(valueChange.getLeft())
				.newPropertyValue(valueChange.getRight())
				.changeType(ChangeType.valueOf(valueChange.getChangeType().toString()))
				.entity(valueChange.getAffectedGlobalId().getTypeName())
				.build();
		valueChange.getCommitMetadata().ifPresent(val -> {
			auditReport.setAuthor(val.getAuthor());
			auditReport.setCommitDatetime(val.getCommitDateInstant());
		});
		return auditReport;
	}

	private boolean isValueChange(Change change) {
		return change instanceof ValueChange;
	}

	private boolean isNewObject(Change change) {
		return change instanceof NewObject;
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
}
