package hu.smartinventory.inventoryimport.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VehicleImportRow(
        LocalDate accountingDate,
        String documentNumber,
        String supplierName,
        String externalDocumentNumber,
        String vin,
        String typeName,
        String registrationNumber,
        String brand,
        BigDecimal amount,
        int sourceRowNumber
) {
}