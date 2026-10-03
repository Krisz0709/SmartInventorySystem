package hu.smartinventory.vehicle.service;

import hu.smartinventory.cost.repository.VehicleCostEntryRepository;
import hu.smartinventory.cost.repository.VehicleCostTotalProjection;
import hu.smartinventory.vehicle.dto.VehicleResponse;
import hu.smartinventory.vehicle.entity.Vehicle;
import hu.smartinventory.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCostEntryRepository costEntryRepository;

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return toResponses(
                vehicleRepository.findAllByOrderByIdAsc()
        );
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> searchByVin(String vin) {

        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException(
                    "VIN must not be empty."
            );
        }

        String normalizedVin = vin.trim().toUpperCase();

        if (normalizedVin.length() == 17) {
            return vehicleRepository
                    .findByVinIgnoreCase(normalizedVin)
                    .map(List::of)
                    .map(this::toResponses)
                    .orElseGet(List::of);
        }

        if (normalizedVin.length() == 7) {
            return toResponses(
                    vehicleRepository
                            .findAllByVinShortIgnoreCaseOrderByIdAsc(
                                    normalizedVin
                            )
            );
        }

        throw new IllegalArgumentException(
                "VIN search must contain either 7 or 17 characters."
        );
    }

    private List<VehicleResponse> toResponses(List<Vehicle> vehicles) {

        Map<Long, BigDecimal> totalCostAmounts =
                findTotalCostAmounts(vehicles);

        return vehicles.stream()
                .map(vehicle -> VehicleResponse.from(
                        vehicle,
                        totalCostAmounts.getOrDefault(
                                vehicle.getId(),
                                BigDecimal.ZERO
                        )
                ))
                .toList();
    }

    private Map<Long, BigDecimal> findTotalCostAmounts(
            List<Vehicle> vehicles
    ) {

        if (vehicles.isEmpty()) {
            return Map.of();
        }

        List<Long> vehicleIds =
                vehicles.stream()
                        .map(Vehicle::getId)
                        .toList();

        return costEntryRepository
                .findTotalCostAmountsByVehicleIds(vehicleIds)
                .stream()
                .collect(Collectors.toMap(
                        VehicleCostTotalProjection::getVehicleId,
                        VehicleCostTotalProjection::getTotalCostAmount
                ));
    }
}
