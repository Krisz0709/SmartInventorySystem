package hu.smartinventory.vehicle.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @Column(name = "vin_short", nullable = false, length = 7)
    private String vinShort;

    @Column(name = "type_name", length = 255)
    private String typeName;

    @Column(length = 100)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_condition", length = 20)
    private VehicleCondition vehicleCondition;

    @Column(name = "registration_number", length = 20)
    private String registrationNumber;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "vat_type", nullable = false, length = 30)
    private VatType vatType;

    @Column(
            name = "advertised_price_gross",
            precision = 15,
            scale = 2
    )
    private BigDecimal advertisedPriceGross;

    @Column(name = "price_source", length = 100)
    private String priceSource;

    @Column(name = "price_updated_at")
    private Instant priceUpdatedAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        normalizeVin();
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = Instant.now();

        normalizeVin();
    }

    private void normalizeVin() {
        if (vin == null) {
            return;
        }

        vin = vin.trim().toUpperCase();

        if (vin.length() >= 7) {
            vinShort = vin.substring(vin.length() - 7);
        }
    }
}