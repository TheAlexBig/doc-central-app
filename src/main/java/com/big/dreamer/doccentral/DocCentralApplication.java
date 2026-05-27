package com.big.dreamer.doccentral;

import com.big.dreamer.doccentral.desktop.DesktopMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocCentralApplication {

    public static void main(String[] args) {
        if (DesktopMode.isRequested(args)) {
            args = DesktopMode.desktopArguments(args);
            if (DesktopMode.reuseExistingInstance()) {
                return;
            }
            if (!DesktopMode.claimNewInstance()) {
                DesktopMode.awaitExistingInstance();
                return;
            }
        }
        SpringApplication.run(DocCentralApplication.class, args);
    }
}
