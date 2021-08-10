package com.wilterson.javersdemo.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditReport {
	private ChangeType changeType;
	private String entity;
	private String propertyName;
	private Object newPropertyValue;
	private Object oldPropertyValue;
	private String author;
	private LocalDateTime commitDatetime;
}
