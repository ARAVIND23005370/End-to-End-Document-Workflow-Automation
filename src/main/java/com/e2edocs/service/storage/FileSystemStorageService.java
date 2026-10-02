package com.e2edocs.service.storage;

import com.e2edocs.exception.BadRequestException;
import com.e2edocs.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    public FileSystemStorageService(
            @Value("${e2edocs.storage.local.base-path:${e2edocs.storage.directory:uploads/}}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + uploadDir, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        return storeWithMetadata(file).getStorageKey();
    }

    @Override
    public String store(InputStream inputStream, String filename, String contentType) {
        return storeWithMetadata(inputStream, filename, contentType, -1).getStorageKey();
    }

    @Override
    public StoredFileMetadata storeWithMetadata(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");
        validateFilenameSafety(originalFilename);

        String extension = extractExtension(originalFilename);
        String storageKey = UUID.randomUUID().toString() + extension;

        try {
            Path destinationFile = resolveAndValidatePath(storageKey);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return new StoredFileMetadata(
                    storageKey,
                    originalFilename,
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                    file.getSize(),
                    extension
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public StoredFileMetadata storeWithMetadata(InputStream inputStream, String originalFilename, String contentType, long size) {
        String cleanName = StringUtils.cleanPath(originalFilename != null ? originalFilename : "document.pdf");
        validateFilenameSafety(cleanName);

        String extension = extractExtension(cleanName);
        String storageKey = UUID.randomUUID().toString() + extension;

        try {
            Path destinationFile = resolveAndValidatePath(storageKey);
            long bytesCopied = Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            long finalSize = size >= 0 ? size : bytesCopied;

            return new StoredFileMetadata(
                    storageKey,
                    cleanName,
                    contentType != null ? contentType : "application/octet-stream",
                    finalSize,
                    extension
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Path load(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new BadRequestException("Storage key cannot be empty");
        }
        return resolveAndValidatePath(storageKey);
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        try {
            Path file = load(storageKey);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Could not read file for key: " + storageKey);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Could not read file for key: " + storageKey);
        }
    }

    @Override
    public InputStream loadAsStream(String storageKey) {
        try {
            Path file = load(storageKey);
            if (!Files.exists(file)) {
                throw new ResourceNotFoundException("File not found for key: " + storageKey);
            }
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open stream for key: " + storageKey, e);
        }
    }

    @Override
    public byte[] loadAsBytes(String storageKey) {
        try {
            Path file = load(storageKey);
            if (!Files.exists(file)) {
                throw new ResourceNotFoundException("File not found for key: " + storageKey);
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read bytes for key: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return;
        try {
            Path file = load(storageKey);
            Files.deleteIfExists(file);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean exists(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return false;
        try {
            Path file = load(storageKey);
            return Files.exists(file);
        } catch (Exception e) {
            return false;
        }
    }

    private void validateFilenameSafety(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BadRequestException("Filename contains invalid path characters: " + filename);
        }
    }

    private Path resolveAndValidatePath(String storageKey) {
        Path destination = this.rootLocation.resolve(storageKey).normalize().toAbsolutePath();
        if (!destination.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new BadRequestException("Invalid storage destination outside storage root");
        }
        return destination;
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot).toLowerCase() : "";
    }
}
