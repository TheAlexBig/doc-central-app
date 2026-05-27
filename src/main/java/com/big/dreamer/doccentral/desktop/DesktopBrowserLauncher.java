package com.big.dreamer.doccentral.desktop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.desktop.enabled", havingValue = "true")
public class DesktopBrowserLauncher {

    @EventListener(ApplicationReadyEvent.class)
    public void openApplication() {
        DesktopMode.openBrowser();
    }
}
