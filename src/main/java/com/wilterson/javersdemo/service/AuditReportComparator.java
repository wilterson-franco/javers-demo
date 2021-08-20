package com.wilterson.javersdemo.service;

import java.util.Comparator;

public class AuditReportComparator implements Comparator<AuditReportImproved> {
	public int compare(AuditReportImproved ar1, AuditReportImproved ar2) {
		return ar1.getMetadata().getCommitId().compareTo(ar2.getMetadata().getCommitId());
	}
}
