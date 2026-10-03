package hu.smartinventory.inventoryimport.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "import_row_errors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportRowError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "import_batch_id",
            nullable = false
    )
    private ImportBatch importBatch;

    @Column(
            name = "source_row_number",
            nullable = false
    )
    private int sourceRowNumber;

    @Column(
            name = "error_message",
            nullable = false,
            length = 1000
    )
    private String errorMessage;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    public ImportRowError(
            ImportBatch importBatch,
            int sourceRowNumber,
            String errorMessage
    ) {
        this.importBatch = importBatch;
        this.sourceRowNumber = sourceRowNumber;
        this.errorMessage = errorMessage;
    }

    @PrePersist
    void beforeInsert() {

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}