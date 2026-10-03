package hu.smartinventory.inventoryimport;

import hu.smartinventory.inventoryimport.dto.VehicleImportHistoryResponse;
import hu.smartinventory.cost.repository.VehicleCostEntryRepository;
import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.entity.ImportRowError;
import hu.smartinventory.inventoryimport.entity.ImportType;
import hu.smartinventory.inventoryimport.repository.ImportBatchRepository;
import hu.smartinventory.inventoryimport.repository.ImportRowErrorRepository;
import hu.smartinventory.inventoryimport.service.HashService;
import hu.smartinventory.inventoryimport.service.VehicleFileImportService;
import hu.smartinventory.inventoryimport.service.VehicleImportHistoryService;
import hu.smartinventory.vehicle.dto.VehicleResponse;
import hu.smartinventory.vehicle.entity.Vehicle;
import hu.smartinventory.vehicle.repository.VehicleRepository;
import hu.smartinventory.vehicle.service.VehicleService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VehicleImportIntegrationTest {

    private static final String TEST_VIN =
            "WBATEST0000000001";

    @Autowired
    private VehicleFileImportService vehicleFileImportService;

    @Autowired
    private VehicleImportHistoryService vehicleImportHistoryService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleCostEntryRepository costEntryRepository;

    @Autowired
    private ImportBatchRepository importBatchRepository;

    @Autowired
    private ImportRowErrorRepository importRowErrorRepository;

    @Autowired
    private HashService hashService;

    @Test
    void shouldImportTwoCostRowsForOneVehicle()
            throws Exception {

        byte[] excelFile = createExcel();

        String fileHash = hashService.sha256(
                new ByteArrayInputStream(excelFile)
        );

        VehicleImportResult result =
                vehicleFileImportService.importFile(
                        "integration-test.xlsx",
                        excelFile
                );

        assertEquals(2, result.totalRows());
        assertEquals(2, result.insertedRows());
        assertEquals(0, result.skippedRows());
        assertEquals(0, result.rejectedRows());

        Vehicle vehicle =
                vehicleRepository
                        .findByVinIgnoreCase(TEST_VIN)
                        .orElseThrow();

        assertEquals(TEST_VIN, vehicle.getVin());
        assertEquals("BMW", vehicle.getBrand());
        assertEquals("BMW 320d", vehicle.getTypeName());

        long costEntryCount =
                costEntryRepository
                        .countByVehicle_Id(vehicle.getId());

        assertEquals(2, costEntryCount);

        VehicleResponse vehicleResponse =
                vehicleService.searchByVin(TEST_VIN)
                        .getFirst();

        assertEquals(
                0,
                new BigDecimal("10350000.00")
                        .compareTo(vehicleResponse.totalCostAmount())
        );

        assertTrue(
                importBatchRepository
                        .existsByImportTypeAndFileHash(
                                ImportType.VEHICLE,
                                fileHash
                        )
        );
    }

    @Test
    void shouldRejectSameFileWhenImportedTwice()
            throws Exception {

        byte[] excelFile = createExcel();

        VehicleImportResult firstResult =
                vehicleFileImportService.importFile(
                        "integration-test.xlsx",
                        excelFile
                );

        assertEquals(2, firstResult.insertedRows());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> vehicleFileImportService.importFile(
                                "integration-test.xlsx",
                                excelFile
                        )
                );

        assertEquals(
                "Ezt a fájlt már korábban importáltuk.",
                exception.getMessage()
        );
    }

    @Test
    void shouldSkipPreviouslyImportedRowFromDifferentFile()
            throws Exception {

        byte[] firstExcelFile = createExcel();

        VehicleImportResult firstResult =
                vehicleFileImportService.importFile(
                        "first-import.xlsx",
                        firstExcelFile
                );

        assertEquals(2, firstResult.insertedRows());

        byte[] secondExcelFile = createSecondExcel();

        VehicleImportResult secondResult =
                vehicleFileImportService.importFile(
                        "second-import.xlsx",
                        secondExcelFile
                );

        assertEquals(2, secondResult.totalRows());
        assertEquals(1, secondResult.insertedRows());
        assertEquals(1, secondResult.skippedRows());
        assertEquals(0, secondResult.rejectedRows());

        Vehicle vehicle =
                vehicleRepository
                        .findByVinIgnoreCase(TEST_VIN)
                        .orElseThrow();

        long costEntryCount =
                costEntryRepository
                        .countByVehicle_Id(vehicle.getId());

        assertEquals(3, costEntryCount);
    }

    @Test
    void shouldCountRejectedRowsAndImportOnlyValidRows()
            throws Exception {

        byte[] excelFile =
                createExcelWithInvalidRows();

        VehicleImportResult result =
                vehicleFileImportService.importFile(
                        "invalid-rows-test.xlsx",
                        excelFile
                );

        assertEquals(4, result.totalRows());
        assertEquals(2, result.insertedRows());
        assertEquals(0, result.skippedRows());
        assertEquals(2, result.rejectedRows());

        List<ImportRowError> rowErrors =
                importRowErrorRepository
                        .findAllByImportBatch_OriginalFilenameOrderBySourceRowNumberAsc(
                                "invalid-rows-test.xlsx"
                        );

        assertEquals(2, rowErrors.size());
        assertEquals(3, rowErrors.get(0).getSourceRowNumber());
        assertEquals(
                result.errors().get(0).message(),
                rowErrors.get(0).getErrorMessage()
        );
        assertEquals(4, rowErrors.get(1).getSourceRowNumber());
        assertEquals(
                result.errors().get(1).message(),
                rowErrors.get(1).getErrorMessage()
        );

        List<VehicleImportHistoryResponse> history =
                vehicleImportHistoryService.findVehicleImportHistory();

        assertEquals(1, history.size());
        assertEquals(
                "invalid-rows-test.xlsx",
                history.getFirst().originalFilename()
        );
        assertEquals(4, history.getFirst().totalRows());
        assertEquals(2, history.getFirst().insertedRows());
        assertEquals(2, history.getFirst().rejectedRows());
        assertEquals(2, history.getFirst().errors().size());
        assertEquals(
                result.errors().get(0),
                history.getFirst().errors().getFirst()
        );

        Vehicle vehicle =
                vehicleRepository
                        .findByVinIgnoreCase(TEST_VIN)
                        .orElseThrow();

        long costEntryCount =
                costEntryRepository
                        .countByVehicle_Id(vehicle.getId());

        assertEquals(2, costEntryCount);
    }

    private byte[] createExcel() throws Exception {

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet("Készlet");

            Row header = sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("Könyvelési dátum");

            header.createCell(1)
                    .setCellValue("Bizonylatszám");

            header.createCell(2)
                    .setCellValue("Szállító neve");

            header.createCell(3)
                    .setCellValue("Külső bizonylatszám");

            header.createCell(4)
                    .setCellValue("Alvázszám");

            header.createCell(5)
                    .setCellValue("Típus");

            header.createCell(6)
                    .setCellValue("Rendszám");

            header.createCell(7)
                    .setCellValue("Gyártmány");

            header.createCell(8)
                    .setCellValue("Összeg");


            CellStyle dateStyle =
                    workbook.createCellStyle();

            dateStyle.setDataFormat(
                    workbook.getCreationHelper()
                            .createDataFormat()
                            .getFormat("yyyy-mm-dd")
            );


            Row firstRow = sheet.createRow(1);

            setDate(
                    firstRow,
                    0,
                    LocalDate.of(2026, 9, 21),
                    dateStyle
            );

            firstRow.createCell(1)
                    .setCellValue("TEST-001");

            firstRow.createCell(2)
                    .setCellValue("BMW TEST");

            firstRow.createCell(3)
                    .setCellValue("EXT-001");

            firstRow.createCell(4)
                    .setCellValue(TEST_VIN);

            firstRow.createCell(5)
                    .setCellValue("BMW 320d");

            firstRow.createCell(6)
                    .setCellValue("TESZT-01");

            firstRow.createCell(7)
                    .setCellValue("BMW");

            firstRow.createCell(8)
                    .setCellValue(10_000_000);


            Row secondRow = sheet.createRow(2);

            setDate(
                    secondRow,
                    0,
                    LocalDate.of(2026, 9, 22),
                    dateStyle
            );

            secondRow.createCell(1)
                    .setCellValue("TEST-002");

            secondRow.createCell(2)
                    .setCellValue("BMW TEST");

            secondRow.createCell(3)
                    .setCellValue("EXT-002");

            secondRow.createCell(4)
                    .setCellValue(TEST_VIN);

            secondRow.createCell(5)
                    .setCellValue("BMW 320d");

            secondRow.createCell(6)
                    .setCellValue("TESZT-01");

            secondRow.createCell(7)
                    .setCellValue("BMW");

            secondRow.createCell(8)
                    .setCellValue(350_000);


            workbook.write(output);

            return output.toByteArray();
        }
    }

    private byte[] createSecondExcel() throws Exception {

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet("Készlet");

            Row header = sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("Könyvelési dátum");

            header.createCell(1)
                    .setCellValue("Bizonylatszám");

            header.createCell(2)
                    .setCellValue("Szállító neve");

            header.createCell(3)
                    .setCellValue("Külső bizonylatszám");

            header.createCell(4)
                    .setCellValue("Alvázszám");

            header.createCell(5)
                    .setCellValue("Típus");

            header.createCell(6)
                    .setCellValue("Rendszám");

            header.createCell(7)
                    .setCellValue("Gyártmány");

            header.createCell(8)
                    .setCellValue("Összeg");


            CellStyle dateStyle =
                    workbook.createCellStyle();

            dateStyle.setDataFormat(
                    workbook.getCreationHelper()
                            .createDataFormat()
                            .getFormat("yyyy-mm-dd")
            );


            // Ez pontosan ugyanaz a könyvelési sor,
            // mint az első fájl TEST-001 sora.
            Row duplicateRow = sheet.createRow(1);

            setDate(
                    duplicateRow,
                    0,
                    LocalDate.of(2026, 9, 21),
                    dateStyle
            );

            duplicateRow.createCell(1)
                    .setCellValue("TEST-001");

            duplicateRow.createCell(2)
                    .setCellValue("BMW TEST");

            duplicateRow.createCell(3)
                    .setCellValue("EXT-001");

            duplicateRow.createCell(4)
                    .setCellValue(TEST_VIN);

            duplicateRow.createCell(5)
                    .setCellValue("BMW 320d");

            duplicateRow.createCell(6)
                    .setCellValue("TESZT-01");

            duplicateRow.createCell(7)
                    .setCellValue("BMW");

            duplicateRow.createCell(8)
                    .setCellValue(10_000_000);


            // Ez viszont új könyvelési sor.
            Row newRow = sheet.createRow(2);

            setDate(
                    newRow,
                    0,
                    LocalDate.of(2026, 9, 23),
                    dateStyle
            );

            newRow.createCell(1)
                    .setCellValue("TEST-003");

            newRow.createCell(2)
                    .setCellValue("BMW TEST");

            newRow.createCell(3)
                    .setCellValue("EXT-003");

            newRow.createCell(4)
                    .setCellValue(TEST_VIN);

            newRow.createCell(5)
                    .setCellValue("BMW 320d");

            newRow.createCell(6)
                    .setCellValue("TESZT-01");

            newRow.createCell(7)
                    .setCellValue("BMW");

            newRow.createCell(8)
                    .setCellValue(500_000);


            workbook.write(output);

            return output.toByteArray();
        }
    }

    private byte[] createExcelWithInvalidRows()
            throws Exception {

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet("Készlet");

            Row header = sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("Könyvelési dátum");

            header.createCell(1)
                    .setCellValue("Bizonylatszám");

            header.createCell(2)
                    .setCellValue("Szállító neve");

            header.createCell(3)
                    .setCellValue("Külső bizonylatszám");

            header.createCell(4)
                    .setCellValue("Alvázszám");

            header.createCell(5)
                    .setCellValue("Típus");

            header.createCell(6)
                    .setCellValue("Rendszám");

            header.createCell(7)
                    .setCellValue("Gyártmány");

            header.createCell(8)
                    .setCellValue("Összeg");


            CellStyle dateStyle =
                    workbook.createCellStyle();

            dateStyle.setDataFormat(
                    workbook.getCreationHelper()
                            .createDataFormat()
                            .getFormat("yyyy-mm-dd")
            );


            // 1. JÓ SOR
            Row goodRow1 = sheet.createRow(1);

            setDate(
                    goodRow1,
                    0,
                    LocalDate.of(2026, 9, 23),
                    dateStyle
            );

            goodRow1.createCell(1)
                    .setCellValue("GOOD-001");

            goodRow1.createCell(2)
                    .setCellValue("BMW TEST");

            goodRow1.createCell(3)
                    .setCellValue("EXT-GOOD-001");

            goodRow1.createCell(4)
                    .setCellValue(TEST_VIN);

            goodRow1.createCell(5)
                    .setCellValue("BMW 320d");

            goodRow1.createCell(6)
                    .setCellValue("TESZT-01");

            goodRow1.createCell(7)
                    .setCellValue("BMW");

            goodRow1.createCell(8)
                    .setCellValue(10_000_000);


            // 2. HIBÁS SOR - a VIN nem 17 karakter
            Row invalidVinRow = sheet.createRow(2);

            setDate(
                    invalidVinRow,
                    0,
                    LocalDate.of(2026, 9, 23),
                    dateStyle
            );

            invalidVinRow.createCell(1)
                    .setCellValue("BAD-001");

            invalidVinRow.createCell(2)
                    .setCellValue("BMW TEST");

            invalidVinRow.createCell(3)
                    .setCellValue("EXT-BAD-001");

            invalidVinRow.createCell(4)
                    .setCellValue("ABC123");

            invalidVinRow.createCell(5)
                    .setCellValue("BMW X1");

            invalidVinRow.createCell(6)
                    .setCellValue("BAD-01");

            invalidVinRow.createCell(7)
                    .setCellValue("BMW");

            invalidVinRow.createCell(8)
                    .setCellValue(8_000_000);


            // 3. HIBÁS SOR - nincs Összeg
            Row missingAmountRow = sheet.createRow(3);

            setDate(
                    missingAmountRow,
                    0,
                    LocalDate.of(2026, 9, 23),
                    dateStyle
            );

            missingAmountRow.createCell(1)
                    .setCellValue("BAD-002");

            missingAmountRow.createCell(2)
                    .setCellValue("BMW TEST");

            missingAmountRow.createCell(3)
                    .setCellValue("EXT-BAD-002");

            missingAmountRow.createCell(4)
                    .setCellValue("WBA12345678909999");

            missingAmountRow.createCell(5)
                    .setCellValue("BMW X3");

            missingAmountRow.createCell(6)
                    .setCellValue("BAD-02");

            missingAmountRow.createCell(7)
                    .setCellValue("BMW");

            // Szándékosan NEM hozunk létre Összeg cellát.


            // 4. JÓ SOR
            Row goodRow2 = sheet.createRow(4);

            setDate(
                    goodRow2,
                    0,
                    LocalDate.of(2026, 9, 24),
                    dateStyle
            );

            goodRow2.createCell(1)
                    .setCellValue("GOOD-002");

            goodRow2.createCell(2)
                    .setCellValue("BMW TEST");

            goodRow2.createCell(3)
                    .setCellValue("EXT-GOOD-002");

            goodRow2.createCell(4)
                    .setCellValue(TEST_VIN);

            goodRow2.createCell(5)
                    .setCellValue("BMW 320d");

            goodRow2.createCell(6)
                    .setCellValue("TESZT-01");

            goodRow2.createCell(7)
                    .setCellValue("BMW");

            goodRow2.createCell(8)
                    .setCellValue(500_000);


            workbook.write(output);

            return output.toByteArray();
        }
    }

    private void setDate(
            Row row,
            int columnIndex,
            LocalDate date,
            CellStyle dateStyle
    ) {

        Cell cell = row.createCell(columnIndex);

        cell.setCellValue(date);
        cell.setCellStyle(dateStyle);
    }
}
