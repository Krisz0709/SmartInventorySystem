package hu.smartinventory.inventoryimport.dto;

import java.util.List;

public record VehicleImportResult(
        String originalFilename,
        int totalRows,
        int insertedRows,
        int skippedRows,
        int rejectedRows,
        List<VehicleImportRowError> errors
) {

    public VehicleImportResult {

        errors = errors == null
                ? List.of()
                : List.copyOf(errors);
    }

    public VehicleImportResult(
            String originalFilename,
            int totalRows,
            int insertedRows,
            int skippedRows,
            int rejectedRows
    ) {
        this(
                originalFilename,
                totalRows,
                insertedRows,
                skippedRows,
                rejectedRows,
                List.of()
        );
    }
}