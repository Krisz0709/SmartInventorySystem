package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.cost.entity.VehicleCostEntry;
import hu.smartinventory.cost.repository.VehicleCostEntryRepository;
import hu.smartinventory.inventoryimport.dto.VehicleImportRow;
import hu.smartinventory.inventoryimport.entity.ImportBatch;
import hu.smartinventory.inventoryimport.entity.ImportType;
import hu.smartinventory.vehicle.entity.Vehicle;
import hu.smartinventory.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VehicleImportServiceTest {

    private VehicleRepository vehicleRepository;
    private VehicleCostEntryRepository costEntryRepository;
    private SourceFingerprintService fingerprintService;

    private VehicleImportService vehicleImportService;

    @BeforeEach
    void setUp() {

        vehicleRepository =
                Mockito.mock(VehicleRepository.class);

        costEntryRepository =
                Mockito.mock(VehicleCostEntryRepository.class);

        fingerprintService =
                Mockito.mock(SourceFingerprintService.class);

        vehicleImportService =
                new VehicleImportService(
                        vehicleRepository,
                        costEntryRepository,
                        fingerprintService
                );
    }

    @Test
    void shouldSkipRowWhenFingerprintAlreadyExists() {

        VehicleImportRow row = createRow();

        ImportBatch batch = createBatch();

        when(fingerprintService.forVehicleRow(row))
                .thenReturn("abc123");

        when(costEntryRepository
                .existsBySourceFingerprint("abc123"))
                .thenReturn(true);

        boolean result =
                vehicleImportService.processRow(row, batch);

        assertFalse(result);

        verify(vehicleRepository, never())
                .findByVinIgnoreCase(any());

        verify(costEntryRepository, never())
                .save(any());
    }

    @Test
    void shouldCreateVehicleAndCostEntryForNewRow() {

        VehicleImportRow row = createRow();

        ImportBatch batch = createBatch();

        when(fingerprintService.forVehicleRow(row))
                .thenReturn("abc123");

        when(costEntryRepository
                .existsBySourceFingerprint("abc123"))
                .thenReturn(false);

        when(vehicleRepository
                .findByVinIgnoreCase(row.vin()))
                .thenReturn(Optional.empty());

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        boolean result =
                vehicleImportService.processRow(row, batch);

        assertTrue(result);

        verify(vehicleRepository)
                .save(any(Vehicle.class));

        verify(costEntryRepository)
                .save(any(VehicleCostEntry.class));
    }

    @Test
    void shouldReuseExistingVehicleForAnotherCostEntry() {

        VehicleImportRow row = createRow();

        ImportBatch batch = createBatch();

        Vehicle existingVehicle = new Vehicle(
                row.vin(),
                row.typeName(),
                row.registrationNumber(),
                row.brand(),
                row.accountingDate()
        );

        when(fingerprintService.forVehicleRow(row))
                .thenReturn("new-fingerprint");

        when(costEntryRepository
                .existsBySourceFingerprint("new-fingerprint"))
                .thenReturn(false);

        when(vehicleRepository
                .findByVinIgnoreCase(row.vin()))
                .thenReturn(Optional.of(existingVehicle));

        boolean result =
                vehicleImportService.processRow(row, batch);

        assertTrue(result);

        verify(vehicleRepository, never())
                .save(any(Vehicle.class));

        verify(costEntryRepository)
                .save(any(VehicleCostEntry.class));
    }

    private VehicleImportRow createRow() {

        return new VehicleImportRow(
                LocalDate.of(2026, 7, 15),
                "BIZ-123",
                "BMW AG",
                "EXT-987",
                "WBA12345678901234",
                "BMW 320d",
                "ABC-123",
                "BMW",
                new BigDecimal("12350000.00"),
                47
        );
    }

    private ImportBatch createBatch() {

        return new ImportBatch(
                ImportType.VEHICLE,
                "keszlet.xlsx",
                "file-hash-123"
        );
    }
}