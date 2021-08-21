package com.wilterson.javersdemo.service;

import lombok.*;
import org.javers.core.diff.changetype.PropertyChangeType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyChange {
	private PropertyChangeType type;
	private String property;
	private Object left;
	private Object right;
	private List<AuditReport> elementChanges;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PropertyChange) {
			PropertyChange that = (PropertyChange) obj;
			return this.type == that.getType()
					&& this.property.equals(that.getProperty())
					&& isLeftEqualsTo(that.getLeft())
					&& isRightEqualsTo(that.getRight())
					&& elementChangesContainsAll(that.getElementChanges());
		}
		return false;
	}

	@Override
	public String toString() {
		return "PropertyChange{" +
				"type=" + type +
				", property='" + property + '\'' +
				", left=" + left +
				", right=" + right +
				", elementChanges=" + elementChanges +
				'}';
	}

	private boolean isLeftEqualsTo(Object that) {
		if (ObjectUtils.isEmpty(this.left) == ObjectUtils.isEmpty(that)) {
			return true;
		} else if (!ObjectUtils.isEmpty(this.left) && this.left.equals(that)) {
			return true;
		}
		return false;
	}

	private boolean isRightEqualsTo(Object that) {
		if (ObjectUtils.isEmpty(this.right) == ObjectUtils.isEmpty(that)) {
			return true;
		} else if (!ObjectUtils.isEmpty(this.right) && this.right.equals(that)) {
			return true;
		}
		return false;
	}

	private boolean elementChangesContainsAll(List<AuditReport> thatList) {
		if (!CollectionUtils.isEmpty(this.elementChanges)) {
			return this.elementChanges.containsAll(thatList);
		} else if (CollectionUtils.isEmpty(thatList)){
			return true;
		}
		return false;
	}
}
