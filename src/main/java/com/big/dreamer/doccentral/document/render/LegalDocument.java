package com.big.dreamer.doccentral.document.render;

import java.util.List;

/** Format-neutral legal document assembled before Word or PDF rendering. */
public record LegalDocument(List<Section> sections) {
    public LegalDocument {
        sections = List.copyOf(sections);
    }

    public record Section(String text, List<Signature> signatures) {
        public Section {
            signatures = signatures == null ? List.of() : List.copyOf(signatures);
        }
    }

    public record Signature(String name, String title) {
    }
}
