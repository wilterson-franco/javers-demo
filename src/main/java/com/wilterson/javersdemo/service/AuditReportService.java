package com.wilterson.javersdemo.service;

import org.javers.core.Changes;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditReportService {

	public List<AuditReport> generateAuditReport(Changes changes) {

		class AuditReportComparator implements Comparator<AuditReport> {
			public int compare(AuditReport ar1, AuditReport ar2) {
				return ar1.getCommitDatetime().compareTo(ar2.getCommitDatetime());
			}
		}

		List<AuditReport> auditReportItems = new ArrayList<>();

		for (Change change : changes) {
			if (change instanceof ValueChange) {
				ValueChange valueChange = (ValueChange) change;
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
				auditReportItems.add(auditReport);
			} else if (change instanceof NewObject) {
				NewObject newObject = (NewObject) change;
				AuditReport auditReport = AuditReport
						.builder()
						.changeType(ChangeType.NEW_ENTITY)
						.entity(newObject.getAffectedGlobalId().getTypeName())
						.build();
				newObject.getCommitMetadata().ifPresent(val -> {
					auditReport.setAuthor(val.getAuthor());
					auditReport.setCommitDatetime(val.getCommitDateInstant());
					auditReportItems.add(auditReport);
				});
			} else if (change instanceof ObjectRemoved) {

				// TODO: to be implemented

			}
		}
		return auditReportItems.stream().sorted(new AuditReportComparator()).collect(Collectors.toList());
	}
}
