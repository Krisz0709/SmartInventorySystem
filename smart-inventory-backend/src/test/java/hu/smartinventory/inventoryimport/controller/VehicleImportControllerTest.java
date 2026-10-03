package hu.smartinventory.inventoryimport.controller;

import hu.smartinventory.inventoryimport.dto.VehicleImportHistoryResponse;
import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.service.VehicleFileImportService;
import hu.smartinventory.inventoryimport.service.VehicleImportHistoryService;
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
import hu.smartinventory.inventoryimport.dto.VehicleImportRowError;

import java.time.Instant;
import java.util.List;

@WebMvcTest(VehicleImportController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleFileImportService vehicleFileImportService;

    @MockitoBean
    private VehicleImportHistoryService vehicleImportHistoryService;

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

    @Test
    void shouldReturnRejectedRowErrors() throws Exception {

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
                        4,
                        2,
                        0,
                        2,
                        List.of(
                                new VehicleImportRowError(
                                        3,
                                        "Az alvázszámnak 17 karakteresnek kell lennie."
                                ),
                                new VehicleImportRowError(
                                        4,
                                        "Az összeg kötelező."
                                )
                        )
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
                        jsonPath("$.totalRows")
                                .value(4)
                )

                .andExpect(
                        jsonPath("$.insertedRows")
                                .value(2)
                )

                .andExpect(
                        jsonPath("$.rejectedRows")
                                .value(2)
                )

                .andExpect(
                        jsonPath("$.errors[0].sourceRowNumber")
                                .value(3)
                )

                .andExpect(
                        jsonPath("$.errors[0].message")
                                .value(
                                        "Az alvázszámnak 17 karakteresnek kell lennie."
                                )
                )

                .andExpect(
                        jsonPath("$.errors[1].sourceRowNumber")
                                .value(4)
                )

                .andExpect(
                        jsonPath("$.errors[1].message")
                                .value(
                                        "Az összeg kötelező."
                                )
                );
    }

    @Test
    void shouldReturnVehicleImportHistory() throws Exception {

        when(vehicleImportHistoryService.findVehicleImportHistory())
                .thenReturn(
                        List.of(
                                new VehicleImportHistoryResponse(
                                        12L,
                                        "keszlet.xlsx",
                                        Instant.parse(
                                                "2026-10-03T10:00:00Z"
                                        ),
                                        4,
                                        2,
                                        0,
                                        2,
                                        List.of(
                                                new VehicleImportRowError(
                                                        3,
                                                        "Az alvĂˇzszĂˇmnak 17 karakteresnek kell lennie."
                                                ),
                                                new VehicleImportRowError(
                                                        4,
                                                        "Az Ă¶sszeg kĂ¶telezĹ‘."
                                                )
                                        )
                                )
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/v1/imports/vehicles/history")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].id")
                                .value(12)
                )
                .andExpect(
                        jsonPath("$[0].originalFilename")
                                .value("keszlet.xlsx")
                )
                .andExpect(
                        jsonPath("$[0].totalRows")
                                .value(4)
                )
                .andExpect(
                        jsonPath("$[0].insertedRows")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].rejectedRows")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].errors[0].sourceRowNumber")
                                .value(3)
                )
                .andExpect(
                        jsonPath("$[0].errors[0].message")
                                .value(
                                        "Az alvĂˇzszĂˇmnak 17 karakteresnek kell lennie."
                                )
                );
    }
}
