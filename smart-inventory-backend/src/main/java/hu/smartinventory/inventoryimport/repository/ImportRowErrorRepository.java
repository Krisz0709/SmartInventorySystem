package hu.smartinventory.inventoryimport.repository;

import hu.smartinventory.inventoryimport.entity.ImportRowError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportRowErrorRepository
        extends JpaRepository<ImportRowError, Long> {

    List<ImportRowError> findAllByImportBatch_OriginalFilenameOrderBySourceRowNumberAsc(
            String originalFilename
    );

    List<ImportRowError> findAllByImportBatch_IdOrderBySourceRowNumberAsc(
            Long importBatchId
    );
}
