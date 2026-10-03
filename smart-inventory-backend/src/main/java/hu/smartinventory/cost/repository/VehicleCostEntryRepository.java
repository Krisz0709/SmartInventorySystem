package hu.smartinventory.cost.repository;

import hu.smartinventory.cost.entity.VehicleCostEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface VehicleCostEntryRepository
        extends JpaRepository<VehicleCostEntry, Long> {

    boolean existsBySourceFingerprint(String sourceFingerprint);

    long countByVehicle_Id(Long vehicleId);

    @Query("""
            select
                costEntry.vehicle.id as vehicleId,
                coalesce(sum(costEntry.amount), 0) as totalCostAmount
            from VehicleCostEntry costEntry
            where costEntry.vehicle.id in :vehicleIds
            group by costEntry.vehicle.id
            """)
    List<VehicleCostTotalProjection> findTotalCostAmountsByVehicleIds(
            @Param("vehicleIds") Collection<Long> vehicleIds
    );
}
