package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class CarSalePdfDocumentRenderer {

    private static final float FONT_SIZE = 11.0f;
    private static final float LINE_HEIGHT = 15.0f;
    private static final float MARGIN = 54.0f;

    byte[] render(CarSaleDocumentSections sections, CarSaleDocumentRequest request) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(document);
            writer.writeParagraph(sections.declaration());
            writer.writeSignatures(request.buyer(), request.seller(), sections.buyerTitle(), sections.sellerTitle());
            writer.newPage();
            writer.writeParagraph(sections.authentic());
            writer.writeSignatures(request.buyer(), request.seller(), sections.buyerTitle(), sections.sellerTitle());
            writer.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to generate the PDF document.", exception);
        }
    }

    private static final class PdfWriter {

        private final PDDocument document;
        private final PDType1Font font;
        private final PDType1Font boldFont;
        private PDPageContentStream content;
        private float y;

        private PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
            boldFont = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
            newPage();
        }

        private void newPage() throws IOException {
            if (content != null) {
                content.close();
            }
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        private void writeParagraph(String text) throws IOException {
            for (String line : wrap(text, PDRectangle.LETTER.getWidth() - (MARGIN * 2))) {
                ensureSpace(LINE_HEIGHT);
                content.beginText();
                content.setFont(font, FONT_SIZE);
                content.newLineAtOffset(MARGIN, y);
                content.showText(line);
                content.endText();
                y -= LINE_HEIGHT;
            }
            y -= LINE_HEIGHT;
        }

        private void writeSignatures(
                PersonDetails buyer,
                PersonDetails seller,
                String buyerTitle,
                String sellerTitle) throws IOException {
            ensureSpace(LINE_HEIGHT * 7);
            y -= LINE_HEIGHT * 3;
            float columnWidth = (PDRectangle.LETTER.getWidth() - (MARGIN * 2)) / 2;
            writeCentered(buyer.givenName() + " " + buyer.lastName(), MARGIN, columnWidth, boldFont);
            writeCentered(seller.givenName() + " " + seller.lastName(), MARGIN + columnWidth, columnWidth, boldFont);
            y -= LINE_HEIGHT;
            writeCentered(buyerTitle, MARGIN, columnWidth, font);
            writeCentered(sellerTitle, MARGIN + columnWidth, columnWidth, font);
            y -= LINE_HEIGHT * 2;
        }

        private void writeCentered(String text, float x, float width, PDType1Font font) throws IOException {
            float textWidth = font.getStringWidth(text) / 1000 * FONT_SIZE;
            content.beginText();
            content.setFont(font, FONT_SIZE);
            content.newLineAtOffset(x + ((width - textWidth) / 2), y);
            content.showText(text);
            content.endText();
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < MARGIN) {
                newPage();
            }
        }

        private void close() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }
        }

        private List<String> wrap(String text, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : text.replace('\n', ' ').split("\\s+")) {
                if (word.isBlank()) {
                    continue;
                }
                String next = line.length() == 0 ? word : line + " " + word;
                float nextWidth = font.getStringWidth(next) / 1000 * FONT_SIZE;
                if (nextWidth <= maxWidth) {
                    line.setLength(0);
                    line.append(next);
                } else {
                    if (line.length() > 0) {
                        lines.add(line.toString());
                    }
                    line.setLength(0);
                    line.append(word);
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
            return lines;
        }
    }
}
