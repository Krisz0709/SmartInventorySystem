package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.inventoryimport.dto.VehicleImportHistoryResponse;
import hu.smartinventory.inventoryimport.dto.VehicleImportRowError;
import hu.smartinventory.inventoryimport.entity.ImportBatch;
import hu.smartinventory.inventoryimport.entity.ImportType;
import hu.smartinventory.inventoryimport.repository.ImportBatchRepository;
import hu.smartinventory.inventoryimport.repository.ImportRowErrorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleImportHistoryService {

    private final ImportBatchRepository importBatchRepository;
    private final ImportRowErrorRepository importRowErrorRepository;

    @Transactional(readOnly = true)
    public List<VehicleImportHistoryResponse> findVehicleImportHistory() {
        return importBatchRepository
                .findAllByImportTypeOrderByImportedAtDescIdDesc(
                        ImportType.VEHICLE
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VehicleImportHistoryResponse toResponse(
            ImportBatch importBatch
    ) {
        List<VehicleImportRowError> errors =
                importRowErrorRepository
                        .findAllByImportBatch_IdOrderBySourceRowNumberAsc(
                                importBatch.getId()
                        )
                        .stream()
                        .map(error ->
                                new VehicleImportRowError(
                                        error.getSourceRowNumber(),
                                        error.getErrorMessage()
                                )
                        )
                        .toList();

        return VehicleImportHistoryResponse.from(
                importBatch,
                errors
        );
    }
}
