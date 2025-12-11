package com.twsela.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploadService {

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/pod/";
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

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate filename using tracking number
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = trackingNumber + fileExtension;
        Path filePath = uploadPath.resolve(filename);

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
