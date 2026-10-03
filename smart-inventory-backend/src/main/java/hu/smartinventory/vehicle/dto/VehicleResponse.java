package hu.smartinventory.vehicle.dto;

import hu.smartinventory.vehicle.entity.VatType;
import hu.smartinventory.vehicle.entity.Vehicle;
import hu.smartinventory.vehicle.entity.VehicleStatus;
import hu.smartinventory.vehicle.entity.VehicleCondition;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record VehicleResponse(
        Long id,
        String vin,
        String vinShort,
        String typeName,
        String brand,
        VehicleCondition vehicleCondition,
        String registrationNumber,
        LocalDate acquisitionDate,
        VehicleStatus status,
        VatType vatType,
        BigDecimal totalCostAmount,
        BigDecimal advertisedPriceGross,
        String priceSource,
        Instant priceUpdatedAt,
        Instant lastSeenAt
) {

    public static VehicleResponse from(Vehicle vehicle) {
        return from(vehicle, BigDecimal.ZERO);
    }

    public static VehicleResponse from(
            Vehicle vehicle,
            BigDecimal totalCostAmount
    ) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVin(),
                vehicle.getVinShort(),
                vehicle.getTypeName(),
                vehicle.getBrand(),
                vehicle.getVehicleCondition(),
                vehicle.getRegistrationNumber(),
                vehicle.getAcquisitionDate(),
                vehicle.getStatus(),
                vehicle.getVatType(),
                totalCostAmount,
                vehicle.getAdvertisedPriceGross(),
                vehicle.getPriceSource(),
                vehicle.getPriceUpdatedAt(),
                vehicle.getLastSeenAt()
        );
    }
}
