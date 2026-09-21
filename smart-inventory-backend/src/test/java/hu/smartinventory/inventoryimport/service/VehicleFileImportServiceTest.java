package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.dto.VehicleImportRow;
import hu.smartinventory.inventoryimport.entity.ImportBatch;
import hu.smartinventory.inventoryimport.entity.ImportType;
import hu.smartinventory.inventoryimport.repository.ImportBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VehicleFileImportServiceTest {

    private HashService hashService;
    private VehicleExcelParser vehicleExcelParser;
    private ImportBatchRepository importBatchRepository;
    private VehicleImportService vehicleImportService;

    private VehicleFileImportService vehicleFileImportService;

    @BeforeEach
    void setUp() {

        hashService = mock(HashService.class);
        vehicleExcelParser = mock(VehicleExcelParser.class);
        importBatchRepository = mock(ImportBatchRepository.class);
        vehicleImportService = mock(VehicleImportService.class);

        vehicleFileImportService =
                new VehicleFileImportService(
                        hashService,
                        vehicleExcelParser,
                        importBatchRepository,
                        vehicleImportService
                );
    }

    @Test
    void shouldImportFileAndCountInsertedAndSkippedRows()
            throws Exception {

        byte[] fileContent = {1, 2, 3};

        VehicleImportRow row1 = createRow("WBA12345678901231");
        VehicleImportRow row2 = createRow("WBA12345678901232");
        VehicleImportRow row3 = createRow("WBA12345678901233");
        VehicleImportRow row4 = createRow("WBA12345678901234");

        when(hashService.sha256(any(InputStream.class)))
                .thenReturn("file-hash-123");

        when(importBatchRepository
                .existsByImportTypeAndFileHash(
                        ImportType.VEHICLE,
                        "file-hash-123"
                ))
                .thenReturn(false);

        when(vehicleExcelParser.parse(any(InputStream.class)))
                .thenReturn(List.of(
                        row1,
                        row2,
                        row3,
                        row4
                ));

        when(importBatchRepository.save(any(ImportBatch.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(vehicleImportService.processRow(
                any(VehicleImportRow.class),
                any(ImportBatch.class)
        ))
                .thenReturn(
                        true,
                        false,
                        true,
                        false
                );

        VehicleImportResult result =
                vehicleFileImportService.importFile(
                        "keszlet.xlsx",
                        fileContent
                );

        assertEquals("keszlet.xlsx", result.originalFilename());
        assertEquals(4, result.totalRows());
        assertEquals(2, result.insertedRows());
        assertEquals(2, result.skippedRows());
        assertEquals(0, result.rejectedRows());
    }

    @Test
    void shouldRejectAlreadyImportedFile()
            throws Exception {

        byte[] fileContent = {1, 2, 3};

        when(hashService.sha256(any(InputStream.class)))
                .thenReturn("existing-hash");

        when(importBatchRepository
                .existsByImportTypeAndFileHash(
                        ImportType.VEHICLE,
                        "existing-hash"
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> vehicleFileImportService.importFile(
                                "keszlet.xlsx",
                                fileContent
                        )
                );

        assertEquals(
                "Ezt a fájlt már korábban importáltuk.",
                exception.getMessage()
        );

        verify(vehicleExcelParser, never())
                .parse(any(InputStream.class));

        verify(vehicleImportService, never())
                .processRow(any(), any());

        verify(importBatchRepository, never())
                .save(any());
    }

    private VehicleImportRow createRow(String vin) {

        return new VehicleImportRow(
                LocalDate.of(2026, 7, 15),
                "BIZ-123",
                "BMW AG",
                "EXT-987",
                vin,
                "BMW 320d",
                "ABC-123",
                "BMW",
                new BigDecimal("12350000.00"),
                47
        );
    }
}