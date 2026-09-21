package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.cost.entity.VehicleCostEntry;
import hu.smartinventory.cost.entity.VehicleCostType;
import hu.smartinventory.cost.repository.VehicleCostEntryRepository;
import hu.smartinventory.inventoryimport.dto.VehicleImportRow;
import hu.smartinventory.inventoryimport.entity.ImportBatch;
import hu.smartinventory.vehicle.entity.Vehicle;
import hu.smartinventory.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleImportService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCostEntryRepository costEntryRepository;
    private final SourceFingerprintService fingerprintService;

    public boolean processRow(
            VehicleImportRow row,
            ImportBatch importBatch
    ) {

        String fingerprint =
                fingerprintService.forVehicleRow(row);

        boolean alreadyExists =
                costEntryRepository
                        .existsBySourceFingerprint(fingerprint);

        if (alreadyExists) {
            return false;
        }

        Vehicle vehicle = findOrCreateVehicle(row);

        VehicleCostEntry costEntry =
                new VehicleCostEntry(
                        vehicle,
                        importBatch,
                        VehicleCostType.ACQUISITION,
                        row.accountingDate(),
                        row.documentNumber(),
                        row.supplierName(),
                        row.externalDocumentNumber(),
                        row.amount(),
                        row.sourceRowNumber(),
                        fingerprint
                );

        costEntryRepository.save(costEntry);

        return true;
    }

    private Vehicle findOrCreateVehicle(
            VehicleImportRow row
    ) {

        return vehicleRepository
                .findByVinIgnoreCase(row.vin())
                .orElseGet(() -> {

                    Vehicle vehicle = new Vehicle(
                            row.vin(),
                            row.typeName(),
                            row.registrationNumber(),
                            row.brand(),
                            row.accountingDate()
                    );

                    return vehicleRepository.save(vehicle);
                });
    }
}