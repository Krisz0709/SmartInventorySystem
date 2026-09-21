package hu.smartinventory.inventoryimport.controller;

import hu.smartinventory.inventoryimport.dto.VehicleImportResult;
import hu.smartinventory.inventoryimport.service.VehicleFileImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class VehicleImportController {

    private final VehicleFileImportService vehicleFileImportService;

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