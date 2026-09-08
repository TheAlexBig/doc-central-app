package com.big.dreamer.doccentral.document.text;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LegalDocumentTextTests {

    @Test
    void writesSharedMoneyDatesAndCountersAsLegalText() {
        assertThat(LegalDocumentText.money(new BigDecimal("1000.00")))
                .isEqualTo("MIL DÓLARES DE LOS ESTADOS UNIDOS DE AMÉRICA");
        assertThat(LegalDocumentText.money(new BigDecimal("146.56")))
                .isEqualTo("CIENTO CUARENTA Y SEIS DÓLARES CON CINCUENTA Y SEIS CENTAVOS "
                        + "DE DÓLAR DE LOS ESTADOS UNIDOS DE AMÉRICA");
        assertThat(LegalDocumentText.currencyFromWords("CINCO MIL CON VEINTIDÓS CENTAVOS"))
                .isEqualTo("CINCO MIL DÓLARES CON VEINTIDÓS CENTAVOS DE DÓLAR "
                        + "DE LOS ESTADOS UNIDOS DE AMÉRICA");
        assertThat(LegalDocumentText.date(LocalDate.of(2027, 9, 1)))
                .isEqualTo("UNO DE SEPTIEMBRE DE DOS MIL VEINTISIETE");
        assertThat(LegalDocumentText.replaceDigits("Cada 12 meses"))
                .isEqualTo("Cada DOCE meses");
    }
}
