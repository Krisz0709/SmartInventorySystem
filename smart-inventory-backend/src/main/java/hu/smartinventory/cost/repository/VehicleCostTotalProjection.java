package hu.smartinventory.cost.repository;

import java.math.BigDecimal;

public interface VehicleCostTotalProjection {

    Long getVehicleId();

    BigDecimal getTotalCostAmount();
}
