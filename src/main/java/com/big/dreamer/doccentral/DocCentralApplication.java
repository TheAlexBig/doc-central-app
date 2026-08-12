package com.big.dreamer.doccentral;

import com.big.dreamer.doccentral.desktop.DesktopMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocCentralApplication {

    public static void main(String[] args) {
        boolean desktopRequested = DesktopMode.isRequested(args);
        if (desktopRequested) {
            DesktopMode.configureLogging();
            args = DesktopMode.desktopArguments(args);
            if (DesktopMode.reuseExistingInstance()) {
                return;
            }
            if (!DesktopMode.claimNewInstance()) {
                if (DesktopMode.awaitExistingInstance()) {
                    return;
                }
                if (!DesktopMode.canOpenApplicationPort()) {
                    return;
                }
            }
        }
        try {
            SpringApplication.run(DocCentralApplication.class, args);
        } catch (RuntimeException | Error exception) {
            if (desktopRequested) {
                DesktopMode.recordStartupFailure(exception);
            }
            throw exception;
        }
    }
}
