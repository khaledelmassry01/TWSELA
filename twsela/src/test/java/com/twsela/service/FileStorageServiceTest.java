package com.twsela.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private final FileStorageService fileStorageService = new FileStorageService();

    @Nested
    @DisplayName("storeFile — تخزين الملفات")
    class StoreFileTests {

        @Test
        @DisplayName("يجب رفض ملف فارغ")
        void storeFile_emptyFile() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            assertThatThrownBy(() -> fileStorageService.storeFile(file, "test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("فارغ");
        }

        @Test
        @DisplayName("يجب رفض ملف يتجاوز الحد الأقصى (5 ميجابايت)")
        void storeFile_tooLarge() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(6 * 1024 * 1024L); // 6 MB

            assertThatThrownBy(() -> fileStorageService.storeFile(file, "test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("5");
        }

        @Test
        @DisplayName("يجب رفض نوع ملف غير مسموح")
        void storeFile_invalidType() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("application/pdf");

            assertThatThrownBy(() -> fileStorageService.storeFile(file, "test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("نوع الملف غير مسموح");
        }

        @Test
        @DisplayName("يجب تخزين ملف صورة بنجاح")
        void storeFile_success() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

            String path = fileStorageService.storeFile(file, "test-dir");

            assertThat(path).startsWith("/uploads/test-dir/");
            assertThat(path).endsWith(".jpg");
        }
    }

    @Nested
    @DisplayName("deleteFile — حذف الملفات")
    class DeleteFileTests {

        @Test
        @DisplayName("يجب إرجاع false عند محاولة حذف ملف غير موجود")
        void deleteFile_notExists() {
            boolean result = fileStorageService.deleteFile("/uploads/nonexistent/file.jpg");
            assertThat(result).isFalse();
        }
    }
}
