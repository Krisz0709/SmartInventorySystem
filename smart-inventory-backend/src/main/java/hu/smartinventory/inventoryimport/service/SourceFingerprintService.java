package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.inventoryimport.dto.VehicleImportRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SourceFingerprintService {

    private final HashService hashService;

    public String forVehicleRow(VehicleImportRow row) {

        String source = String.join(
                "|",
                "VEHICLE",
                normalizeText(row.vin()),
                row.accountingDate().toString(),
                normalizeText(row.documentNumber()),
                normalizeText(row.supplierName()),
                normalizeText(row.externalDocumentNumber()),
                normalizeAmount(row.amount())
        );

        return hashService.sha256(source);
    }

    private String normalizeText(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase();
    }

    private String normalizeAmount(BigDecimal amount) {

        if (amount == null) {
            return "";
        }

        return amount
                .stripTrailingZeros()
                .toPlainString();
    }
}