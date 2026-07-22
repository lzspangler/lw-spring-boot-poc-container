package com.redhat.lightwell.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final Long resourceId;

    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(resourceType + " not found with id: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }
}
