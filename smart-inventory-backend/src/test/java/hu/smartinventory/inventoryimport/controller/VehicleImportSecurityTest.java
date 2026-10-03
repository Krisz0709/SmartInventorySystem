package hu.smartinventory.inventoryimport.controller;

import hu.smartinventory.config.SecurityConfig;
import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.service.VehicleFileImportService;
import hu.smartinventory.inventoryimport.service.VehicleImportHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleImportController.class)
@Import(SecurityConfig.class)
class VehicleImportSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleFileImportService vehicleFileImportService;

    @MockitoBean
    private VehicleImportHistoryService vehicleImportHistoryService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRejectUnauthenticatedUser() throws Exception {

        MockMultipartFile file = createFile();

        mockMvc.perform(
                        multipart("/api/v1/imports/vehicles")
                                .file(file)
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectNormalUser() throws Exception {

        MockMultipartFile file = createFile();

        mockMvc.perform(
                        multipart("/api/v1/imports/vehicles")
                                .file(file)
                                .with(csrf())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdmin() throws Exception {

        MockMultipartFile file = createFile();

        when(vehicleFileImportService.importFile(
                anyString(),
                any(byte[].class)
        )).thenReturn(
                new VehicleImportResult(
                        "keszlet.xlsx",
                        10,
                        3,
                        7,
                        0
                )
        );

        mockMvc.perform(
                        multipart("/api/v1/imports/vehicles")
                                .file(file)
                                .with(csrf())
                )
                .andExpect(status().isOk());
    }

    private MockMultipartFile createFile() {

        return new MockMultipartFile(
                "file",
                "keszlet.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3}
        );
    }
}
