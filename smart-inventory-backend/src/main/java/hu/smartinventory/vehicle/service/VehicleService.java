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
}