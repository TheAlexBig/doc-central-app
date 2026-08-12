package com.big.dreamer.doccentral.document;

public enum DocumentFormat {
    WORD("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PDF("pdf", "application/pdf");

    private final String extension;
    private final String contentType;

    DocumentFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public static DocumentFormat from(String value) {
        return PDF.extension.equalsIgnoreCase(value) ? PDF : WORD;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }

    public boolean isPdf() {
        return this == PDF;
    }
}
