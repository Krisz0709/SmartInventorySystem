package hu.smartinventory.inventoryimport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "import_batches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_type", nullable = false, length = 30)
    private ImportType importType;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Column(name = "imported_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant importedAt;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "inserted_rows", nullable = false)
    private int insertedRows;

    @Column(name = "skipped_rows", nullable = false)
    private int skippedRows;

    @Column(name = "rejected_rows", nullable = false)
    private int rejectedRows;

    public ImportBatch(
            ImportType importType,
            String originalFilename,
            String fileHash
    ) {
        this.importType = importType;
        this.originalFilename = originalFilename;
        this.fileHash = fileHash;
    }

    @PrePersist
    void beforeInsert() {
        if (importedAt == null) {
            importedAt = Instant.now();
        }
    }

    public void setResult(
            int totalRows,
            int insertedRows,
            int skippedRows,
            int rejectedRows
    ) {
        validateCount(totalRows);
        validateCount(insertedRows);
        validateCount(skippedRows);
        validateCount(rejectedRows);

        this.totalRows = totalRows;
        this.insertedRows = insertedRows;
        this.skippedRows = skippedRows;
        this.rejectedRows = rejectedRows;
    }

    private void validateCount(int value) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    "Import row counts cannot be negative."
            );
        }
    }
}