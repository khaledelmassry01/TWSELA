package com.twsela.web;

import com.twsela.domain.Role;
import com.twsela.domain.Shipment;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.repository.ShipmentRepository;
import com.twsela.security.JwtService;
import com.twsela.service.AwbService;
import com.twsela.service.BarcodeService;
import com.twsela.service.FileUploadService;
import com.twsela.service.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LabelController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(LabelControllerTest.TestMethodSecurityConfig.class)
class LabelControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private ShipmentRepository shipmentRepository;
    @MockBean private PdfService pdfService;
    @MockBean private BarcodeService barcodeService;
    @MockBean private AwbService awbService;
    @MockBean private FileUploadService fileUploadService;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        Mockito.when(authHelper.getCurrentUser(any(Authentication.class)))
                .thenAnswer(inv -> (User) ((Authentication) inv.getArgument(0)).getPrincipal());

        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setTrackingNumber("TWS-20250101-000001");
    }

    private Authentication createAuth(String roleName) {
        Role role = new Role(roleName);
        role.setId(1L);
        UserStatus activeStatus = new UserStatus("ACTIVE");
        activeStatus.setId(1L);
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setPhone("0501234567");
        user.setRole(role);
        user.setStatus(activeStatus);
        user.setIsDeleted(false);
        return new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }

    @Test
    @DisplayName("GET /api/shipments/{id}/label — returns PDF")
    void getLabel_success() throws Exception {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(pdfService.generateShipmentLabel(any(Shipment.class))).thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46}); // %PDF

        mockMvc.perform(get("/api/shipments/1/label")
                        .with(authentication(createAuth("MERCHANT"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=label_TWS-20250101-000001.pdf"));
    }

    @Test
    @DisplayName("GET /api/shipments/{id}/label — returns 500 for missing shipment")
    void getLabel_notFound() throws Exception {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/shipments/999/label")
                        .with(authentication(createAuth("MERCHANT"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/shipments/{id}/barcode — returns PNG")
    void getBarcode_success() throws Exception {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        byte[] pngBytes = {(byte) 0x89, 0x50, 0x4E, 0x47};
        when(barcodeService.generateBarcode("TWS-20250101-000001")).thenReturn(pngBytes);

        mockMvc.perform(get("/api/shipments/1/barcode")
                        .with(authentication(createAuth("COURIER"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("GET /api/shipments/{id}/qrcode — returns PNG")
    void getQrCode_success() throws Exception {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        byte[] pngBytes = {(byte) 0x89, 0x50, 0x4E, 0x47};
        when(barcodeService.generateQrCode("TWS-20250101-000001")).thenReturn(pngBytes);

        mockMvc.perform(get("/api/shipments/1/qrcode")
                        .with(authentication(createAuth("COURIER"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("POST /api/shipments/{id}/pod — uploads proof of delivery")
    void uploadPod_success() throws Exception {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(fileUploadService.uploadPodImage(any(), eq("TWS-20250101-000001")))
                .thenReturn("/uploads/pod/TWS-20250101-000001.jpg");
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file", "pod.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/shipments/1/pod")
                        .file(file)
                        .param("podType", "PHOTO")
                        .with(authentication(createAuth("COURIER")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.podUrl").exists());
    }

    @Test
    @DisplayName("GET /api/shipments/{id}/pod — returns POD data")
    void getPod_success() throws Exception {
        testShipment.setPodData("/uploads/pod/image.jpg");
        testShipment.setPodType(Shipment.PodType.PHOTO);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));

        mockMvc.perform(get("/api/shipments/1/pod")
                        .with(authentication(createAuth("MERCHANT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.podUrl").value("/uploads/pod/image.jpg"));
    }

    @Test
    @DisplayName("GET /api/shipments/{id}/pod — returns 404 when no POD")
    void getPod_notFound() throws Exception {
        testShipment.setPodData(null);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));

        mockMvc.perform(get("/api/shipments/1/pod")
                        .with(authentication(createAuth("MERCHANT"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/shipments/labels/bulk — generates bulk labels PDF")
    void getBulkLabels_success() throws Exception {
        when(shipmentRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(testShipment));
        when(pdfService.generateBulkLabels(anyList())).thenReturn(new byte[]{0x25, 0x50});

        mockMvc.perform(post("/api/shipments/labels/bulk")
                        .with(authentication(createAuth("OWNER")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2]"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
