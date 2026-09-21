package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.inventoryimport.dto.VehicleImportRow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SourceFingerprintServiceTest {

    private final HashService hashService =
            new HashService();

    private final SourceFingerprintService fingerprintService =
            new SourceFingerprintService(hashService);

    @Test
    void shouldReturnSameFingerprintForSameLogicalRow() {

        VehicleImportRow first = new VehicleImportRow(
                LocalDate.of(2026, 7, 15),
                "BIZ-123",
                "BMW AG",
                "EXT-987",
                "WBA12345678901234",
                "BMW 320d",
                "ABC-123",
                "BMW",
                new BigDecimal("12350000.00"),
                10
        );

        VehicleImportRow second = new VehicleImportRow(
                LocalDate.of(2026, 7, 15),
                " biz-123 ",
                "bmw ag",
                "ext-987",
                "wba12345678901234",
                "BMW 320d",
                "ABC-123",
                "BMW",
                new BigDecimal("12350000.0"),
                78
        );

        String firstFingerprint =
                fingerprintService.forVehicleRow(first);

        String secondFingerprint =
                fingerprintService.forVehicleRow(second);

        assertEquals(
                firstFingerprint,
                secondFingerprint
        );
    }

    @Test
    void shouldReturnDifferentFingerprintWhenAmountChanges() {

        VehicleImportRow first = new VehicleImportRow(
                LocalDate.of(2026, 7, 15),
                "BIZ-123",
                "BMW AG",
                "EXT-987",
                "WBA12345678901234",
                "BMW 320d",
                "ABC-123",
                "BMW",
                new BigDecimal("12350000"),
                10
        );

        VehicleImportRow second = new VehicleImportRow(
                LocalDate.of(2026, 7, 15),
                "BIZ-123",
                "BMW AG",
                "EXT-987",
                "WBA12345678901234",
                "BMW 320d",
                "ABC-123",
                "BMW",
                new BigDecimal("12400000"),
                10
        );

        String firstFingerprint =
                fingerprintService.forVehicleRow(first);

        String secondFingerprint =
                fingerprintService.forVehicleRow(second);

        assertNotEquals(
                firstFingerprint,
                secondFingerprint
        );
    }
}