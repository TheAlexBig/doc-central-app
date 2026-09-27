package com.big.dreamer.doccentral.document.render;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LegalDocumentRendererTests {
    @Test
    void rendersTheSameSectionsAndSignaturesToWordAndPdf() throws Exception {
        LegalDocument model = new LegalDocument(List.of(
                new LegalDocument.Section("PRIMERA SECCIÓN con contenido común.", List.of(
                        new LegalDocument.Signature("PERSONA UNO", "OTORGANTE"),
                        new LegalDocument.Signature("PERSONA DOS", "OTORGANTE"))),
                new LegalDocument.Section("SEGUNDA SECCIÓN.", List.of())));
        LegalDocumentRenderer renderer = new LegalDocumentRenderer();

        try (XWPFDocument word = new XWPFDocument(new ByteArrayInputStream(renderer.word(model)));
             var pdf = Loader.loadPDF(renderer.pdf(model))) {
            String wordText = word.getParagraphs().stream().map(paragraph -> paragraph.getText())
                    .reduce("", (left, right) -> left + " " + right);
            String wordTables = word.getTables().stream().flatMap(table -> table.getRows().stream())
                    .flatMap(row -> row.getTableCells().stream()).map(cell -> cell.getText())
                    .reduce("", (left, right) -> left + " " + right);
            String pdfText = new PDFTextStripper().getText(pdf);
            assertThat(wordText).contains("PRIMERA SECCIÓN", "SEGUNDA SECCIÓN");
            assertThat(wordTables).contains("PERSONA UNO", "PERSONA DOS");
            assertThat(pdfText).contains("PRIMERA SECCIÓN", "SEGUNDA SECCIÓN", "PERSONA UNO", "PERSONA DOS");
        }
    }
}
