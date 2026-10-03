package hu.smartinventory.inventoryimport.dto;

import hu.smartinventory.inventoryimport.entity.ImportBatch;

import java.time.Instant;
import java.util.List;

public record VehicleImportHistoryResponse(
        Long id,
        String originalFilename,
        Instant importedAt,
        int totalRows,
        int insertedRows,
        int skippedRows,
        int rejectedRows,
        List<VehicleImportRowError> errors
) {

    public VehicleImportHistoryResponse {
        errors = errors == null
                ? List.of()
                : List.copyOf(errors);
    }

    public static VehicleImportHistoryResponse from(
            ImportBatch importBatch,
            List<VehicleImportRowError> errors
    ) {
        return new VehicleImportHistoryResponse(
                importBatch.getId(),
                importBatch.getOriginalFilename(),
                importBatch.getImportedAt(),
                importBatch.getTotalRows(),
                importBatch.getInsertedRows(),
                importBatch.getSkippedRows(),
                importBatch.getRejectedRows(),
                errors
        );
    }
}
