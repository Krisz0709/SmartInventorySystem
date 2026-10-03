package hu.smartinventory.inventoryimport.service;

import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.dto.VehicleImportRow;
import hu.smartinventory.inventoryimport.dto.VehicleImportRowError;
import hu.smartinventory.inventoryimport.entity.ImportBatch;
import hu.smartinventory.inventoryimport.entity.ImportRowError;
import hu.smartinventory.inventoryimport.entity.ImportType;
import hu.smartinventory.inventoryimport.repository.ImportBatchRepository;
import hu.smartinventory.inventoryimport.repository.ImportRowErrorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import hu.smartinventory.inventoryimport.dto.VehicleExcelParseResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleFileImportService {

    private final HashService hashService;
    private final VehicleExcelParser vehicleExcelParser;
    private final ImportBatchRepository importBatchRepository;
    private final ImportRowErrorRepository importRowErrorRepository;
    private final VehicleImportService vehicleImportService;

    @Transactional
    public VehicleImportResult importFile(
            String originalFilename,
            byte[] fileContent
    ) throws IOException {

        String fileHash = hashService.sha256(
                new ByteArrayInputStream(fileContent)
        );

        boolean alreadyImported =
                importBatchRepository
                        .existsByImportTypeAndFileHash(
                                ImportType.VEHICLE,
                                fileHash
                        );

        if (alreadyImported) {
            throw new IllegalArgumentException(
                    "Ezt a fájlt már korábban importáltuk."
            );
        }

        VehicleExcelParseResult parseResult =
                vehicleExcelParser.parse(
                        new ByteArrayInputStream(fileContent)
                );

        List<VehicleImportRow> rows =
                parseResult.rows();

        ImportBatch importBatch = new ImportBatch(
                ImportType.VEHICLE,
                originalFilename,
                fileHash
        );

        importBatch =
                importBatchRepository.save(importBatch);

        for (VehicleImportRowError error : parseResult.errors()) {

            ImportRowError importRowError =
                    new ImportRowError(
                            importBatch,
                            error.sourceRowNumber(),
                            error.message()
                    );

            importRowErrorRepository.save(importRowError);
        }

        int insertedRows = 0;
        int skippedRows = 0;

        for (VehicleImportRow row : rows) {

            boolean inserted =
                    vehicleImportService.processRow(
                            row,
                            importBatch
                    );

            if (inserted) {
                insertedRows++;
            } else {
                skippedRows++;
            }
        }

        int rejectedRows =
                parseResult.rejectedRows();

        int totalRows =
                rows.size() + rejectedRows;

        importBatch.setResult(
                totalRows,
                insertedRows,
                skippedRows,
                rejectedRows
        );

        importBatchRepository.save(importBatch);

        return new VehicleImportResult(
                originalFilename,
                totalRows,
                insertedRows,
                skippedRows,
                rejectedRows,
                parseResult.errors()
        );
    }
}
