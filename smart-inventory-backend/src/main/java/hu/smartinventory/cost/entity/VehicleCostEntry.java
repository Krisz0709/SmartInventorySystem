package hu.smartinventory.cost.entity;

import hu.smartinventory.inventoryimport.entity.ImportBatch;
import hu.smartinventory.vehicle.entity.Vehicle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_cost_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VehicleCostEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_batch_id")
    private ImportBatch importBatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", nullable = false, length = 30)
    private VehicleCostType costType;

    @Column(name = "accounting_date", nullable = false)
    private LocalDate accountingDate;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "external_document_number", length = 100)
    private String externalDocumentNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "source_row_number")
    private Integer sourceRowNumber;

    @Column(name = "source_fingerprint", nullable = false, length = 64)
    private String sourceFingerprint;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    public VehicleCostEntry(
            Vehicle vehicle,
            ImportBatch importBatch,
            VehicleCostType costType,
            LocalDate accountingDate,
            String documentNumber,
            String supplierName,
            String externalDocumentNumber,
            BigDecimal amount,
            Integer sourceRowNumber,
            String sourceFingerprint
    ) {
        this.vehicle = vehicle;
        this.importBatch = importBatch;
        this.costType = costType;
        this.accountingDate = accountingDate;
        this.documentNumber = documentNumber;
        this.supplierName = supplierName;
        this.externalDocumentNumber = externalDocumentNumber;
        this.amount = amount;
        this.sourceRowNumber = sourceRowNumber;
        this.sourceFingerprint = sourceFingerprint;
    }

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}