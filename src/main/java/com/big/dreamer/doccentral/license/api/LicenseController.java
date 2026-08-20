package com.big.dreamer.doccentral.license.api;

import com.big.dreamer.doccentral.license.model.LicenseDocument;
import com.big.dreamer.doccentral.license.model.LicenseStatus;
import com.big.dreamer.doccentral.license.service.LicenseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping("/status")
    public LicenseStatus status() {
        return licenseService.status();
    }

    @PostMapping("/activate")
    public LicenseStatus activate(@RequestBody LicenseDocument license) {
        return licenseService.activate(license);
    }
}
