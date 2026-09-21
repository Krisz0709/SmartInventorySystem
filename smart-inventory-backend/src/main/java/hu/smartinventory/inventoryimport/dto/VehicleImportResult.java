package hu.smartinventory.inventoryimport.dto;

public record VehicleImportResult(
        String originalFilename,
        int totalRows,
        int insertedRows,
        int skippedRows,
        int rejectedRows
) {
}