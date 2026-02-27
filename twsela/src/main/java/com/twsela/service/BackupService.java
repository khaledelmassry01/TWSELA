package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    @Value("${backup.enabled:true}")
    private boolean backupEnabled;

    @Value("${backup.path:/var/backups/twsela}")
    private String backupPath;

    @Value("${backup.retention-days:30}")
    private int retentionDays;

    @Value("${spring.datasource.username:root}")
    private String dbUsername;

    @Value("${spring.datasource.password:root}")
    private String dbPassword;

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/twsela}")
    private String dbUrl;

    /**
     * Create database backup
     */
    public boolean createBackup() {
        if (!backupEnabled) {
            logger.info("Backup is disabled");
            return false;
        }

        try {
            // Extract database name from URL
            String dbName = extractDatabaseName(dbUrl);
            String host = extractHost(dbUrl);
            String port = extractPort(dbUrl);

            // Create backup directory if it doesn't exist
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
                logger.info("Created backup directory: {}", backupPath);
            }

            // Generate backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = String.format("twsela_backup_%s.sql", timestamp);
            String backupFilePath = Paths.get(backupPath, backupFileName).toString();

            // Build mysqldump command
            List<String> command = new ArrayList<>();
            command.add("mysqldump");
            command.add("-h" + host);
            command.add("-P" + port);
            command.add("-u" + dbUsername);
            command.add("--single-transaction");
            command.add("--routines");
            command.add("--triggers");
            command.add("--events");
            command.add("--add-drop-database");
            command.add("--databases");
            command.add(dbName);

            // Execute backup command — pass password via environment variable to avoid exposure in process listing
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("MYSQL_PWD", dbPassword);
            processBuilder.redirectOutput(new File(backupFilePath));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Database backup created successfully: {}", backupFilePath);
                
                // Compress backup file
                compressBackup(backupFilePath);
                
                // Clean old backups
                cleanOldBackups();
                
                return true;
            } else {
                logger.error("Database backup failed with exit code: {}", exitCode);
                return false;
            }

        } catch (Exception e) {
            logger.error("Failed to create database backup: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Restore database from backup
     */
    public boolean restoreBackup(String backupFilePath) {
        if (!backupEnabled) {
            logger.info("Backup is disabled");
            return false;
        }

        try {
            // Extract database connection details from URL
            String host = extractHost(dbUrl);
            String port = extractPort(dbUrl);

            // Build mysql command for restore
            List<String> command = new ArrayList<>();
            command.add("mysql");
            command.add("-h" + host);
            command.add("-P" + port);
            command.add("-u" + dbUsername);
            command.add("-p" + dbPassword);

            // Execute restore command
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectInput(new File(backupFilePath));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Database restored successfully from: {}", backupFilePath);
                return true;
            } else {
                logger.error("Database restore failed with exit code: {}", exitCode);
                return false;
            }

        } catch (Exception e) {
            logger.error("Failed to restore database backup: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Scheduled backup - runs daily at 2 AM
     */
    @Scheduled(cron = "${backup.schedule:0 0 2 * * ?}")
    public void scheduledBackup() {
        logger.info("Starting scheduled database backup");
        boolean success = createBackup();
        if (success) {
            logger.info("Scheduled backup completed successfully");
        } else {
            logger.error("Scheduled backup failed");
        }
    }

    /**
     * Compress backup file using gzip
     */
    private void compressBackup(String backupFilePath) {
        try {
            String compressedFilePath = backupFilePath + ".gz";
            
            List<String> command = new ArrayList<>();
            command.add("gzip");
            command.add(backupFilePath);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Backup compressed successfully: {}", compressedFilePath);
                // Delete original file
                Files.deleteIfExists(Paths.get(backupFilePath));
            } else {
                logger.warn("Failed to compress backup file");
            }

        } catch (Exception e) {
            logger.warn("Failed to compress backup file: {}", e.getMessage());
        }
    }

    /**
     * Clean old backup files based on retention policy
     */
    private void cleanOldBackups() {
        try {
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                return;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            
            Files.list(backupDir)
                .filter(path -> path.toString().endsWith(".sql.gz"))
                .filter(path -> {
                    try {
                        LocalDateTime fileTime = LocalDateTime.from(
                            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                                .parse(path.getFileName().toString()
                                    .replace("twsela_backup_", "")
                                    .replace(".sql.gz", ""))
                        );
                        return fileTime.isBefore(cutoffDate);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.info("Deleted old backup file: {}", path.getFileName());
                    } catch (IOException e) {
                        logger.warn("Failed to delete old backup file {}: {}", path.getFileName(), e.getMessage());
                    }
                });

        } catch (Exception e) {
            logger.warn("Failed to clean old backups: {}", e.getMessage());
        }
    }

    /**
     * Extract database name from JDBC URL
     */
    private String extractDatabaseName(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/twsela?useSSL=false...
        String[] parts = jdbcUrl.split("/");
        if (parts.length >= 4) {
            String dbPart = parts[3];
            if (dbPart.contains("?")) {
                return dbPart.split("\\?")[0];
            }
            return dbPart;
        }
        return "twsela";
    }

    /**
     * Extract host from JDBC URL
     */
    private String extractHost(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/twsela?useSSL=false...
        String[] parts = jdbcUrl.split("//");
        if (parts.length >= 2) {
            String hostPart = parts[1];
            if (hostPart.contains(":")) {
                return hostPart.split(":")[0];
            }
            return hostPart.split("/")[0];
        }
        return "localhost";
    }

    /**
     * Extract port from JDBC URL
     */
    private String extractPort(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/twsela?useSSL=false...
        String[] parts = jdbcUrl.split("//");
        if (parts.length >= 2) {
            String hostPart = parts[1];
            if (hostPart.contains(":")) {
                String[] hostPortParts = hostPart.split(":");
                if (hostPortParts.length >= 2) {
                    return hostPortParts[1].split("/")[0];
                }
            }
        }
        return "3306";
    }

    /**
     * Get backup status
     */
    public String getBackupStatus() {
        if (!backupEnabled) {
            return "Backup is disabled";
        }

        try {
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                return "Backup directory does not exist";
            }

            long backupCount = Files.list(backupDir)
                .filter(path -> path.toString().endsWith(".sql.gz"))
                .count();

            return String.format("Backup enabled. %d backup files found in %s", backupCount, backupPath);

        } catch (Exception e) {
            return "Error checking backup status: " + e.getMessage();
        }
    }
}
