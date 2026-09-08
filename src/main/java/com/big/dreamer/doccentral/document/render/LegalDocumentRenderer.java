package com.big.dreamer.doccentral.document.render;

import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class LegalDocumentRenderer {
    private static final float FONT_SIZE = 11;
    private static final float LINE_HEIGHT = 15;
    private static final float MARGIN = 54;

    public byte[] word(List<Section> sections) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (int index = 0; index < sections.size(); index++) {
                if (index > 0) document.createParagraph().createRun().addBreak(BreakType.PAGE);
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setAlignment(ParagraphAlignment.BOTH);
                paragraph.createRun().setText(sections.get(index).text());
                addWordSignatures(document, sections.get(index).signatures());
            }
            document.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("No se pudo generar el documento Word.", exception);
        }
    }

    public byte[] pdf(List<Section> sections) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(document);
            for (int index = 0; index < sections.size(); index++) {
                if (index > 0) writer.newPage();
                writer.paragraph(sections.get(index).text());
                writer.signatures(sections.get(index).signatures());
            }
            writer.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("No se pudo generar el documento PDF.", exception);
        }
    }

    private void addWordSignatures(XWPFDocument document, List<Signature> signatures) {
        for (int offset = 0; offset < signatures.size(); offset += 2) {
            int columns = Math.min(2, signatures.size() - offset);
            XWPFTable table = document.createTable(1, columns);
            table.removeBorders();
            table.setWidth("100%");
            for (int column = 0; column < columns; column++) {
                Signature signature = signatures.get(offset + column);
                XWPFParagraph paragraph = table.getRow(0).getCell(column).getParagraphs().getFirst();
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                var run = paragraph.createRun();
                run.addBreak(); run.addBreak(); run.addBreak();
                run.setText(signature.name());
                run.addBreak();
                run.setText(signature.title());
            }
        }
    }

    public record Section(String text, List<Signature> signatures) {
        public Section {
            signatures = signatures == null ? List.of() : List.copyOf(signatures);
        }
    }

    public record Signature(String name, String title) {
    }

    private static final class PdfWriter {
        private final PDDocument document;
        private final PDType1Font font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
        private PDPageContentStream content;
        private float y;

        private PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        private void newPage() throws IOException {
            if (content != null) content.close();
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        private void paragraph(String text) throws IOException {
            float width = PDRectangle.LETTER.getWidth() - MARGIN * 2;
            List<String> lines = wrap(text, width);
            for (int index = 0; index < lines.size(); index++) {
                ensure(LINE_HEIGHT);
                String line = lines.get(index);
                int spaces = Math.max(0, line.split(" ").length - 1);
                float lineWidth = font.getStringWidth(line) / 1000 * FONT_SIZE;
                float spacing = index < lines.size() - 1 && spaces > 0 ? (width - lineWidth) / spaces : 0;
                content.beginText();
                content.setFont(font, FONT_SIZE);
                content.setWordSpacing(spacing);
                content.newLineAtOffset(MARGIN, y);
                content.showText(line);
                content.endText();
                y -= LINE_HEIGHT;
            }
        }

        private void signatures(List<Signature> signatures) throws IOException {
            float width = (PDRectangle.LETTER.getWidth() - MARGIN * 2) / 2;
            for (int offset = 0; offset < signatures.size(); offset += 2) {
                ensure(LINE_HEIGHT * 6);
                y -= LINE_HEIGHT * 3;
                for (int column = 0; column < Math.min(2, signatures.size() - offset); column++) {
                    Signature signature = signatures.get(offset + column);
                    centered(signature.name(), MARGIN + width * column, width, bold);
                    float nameY = y;
                    y -= LINE_HEIGHT;
                    centered(signature.title(), MARGIN + width * column, width, font);
                    y = nameY;
                }
                y -= LINE_HEIGHT * 3;
            }
        }

        private void centered(String text, float x, float width, PDType1Font selectedFont) throws IOException {
            float textWidth = selectedFont.getStringWidth(text) / 1000 * FONT_SIZE;
            content.beginText();
            content.setFont(selectedFont, FONT_SIZE);
            content.newLineAtOffset(x + Math.max(0, (width - textWidth) / 2), y);
            content.showText(text);
            content.endText();
        }

        private List<String> wrap(String text, float width) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : text.replace('\n', ' ').split("\\s+")) {
                if (word.isBlank()) continue;
                String next = line.isEmpty() ? word : line + " " + word;
                if (font.getStringWidth(next) / 1000 * FONT_SIZE <= width) {
                    line.setLength(0); line.append(next);
                } else {
                    if (!line.isEmpty()) lines.add(line.toString());
                    line.setLength(0); line.append(word);
                }
            }
            if (!line.isEmpty()) lines.add(line.toString());
            return lines;
        }

        private void ensure(float required) throws IOException {
            if (y - required < MARGIN) newPage();
        }

        private void close() throws IOException {
            if (content != null) content.close();
        }
    }
}
