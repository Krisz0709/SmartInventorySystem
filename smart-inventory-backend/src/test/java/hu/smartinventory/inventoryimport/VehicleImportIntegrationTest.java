package hu.smartinventory.inventoryimport;

import hu.smartinventory.cost.repository.VehicleCostEntryRepository;
import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.entity.ImportType;
import hu.smartinventory.inventoryimport.repository.ImportBatchRepository;
import hu.smartinventory.inventoryimport.service.HashService;
import hu.smartinventory.inventoryimport.service.VehicleFileImportService;
import hu.smartinventory.vehicle.entity.Vehicle;
import hu.smartinventory.vehicle.repository.VehicleRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VehicleImportIntegrationTest {

    private static final String TEST_VIN =
            "WBATEST0000000001";

    @Autowired
    private VehicleFileImportService vehicleFileImportService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleCostEntryRepository costEntryRepository;

    @Autowired
    private ImportBatchRepository importBatchRepository;

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

        assertTrue(
                importBatchRepository
                        .existsByImportTypeAndFileHash(
                                ImportType.VEHICLE,
                                fileHash
                        )
        );
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