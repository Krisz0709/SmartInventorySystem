package hu.smartinventory.inventoryimport.controller;

import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.service.VehicleFileImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleImportController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleFileImportService vehicleFileImportService;

    @Test
    void shouldImportXlsxFile() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "keszlet.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        new byte[]{1, 2, 3}
                );

        VehicleImportResult result =
                new VehicleImportResult(
                        "keszlet.xlsx",
                        10,
                        3,
                        7,
                        0
                );

        when(vehicleFileImportService.importFile(
                anyString(),
                any(byte[].class)
        )).thenReturn(result);

        mockMvc.perform(
                        multipart("/api/v1/imports/vehicles")
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.originalFilename")
                                .value("keszlet.xlsx")
                )
                .andExpect(
                        jsonPath("$.totalRows")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.insertedRows")
                                .value(3)
                )
                .andExpect(
                        jsonPath("$.skippedRows")
                                .value(7)
                )
                .andExpect(
                        jsonPath("$.rejectedRows")
                                .value(0)
                );
    }

    @Test
    void shouldRejectNonXlsxFile() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "keszlet.pdf",
                        "application/pdf",
                        new byte[]{1, 2, 3}
                );

        mockMvc.perform(
                        multipart("/api/v1/imports/vehicles")
                                .file(file)
                )
                .andExpect(status().isBadRequest());

        verify(
                vehicleFileImportService,
                never()
        ).importFile(
                anyString(),
                any(byte[].class)
        );
    }

    @Test
    void shouldRejectEmptyFile() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "keszlet.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        new byte[0]
                );

        mockMvc.perform(
                        multipart("/api/v1/imports/vehicles")
                                .file(file)
                )
                .andExpect(status().isBadRequest());

        verify(
                vehicleFileImportService,
                never()
        ).importFile(
                anyString(),
                any(byte[].class)
        );
    }
}