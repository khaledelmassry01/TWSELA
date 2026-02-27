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

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    private static final String UPLOAD_DIR = System.getProperty("app.upload.dir", "uploads/pod/");
    private static final String PUBLIC_PATH = "/uploads/pod/";

    public String uploadPodImage(MultipartFile file, String trackingNumber) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Sanitize tracking number to prevent path traversal
        String sanitizedTrackingNumber = trackingNumber.replaceAll("[^a-zA-Z0-9_-]", "_");

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate filename using sanitized tracking number
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // Only allow safe extensions
            if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                throw new IllegalArgumentException("Unsupported image format");
            }
        }

        String filename = sanitizedTrackingNumber + fileExtension;
        Path filePath = uploadPath.resolve(filename).normalize();
        
        // Verify resolved path is still within upload directory (prevent path traversal)
        if (!filePath.startsWith(uploadPath)) {
            throw new SecurityException("Invalid file path detected");
        }

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return public path
        return PUBLIC_PATH + filename;
    }

    public boolean deletePodImage(String imagePath) {
        try {
            if (imagePath != null && imagePath.startsWith(PUBLIC_PATH)) {
                String filename = imagePath.substring(PUBLIC_PATH.length());
                Path filePath = Paths.get(UPLOAD_DIR + filename);
                return Files.deleteIfExists(filePath);
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
