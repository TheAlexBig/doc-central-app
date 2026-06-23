package com.big.dreamer.doccentral.vehicle.api;

import com.big.dreamer.doccentral.vehicle.model.SavedVehicle;
import com.big.dreamer.doccentral.vehicle.model.VehicleOptionRemoval;
import com.big.dreamer.doccentral.vehicle.model.VehicleOptions;
import com.big.dreamer.doccentral.vehicle.service.SavedVehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
public class SavedVehicleController {

    private final SavedVehicleRepository repository;

    public SavedVehicleController(SavedVehicleRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/options")
    public VehicleOptions listOptions() {
        return repository.findOptions();
    }

    @PostMapping
    public ResponseEntity<SavedVehicle> saveVehicle(@Valid @RequestBody SavedVehicle vehicle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(vehicle));
    }

    @DeleteMapping("/options")
    public VehicleOptions removeOption(@Valid @RequestBody VehicleOptionRemoval removal) {
        return repository.forgetOption(removal.kind(), removal.value());
    }
}
