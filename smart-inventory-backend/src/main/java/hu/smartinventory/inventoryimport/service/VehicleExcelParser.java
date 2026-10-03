package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.inventoryimport.dto.VehicleImportRow;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import hu.smartinventory.inventoryimport.dto.VehicleExcelParseResult;
import hu.smartinventory.inventoryimport.dto.VehicleImportRowError;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VehicleExcelParser {

    private static final List<String> REQUIRED_HEADERS = List.of(
            "Könyvelési dátum",
            "Bizonylatszám",
            "Szállító neve",
            "Külső bizonylatszám",
            "Alvázszám",
            "Típus",
            "Rendszám",
            "Gyártmány",
            "Összeg"
    );

    private void validateRow(VehicleImportRow row) {

        if (row.vin() == null
                || row.vin().trim().length() != 17) {

            throw new IllegalArgumentException(
                    "Az alvázszámnak 17 karakteresnek kell lennie."
            );
        }

        if (row.accountingDate() == null) {

            throw new IllegalArgumentException(
                    "A könyvelési dátum kötelező."
            );
        }

        if (row.amount() == null) {

            throw new IllegalArgumentException(
                    "Az összeg kötelező."
            );
        }
    }

    public VehicleExcelParseResult parse(InputStream inputStream)
            throws IOException {

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            int headerRowIndex = findHeaderRow(sheet);

            Row headerRow = sheet.getRow(headerRowIndex);

            Map<String, Integer> columns =
                    buildColumnMap(headerRow);

            List<VehicleImportRow> result =
                    new ArrayList<>();

            List<VehicleImportRowError> errors =
                    new ArrayList<>();

            for (
                    int rowIndex = headerRowIndex + 1;
                    rowIndex <= sheet.getLastRowNum();
                    rowIndex++
            ) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                String vin =
                        getText(
                                row,
                                columns.get("Alvázszám")
                        );

                /*
                 * VIN nélküli sorokat nem tekintünk autósornak.
                 *
                 * Ez azért fontos, mert az ERP Excelben lehet például
                 * összesítő sor a táblázat végén.
                 */
                if (vin.isBlank()) {
                    continue;
                }

                try {

                    VehicleImportRow importRow =
                            parseRow(row, columns);

                    validateRow(importRow);

                    result.add(importRow);

                } catch (IllegalArgumentException exception) {

                    errors.add(
                            new VehicleImportRowError(
                                    row.getRowNum() + 1,
                                    exception.getMessage()
                            )
                    );
                }
            }

            return new VehicleExcelParseResult(
                    result,
                    errors
            );
        }
    }

    private int findHeaderRow(Sheet sheet) {

        for (Row row : sheet) {

            Map<String, Integer> columns = buildColumnMap(row);

            if (columns.keySet().containsAll(REQUIRED_HEADERS)) {
                return row.getRowNum();
            }
        }

        throw new IllegalArgumentException(
                "Nem található a szükséges fejléc az Excel fájlban."
        );
    }

    private Map<String, Integer> buildColumnMap(Row row) {

        Map<String, Integer> columns = new HashMap<>();
        DataFormatter formatter = new DataFormatter();

        for (Cell cell : row) {

            String headerName = formatter
                    .formatCellValue(cell)
                    .trim();

            if (!headerName.isBlank()) {
                columns.put(headerName, cell.getColumnIndex());
            }
        }

        return columns;
    }

    private String getText(Row row, int columnIndex) {

        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();

        return formatter
                .formatCellValue(cell)
                .trim();
    }

    private LocalDate getDate(Row row, int columnIndex) {

        Cell cell = row.getCell(columnIndex);

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {

            return cell.getLocalDateTimeCellValue()
                    .toLocalDate();
        }

        throw new IllegalArgumentException(
                "Érvénytelen dátum az Excel "
                        + (row.getRowNum() + 1)
                        + ". sorában."
        );
    }

    private BigDecimal getBigDecimal(Row row, int columnIndex) {

        Cell cell = row.getCell(columnIndex);

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(
                    cell.getNumericCellValue()
            );
        }

        if (cell.getCellType() == CellType.STRING) {

            String value = cell.getStringCellValue()
                    .trim()
                    .replace(" ", "")
                    .replace(",", ".");

            return new BigDecimal(value);
        }

        throw new IllegalArgumentException(
                "Érvénytelen összeg az Excel "
                        + (row.getRowNum() + 1)
                        + ". sorában."
        );
    }

    private VehicleImportRow parseRow(
            Row row,
            Map<String, Integer> columns
    ) {

        return new VehicleImportRow(

                getDate(
                        row,
                        columns.get("Könyvelési dátum")
                ),

                getText(
                        row,
                        columns.get("Bizonylatszám")
                ),

                getText(
                        row,
                        columns.get("Szállító neve")
                ),

                getText(
                        row,
                        columns.get("Külső bizonylatszám")
                ),

                getText(
                        row,
                        columns.get("Alvázszám")
                ),

                getText(
                        row,
                        columns.get("Típus")
                ),

                getText(
                        row,
                        columns.get("Rendszám")
                ),

                getText(
                        row,
                        columns.get("Gyártmány")
                ),

                getBigDecimal(
                        row,
                        columns.get("Összeg")
                ),

                row.getRowNum() + 1
        );
    }
}