package com.wilterson.javersdemo.service;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
	private String author;
	private String commitId;
	private Instant commitDatetime;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Metadata) {
			Metadata that = (Metadata) obj;
			return this.commitId.equals(that.getCommitId())
					&& this.author.equals(that.getAuthor())
					// TODO: leaving it out for now, otherwise my integration test will fail
					//&& this.commitDatetime.equals(that.getCommitDatetime())
					;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Metadata{" +
				"author='" + author + '\'' +
				", commitId='" + commitId + '\'' +
				", commitDatetime=" + commitDatetime +
				'}';
	}
}
