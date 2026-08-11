package hu.smartinventory.vehicle.controller;

import hu.smartinventory.vehicle.dto.VehicleResponse;
import hu.smartinventory.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public List<VehicleResponse> findAll() {
        return vehicleService.findAll();
    }

    @GetMapping("/search")
    public List<VehicleResponse> searchByVin(
            @RequestParam String vin
    ) {
        return vehicleService.searchByVin(vin);
    }
}