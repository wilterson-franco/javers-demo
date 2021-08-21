package com.wilterson.javersdemo.service;

import java.util.Comparator;

public class AuditReportComparator implements Comparator<AuditReport> {
	public int compare(AuditReport ar1, AuditReport ar2) {
		return ar1.getMetadata().getCommitId().compareTo(ar2.getMetadata().getCommitId());
	}
}
