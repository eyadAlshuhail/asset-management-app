package com.se411.assetmanagement.exception;

/** Thrown when no asset exists with the requested id. Mapped to HTTP 404 by GlobalExceptionHandler. */
public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException(Long id) {
        super("Asset not found with id " + id);
    }
}
