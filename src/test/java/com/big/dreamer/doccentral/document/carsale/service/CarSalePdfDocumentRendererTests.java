package com.big.dreamer.doccentral.document.carsale.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class CarSalePdfDocumentRendererTests {

    @Test
    void justifiesWrappedParagraphLinesButNotTheirFinalLine() throws Exception {
        String paragraph = "Texto legal suficientemente extenso para ocupar varias líneas y comprobar "
                + "que el documento PDF distribuye uniformemente el espacio entre las palabras, "
                + "mantiene los márgenes definidos y deja natural la última línea del párrafo. ".repeat(4);
        var sections = new CarSaleDocumentSections(
                paragraph,
                paragraph,
                "PERSONA COMPRADORA",
                "PERSONA VENDEDORA",
                "LA COMPRADORA",
                "LA VENDEDORA");

        byte[] pdf = new CarSalePdfDocumentRenderer().render(sections);

        try (var document = Loader.loadPDF(pdf)) {
            List<Float> wordSpacingValues = StreamSupport.stream(
                            document.getPages().spliterator(), false)
                    .flatMap(page -> wordSpacingValues(page).stream())
                    .toList();

            assertThat(wordSpacingValues).anyMatch(value -> value > 0);
            assertThat(wordSpacingValues).contains(0.0f);
        }
    }

    private List<Object> parse(org.apache.pdfbox.pdmodel.PDPage page) {
        try {
            return new PDFStreamParser(page).parse();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private List<Float> wordSpacingValues(org.apache.pdfbox.pdmodel.PDPage page) {
        List<Object> tokens = parse(page);
        var values = new java.util.ArrayList<Float>();
        for (int index = 1; index < tokens.size(); index++) {
            if (tokens.get(index) instanceof Operator operator
                    && "Tw".equals(operator.getName())
                    && tokens.get(index - 1) instanceof COSNumber number) {
                values.add(number.floatValue());
            }
        }
        return values;
    }
}
