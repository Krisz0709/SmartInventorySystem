package hu.smartinventory.inventoryimport.dto;

public record VehicleImportRowError(
        int sourceRowNumber,
        String message
) {
}