package hu.smartinventory.cost.repository;

import hu.smartinventory.cost.entity.VehicleCostEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleCostEntryRepository
        extends JpaRepository<VehicleCostEntry, Long> {

    boolean existsBySourceFingerprint(String sourceFingerprint);

    long countByVehicle_Id(Long vehicleId);
}