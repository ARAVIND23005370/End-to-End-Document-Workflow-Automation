package com.e2edocs.service.validation;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Component
public class FileSignatureDetector {

    // Magic Bytes Constants
    private static final byte[] PDF_HEADER = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] ZIP_HEADER = new byte[]{0x50, 0x4B, 0x03, 0x04}; // PK.. (DOCX is a zip archive)
    private static final byte[] PNG_HEADER = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}; // .PNG....
    private static final byte[] JPEG_HEADER_1 = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG SOI
    private static final byte[] TIFF_LE_HEADER = new byte[]{0x49, 0x49, 0x2A, 0x00}; // II*. (Little Endian)
    private static final byte[] TIFF_BE_HEADER = new byte[]{0x4D, 0x4D, 0x00, 0x2A}; // MM.* (Big Endian)

    public String detectMimeType(InputStream inputStream, String fallbackMimeType) {
        if (inputStream == null) {
            return fallbackMimeType != null ? fallbackMimeType : "application/octet-stream";
        }

        try {
            if (!inputStream.markSupported()) {
                return fallbackMimeType;
            }

            inputStream.mark(32);
            byte[] header = new byte[16];
            int read = inputStream.read(header);
            inputStream.reset();

            if (read < 4) {
                return fallbackMimeType;
            }

            if (matches(header, PDF_HEADER)) {
                return "application/pdf";
            }
            if (matches(header, PNG_HEADER)) {
                return "image/png";
            }
            if (matches(header, JPEG_HEADER_1)) {
                return "image/jpeg";
            }
            if (matches(header, TIFF_LE_HEADER) || matches(header, TIFF_BE_HEADER)) {
                return "image/tiff";
            }
            if (matches(header, ZIP_HEADER)) {
                // Could be DOCX, XLSX, or generic ZIP
                if (fallbackMimeType != null && fallbackMimeType.contains("wordprocessingml")) {
                    return fallbackMimeType;
                }
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            return fallbackMimeType != null ? fallbackMimeType : "application/octet-stream";
        } catch (IOException e) {
            return fallbackMimeType != null ? fallbackMimeType : "application/octet-stream";
        }
    }

    public boolean isValidSignatureForExtension(InputStream inputStream, String extension) {
        if (extension == null || inputStream == null) return true;
        String ext = extension.toLowerCase().trim();
        if (!ext.startsWith(".")) ext = "." + ext;

        try {
            if (!inputStream.markSupported()) return true;
            inputStream.mark(32);
            byte[] header = new byte[16];
            int read = inputStream.read(header);
            inputStream.reset();

            if (read < 3) return true;

            switch (ext) {
                case ".pdf":
                    return matches(header, PDF_HEADER);
                case ".png":
                    return matches(header, PNG_HEADER);
                case ".jpg":
                case ".jpeg":
                    return matches(header, JPEG_HEADER_1);
                case ".tiff":
                case ".tif":
                    return matches(header, TIFF_LE_HEADER) || matches(header, TIFF_BE_HEADER);
                case ".docx":
                    return matches(header, ZIP_HEADER);
                case ".txt":
                case ".csv":
                case ".json":
                    // Text files have no fixed binary signature; valid as long as not raw binary control chars
                    return isTextFormat(header, read);
                default:
                    return true;
            }
        } catch (IOException e) {
            return true;
        }
    }

    private boolean isTextFormat(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) {
            byte b = bytes[i];
            if (b == 0) return false; // Null byte indicates binary file
        }
        return true;
    }

    private boolean matches(byte[] data, byte[] signature) {
        if (data == null || signature == null || data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
