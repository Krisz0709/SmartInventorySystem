package hu.smartinventory.inventoryimport.dto;

import java.util.List;

public record VehicleExcelParseResult(
        List<VehicleImportRow> rows,
        List<VehicleImportRowError> errors
) {

    public VehicleExcelParseResult {
        rows = List.copyOf(rows);
        errors = List.copyOf(errors);
    }

    public int rejectedRows() {
        return errors.size();
    }
}