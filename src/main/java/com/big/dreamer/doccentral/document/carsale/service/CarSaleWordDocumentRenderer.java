package com.big.dreamer.doccentral.document.carsale.service;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

final class CarSaleWordDocumentRenderer {

    byte[] render(CarSaleDocumentSections sections) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            createTextSection(document, sections.declaration());
            createSignatures(document, sections);
            createPageBreak(document);
            createTextSection(document, sections.authentic());
            createSignatures(document, sections);
            document.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to generate the Word document.", exception);
        }
    }

    private void createTextSection(XWPFDocument document, String text) {
        XWPFParagraph paragraph = justifiedParagraph(document);
        paragraph.createRun().setText(text);
    }

    private XWPFParagraph justifiedParagraph(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.BOTH);
        return paragraph;
    }

    private void createSignatures(
            XWPFDocument document,
            CarSaleDocumentSections sections) {
        XWPFTable table = document.createTable(1, 2);
        table.getCTTbl().getTblPr().unsetTblBorders();
        table.removeBorders();
        table.setWidth("100%");
        createSignature(table.getRow(0).getCell(0).getParagraphs().getFirst(),
                sections.buyerName(), sections.buyerTitle());
        createSignature(table.getRow(0).getCell(1).getParagraphs().getFirst(),
                sections.sellerName(), sections.sellerTitle());
        document.createParagraph().createRun().addBreak();
    }

    private void createSignature(XWPFParagraph paragraph, String name, String title) {
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        var run = paragraph.createRun();
        run.addBreak();
        run.addBreak();
        run.addBreak();
        run.addBreak();
        run.setText(name);
        run.addBreak();
        run.setText(title);
    }

    private void createPageBreak(XWPFDocument document) {
        document.createParagraph().createRun().addBreak(BreakType.PAGE);
    }
}
