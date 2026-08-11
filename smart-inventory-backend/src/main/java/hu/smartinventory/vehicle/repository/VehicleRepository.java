package hu.smartinventory.vehicle.repository;

import hu.smartinventory.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByVinIgnoreCase(String vin);

    List<Vehicle> findAllByVinShortIgnoreCaseOrderByIdAsc(String vinShort);

    List<Vehicle> findAllByOrderByIdAsc();
}