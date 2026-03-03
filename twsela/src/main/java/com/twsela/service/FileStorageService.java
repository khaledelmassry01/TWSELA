package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Generic file storage service. Stores files on the local filesystem.
 * Can be replaced by an S3-compatible implementation later.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final String BASE_UPLOAD_DIR = System.getProperty("app.upload.dir", "uploads/");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * Store a file in the specified sub-directory.
     *
     * @param file      the uploaded file
     * @param directory sub-directory under the base upload dir (e.g., "delivery-photos")
     * @return the relative path to the stored file
     */
    public String storeFile(MultipartFile file, String directory) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("الملف فارغ");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("حجم الملف يتجاوز الحد الأقصى (5 ميجابايت)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("نوع الملف غير مسموح به — فقط JPG, PNG, GIF, WEBP");
        }

        // Prepare directory
        Path uploadPath = Paths.get(BASE_UPLOAD_DIR, directory).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename).normalize();

        // Path traversal check
        if (!filePath.startsWith(uploadPath)) {
            throw new SecurityException("مسار ملف غير صالح");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.debug("File stored: {}", filePath);

        return "/uploads/" + directory + "/" + filename;
    }

    /**
     * Retrieve file bytes from storage.
     */
    public byte[] getFile(String relativePath) throws IOException {
        Path filePath = Paths.get(BASE_UPLOAD_DIR)
                .resolve(relativePath.replaceFirst("^/uploads/", ""))
                .toAbsolutePath().normalize();

        if (!Files.exists(filePath)) {
            throw new IOException("الملف غير موجود: " + relativePath);
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * Delete a file from storage.
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(BASE_UPLOAD_DIR)
                    .resolve(relativePath.replaceFirst("^/uploads/", ""))
                    .toAbsolutePath().normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", relativePath, e.getMessage());
            return false;
        }
    }

    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase();
            if (ext.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                return ext;
            }
        }
        return ".jpg";
    }
}
