package com.big.dreamer.doccentral.car.sale.service

import com.big.dreamer.doccentral.car.sale.dto.CarSale
import com.big.dreamer.doccentral.car.sale.dto.PersonDetails
import com.big.dreamer.doccentral.car.sale.template.CarInvolved
import com.big.dreamer.doccentral.car.sale.template.DocumentInvolved
import com.big.dreamer.doccentral.car.sale.template.LegalAgentInvolved
import com.big.dreamer.doccentral.car.sale.template.PeopleInvolved
import org.apache.poi.xwpf.usermodel.BreakType
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset


@Service
class CarSaleService {
    private val f = "femenino"
    private val b = "con "

    private val logger = LoggerFactory.getLogger(javaClass)

    fun createDocument(outputStream: OutputStream, charsets: Charset, carSale: CarSale){
        carSale.buyer.gender  = if(carSale.buyer.gender == f) PeopleInvolved.BUYER_WOMAN else PeopleInvolved.BUYER_DEFAULT
        carSale.seller.gender  = if(carSale.buyer.gender == f) PeopleInvolved.SELLER_WOMAN else PeopleInvolved.SELLER_DEFAULT
        carSale.legalAgent.gender  = if(carSale.legalAgent.gender == f) LegalAgentInvolved.LEGAL_WOMAN else LegalAgentInvolved.LEGAL_DEFAULT
        carSale.document.institution = if(carSale.document.institution.isNotBlank()) b+carSale.document.institution else carSale.document.institution
        carSale.document.identifiesSeller = if( carSale.document.identifiesSeller == "No") "a quien no conozco" else "a quien hoy conozco"
        carSale.document.identifiesBuyer = if( carSale.document.identifiesBuyer == "No") "a quien no conozco" else "a quien hoy conozco"


        try{
            val doc = XWPFDocument()
            createDeclarationSection(doc, carSale)
            createSingleLineSignSection(doc, carSale.buyer, carSale.seller)
            createPageBreak(doc)
            createAuthenticSection(doc, carSale)
            createSingleLineSignSection(doc, carSale.buyer, carSale.seller)

            doc.write(outputStream)
            val osw = OutputStreamWriter(outputStream, charsets)
            osw.close()
            logger.debug("File created and now returning")
        }
        catch (e: Throwable){
          logger.error("Error generating file: {}", e.message)
        }
    }
    
    private fun createDeclarationSection(doc: XWPFDocument, carSale: CarSale) {
        val paragraph = doc.createParagraph()
        paragraph.alignment = ParagraphAlignment.BOTH
        val peopleInvolved : StringBuilder = StringBuilder(PeopleInvolved.document)
        val carInvolved : StringBuilder = StringBuilder(CarInvolved.template)
        val documentInvolved: StringBuilder = StringBuilder(DocumentInvolved.template+DocumentInvolved.firstSectionEnd)

        // First section
        replaceWithClassFields(carSale.seller, peopleInvolved)
        replaceWithClassFields(carSale.buyer, peopleInvolved)
        // Second section
        replaceWithClassFields(carSale.vehicle, carInvolved)
        // Third section
        replaceWithClassFields(carSale.document, documentInvolved)

        addToParagraph(paragraph, listOf(peopleInvolved, carInvolved, documentInvolved))
    }

    private fun createAuthenticSection(doc: XWPFDocument, carSale: CarSale) {
        val paragraph = doc.createParagraph()
        paragraph.alignment = ParagraphAlignment.BOTH
        val legalAgentInvolved : StringBuilder = StringBuilder(LegalAgentInvolved.authentic)
        val peopleInvolved : StringBuilder = StringBuilder(PeopleInvolved.authentic)
        val carInvolved : StringBuilder = StringBuilder(CarInvolved.authentic)
        val documentInvolved: StringBuilder = StringBuilder(DocumentInvolved.authentic+DocumentInvolved.secondSectionEnd)

        // First section
        replaceWithClassFields(carSale.legalAgent, legalAgentInvolved)
        replaceWithClassFields(carSale.document, legalAgentInvolved)
        // Second section
        replaceWithClassFields(carSale.seller, peopleInvolved)
        replaceWithClassFields(carSale.buyer, peopleInvolved)
        replaceWithClassFields(carSale.document, peopleInvolved)
        // Third section
        replaceWithClassFields(carSale.vehicle, carInvolved)
        // Fourth section
        replaceWithClassFields(carSale.document, documentInvolved)

        addToParagraph(paragraph, listOf(legalAgentInvolved, peopleInvolved, carInvolved, documentInvolved))
    }

    private fun addToParagraph(
        paragraph: XWPFParagraph, builders: List<StringBuilder>
    ) {
        val run = paragraph.createRun()
        builders.forEach { stringBuilder ->
            run.setText(stringBuilder.toString())
        }
    }

    private fun createPageBreak(doc: XWPFDocument) {
        val paragraph: XWPFParagraph = doc.createParagraph()
        val run = paragraph.createRun()
        run.addBreak(BreakType.PAGE)
    }
    
    private fun replaceWithClassFields(targetObject: Any, stringBuilder: StringBuilder){
        val seller = targetObject.javaClass
        val sellerFields = seller.declaredFields
        for (field in sellerFields) {
            field.isAccessible = true
            val value = field.get(targetObject)
            replaceKeyAndValue(stringBuilder, ":"+field.name, value.toString())
        }
    }


    // Check the number of users
    private fun createSingleLineSignSection(doc: XWPFDocument, leftSign: PersonDetails, rightSign: PersonDetails) {

        val table = doc.createTable(1, 2)
        table.ctTbl.tblPr.unsetTblBorders()
        table.removeBorders()
        table.setWidth("100%")
        val f1 = table.getRow(0).getCell(0).paragraphs[0]
        createSign(f1, leftSign)

        val f2 = table.getRow(0).getCell(1).paragraphs[0]
        createSign(f2, leftSign)

        val paragraph = doc.createParagraph()
        val end = paragraph.createRun()
        end.addBreak()
    }

    private fun createSign(
        f1: XWPFParagraph,
        personDetails: PersonDetails
    ) {
        f1.alignment = ParagraphAlignment.CENTER
        val run1 = f1.createRun()
        run1.addBreak()
        run1.addBreak()
        run1.addBreak()
        run1.addBreak()
        run1.setText(personDetails.givenName + personDetails.lastName)
        run1.addBreak()
        run1.setText(personDetails.gender)
    }


    private fun replaceKeyAndValue(sourceBuilder: StringBuilder, replaceKey: String, replaceValue: String) {
        val newString = sourceBuilder.toString()
        val start = newString.indexOf(replaceKey)
        val end = start + replaceKey.length
        if(start >= 0){
            sourceBuilder.replace(
                start,
                end,
                replaceValue
            )
        }
    }
}