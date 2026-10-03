package hu.smartinventory.inventoryimport.repository;

import hu.smartinventory.inventoryimport.entity.ImportBatch;
import hu.smartinventory.inventoryimport.entity.ImportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportBatchRepository
        extends JpaRepository<ImportBatch, Long> {

    boolean existsByImportTypeAndFileHash(
            ImportType importType,
            String fileHash
    );

    List<ImportBatch> findAllByImportTypeOrderByImportedAtDescIdDesc(
            ImportType importType
    );
}
