package hu.smartinventory.vehicle.service;

import hu.smartinventory.vehicle.dto.VehicleResponse;
import hu.smartinventory.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAllByOrderByIdAsc()
                .stream()
                .map(VehicleResponse::from)
                .toList();
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
            return vehicleRepository.findByVinIgnoreCase(normalizedVin)
                    .map(VehicleResponse::from)
                    .map(List::of)
                    .orElseGet(List::of);
        }

        if (normalizedVin.length() == 7) {
            return vehicleRepository
                    .findAllByVinShortIgnoreCaseOrderByIdAsc(normalizedVin)
                    .stream()
                    .map(VehicleResponse::from)
                    .toList();
        }

        throw new IllegalArgumentException(
                "VIN search must contain either 7 or 17 characters."
        );
    }
}