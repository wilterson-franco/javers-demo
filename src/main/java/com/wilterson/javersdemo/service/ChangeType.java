package com.wilterson.javersdemo.service;

public enum ChangeType {
	PROPERTY_ADDED,
	PROPERTY_REMOVED,
	PROPERTY_VALUE_CHANGED,
	DELETED_ENTITY,
	NEW_ENTITY,

	NewObject,
	CollectionChange,
	ValueChange,
	PropertyValueChange
}
