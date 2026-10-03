package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.inventoryimport.dto.VehicleImportRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import hu.smartinventory.inventoryimport.dto.VehicleExcelParseResult;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VehicleExcelParserTest {

    private final VehicleExcelParser parser =
            new VehicleExcelParser();

    @Test
    void shouldParseVehicleExcelRow() throws Exception {

        byte[] excelFile = createTestExcel();

        VehicleExcelParseResult parseResult = parser.parse(
                new ByteArrayInputStream(excelFile)
        );

        List<VehicleImportRow> result = parseResult.rows();

        assertEquals(0, parseResult.rejectedRows());

        assertEquals(1, result.size());

        VehicleImportRow row = result.getFirst();

        assertEquals(
                LocalDate.of(2026, 7, 15),
                row.accountingDate()
        );

        assertEquals(
                "BIZ-123",
                row.documentNumber()
        );

        assertEquals(
                "BMW AG",
                row.supplierName()
        );

        assertEquals(
                "EXT-987",
                row.externalDocumentNumber()
        );

        assertEquals(
                "WBA12345678901234",
                row.vin()
        );

        assertEquals(
                "BMW 320d",
                row.typeName()
        );

        assertEquals(
                "ABC-123",
                row.registrationNumber()
        );

        assertEquals(
                "BMW",
                row.brand()
        );

        assertEquals(
                0,
                new BigDecimal("12350000.00")
                        .compareTo(row.amount())
        );

        assertEquals(
                3,
                row.sourceRowNumber()
        );
    }

    private byte[] createTestExcel() throws Exception {

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet = workbook.createSheet("Készlet");

            // Ez szándékosan NEM a fejléc.
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0)
                    .setCellValue("BMW készlet");

            // A fejléc a második Excel-sorban lesz.
            Row header = sheet.createRow(1);

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

            // Egy teszt jármű.
            Row dataRow = sheet.createRow(2);

            Cell dateCell = dataRow.createCell(0);

            dateCell.setCellValue(
                    LocalDate.of(2026, 7, 15)
            );

            CellStyle dateStyle = workbook.createCellStyle();

            dateStyle.setDataFormat(
                    workbook.getCreationHelper()
                            .createDataFormat()
                            .getFormat("yyyy-mm-dd")
            );

            dateCell.setCellStyle(dateStyle);

            dataRow.createCell(1)
                    .setCellValue("BIZ-123");

            dataRow.createCell(2)
                    .setCellValue("BMW AG");

            dataRow.createCell(3)
                    .setCellValue("EXT-987");

            dataRow.createCell(4)
                    .setCellValue("WBA12345678901234");

            dataRow.createCell(5)
                    .setCellValue("BMW 320d");

            dataRow.createCell(6)
                    .setCellValue("ABC-123");

            dataRow.createCell(7)
                    .setCellValue("BMW");

            dataRow.createCell(8)
                    .setCellValue(12_350_000);

            workbook.write(output);

            return output.toByteArray();
        }
    }

    @Test
    void shouldParseVehicleExcelWhenColumnsAreInDifferentOrder()
            throws Exception {

        byte[] excelFile = createTestExcelWithDifferentColumnOrder();

        VehicleExcelParseResult parseResult = parser.parse(
                new ByteArrayInputStream(excelFile)
        );

        List<VehicleImportRow> result = parseResult.rows();

        assertEquals(0, parseResult.rejectedRows());

        assertEquals(1, result.size());

        VehicleImportRow row = result.getFirst();

        assertEquals(
                "WBA12345678901234",
                row.vin()
        );

        assertEquals(
                "BMW",
                row.brand()
        );

        assertEquals(
                "BIZ-123",
                row.documentNumber()
        );

        assertEquals(
                0,
                new BigDecimal("12350000.00")
                        .compareTo(row.amount())
        );
    }

    private byte[] createTestExcelWithDifferentColumnOrder()
            throws Exception {

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet = workbook.createSheet("Készlet");

            Row header = sheet.createRow(0);

            // Szándékosan teljesen más sorrend!
            header.createCell(0)
                    .setCellValue("Alvázszám");

            header.createCell(1)
                    .setCellValue("Összeg");

            header.createCell(2)
                    .setCellValue("Gyártmány");

            header.createCell(3)
                    .setCellValue("Rendszám");

            header.createCell(4)
                    .setCellValue("Szállító neve");

            header.createCell(5)
                    .setCellValue("Könyvelési dátum");

            header.createCell(6)
                    .setCellValue("Típus");

            header.createCell(7)
                    .setCellValue("Külső bizonylatszám");

            header.createCell(8)
                    .setCellValue("Bizonylatszám");


            Row dataRow = sheet.createRow(1);

            dataRow.createCell(0)
                    .setCellValue("WBA12345678901234");

            dataRow.createCell(1)
                    .setCellValue(12_350_000);

            dataRow.createCell(2)
                    .setCellValue("BMW");

            dataRow.createCell(3)
                    .setCellValue("ABC-123");

            dataRow.createCell(4)
                    .setCellValue("BMW AG");


            Cell dateCell = dataRow.createCell(5);

            dateCell.setCellValue(
                    LocalDate.of(2026, 7, 15)
            );

            CellStyle dateStyle = workbook.createCellStyle();

            dateStyle.setDataFormat(
                    workbook.getCreationHelper()
                            .createDataFormat()
                            .getFormat("yyyy-mm-dd")
            );

            dateCell.setCellStyle(dateStyle);


            dataRow.createCell(6)
                    .setCellValue("BMW 320d");

            dataRow.createCell(7)
                    .setCellValue("EXT-987");

            dataRow.createCell(8)
                    .setCellValue("BIZ-123");

            workbook.write(output);

            return output.toByteArray();
        }
    }
}