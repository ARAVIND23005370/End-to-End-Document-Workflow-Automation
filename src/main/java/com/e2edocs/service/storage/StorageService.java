package com.e2edocs.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

public interface StorageService {
    String store(MultipartFile file);
    String store(InputStream inputStream, String filename, String contentType);
    StoredFileMetadata storeWithMetadata(MultipartFile file);
    StoredFileMetadata storeWithMetadata(InputStream inputStream, String originalFilename, String contentType, long size);
    Path load(String storageKey);
    Resource loadAsResource(String storageKey);
    InputStream loadAsStream(String storageKey);
    byte[] loadAsBytes(String storageKey);
    void delete(String storageKey);
    boolean exists(String storageKey);
}
