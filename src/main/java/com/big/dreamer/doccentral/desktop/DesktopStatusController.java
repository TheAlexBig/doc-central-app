package com.big.dreamer.doccentral.desktop;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/desktop")
public class DesktopStatusController {

    @GetMapping(value = "/status", produces = MediaType.TEXT_PLAIN_VALUE)
    public String status() {
        return "central-docs";
    }
}
