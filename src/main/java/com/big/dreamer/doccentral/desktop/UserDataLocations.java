package com.big.dreamer.doccentral.desktop;

import java.nio.file.Path;

public final class UserDataLocations {

    private static final String APPLICATION_DIRECTORY = "Central Docs";

    private UserDataLocations() {
    }

    public static Path applicationDataDirectory() {
        String localApplicationData = System.getenv("LOCALAPPDATA");
        if (localApplicationData != null && !localApplicationData.isBlank()) {
            return Path.of(localApplicationData, APPLICATION_DIRECTORY);
        }
        return Path.of(System.getProperty("user.home"), ".central-docs");
    }

    public static Path documentsDirectory() {
        return Path.of(System.getProperty("user.home"), "Documents", APPLICATION_DIRECTORY, "Documents");
    }
}
