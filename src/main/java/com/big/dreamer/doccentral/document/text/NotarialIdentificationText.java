package com.big.dreamer.doccentral.document.text;

public final class NotarialIdentificationText {
    private NotarialIdentificationText() {
    }

    public static String knowledge(String known) {
        return "No".equalsIgnoreCase(known) ? "a quien no conozco" : "a quien conozco";
    }
}
