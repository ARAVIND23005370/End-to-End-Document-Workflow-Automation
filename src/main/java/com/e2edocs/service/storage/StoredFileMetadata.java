package com.e2edocs.service.storage;

public class StoredFileMetadata {
    private String storageKey;
    private String originalFilename;
    private String contentType;
    private Long size;
    private String fileExtension;

    public StoredFileMetadata() {
    }

    public StoredFileMetadata(String storageKey, String originalFilename, String contentType, Long size, String fileExtension) {
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.fileExtension = fileExtension;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
