package hu.smartinventory.inventoryimport.controller;

import hu.smartinventory.inventoryimport.dto.VehicleImportHistoryResponse;
import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.service.VehicleFileImportService;
import hu.smartinventory.inventoryimport.service.VehicleImportHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class VehicleImportController {

    private final VehicleFileImportService vehicleFileImportService;
    private final VehicleImportHistoryService vehicleImportHistoryService;

    @PostMapping(
            value = "/vehicles",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public VehicleImportResult importVehicles(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "A feltöltött fájl üres."
            );
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.isBlank()) {

            originalFilename = "unknown.xlsx";
        }

        validateFilename(originalFilename);

        return vehicleFileImportService.importFile(
                originalFilename,
                file.getBytes()
        );
    }

    @GetMapping("/vehicles/history")
    public List<VehicleImportHistoryResponse> findVehicleImportHistory() {
        return vehicleImportHistoryService.findVehicleImportHistory();
    }

    private void validateFilename(String filename) {

        String lowerCaseFilename =
                filename.toLowerCase();

        if (!lowerCaseFilename.endsWith(".xlsx")) {
            throw new IllegalArgumentException(
                    "Csak .xlsx fájl tölthető fel."
            );
        }
    }
}
