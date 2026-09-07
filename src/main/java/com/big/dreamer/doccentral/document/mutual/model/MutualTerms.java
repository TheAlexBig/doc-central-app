package com.big.dreamer.doccentral.document.mutual.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MutualTerms(
        @JsonProperty("monto") @NotBlank String amount,
        @JsonProperty("plazo") @NotBlank String term,
        @JsonProperty("fecha_vencimiento") @NotBlank String dueDate,
        @JsonProperty("numero_cuotas") @NotBlank String installmentCount,
        @JsonProperty("monto_cuota") @NotBlank String installmentAmount,
        @JsonProperty("banco_pago") @NotBlank String paymentBank,
        @JsonProperty("cuenta_pago") @NotBlank String paymentAccount,
        @JsonProperty("interes_mensual") String monthlyInterest,
        @JsonProperty("interes_mora") String defaultInterest,
        @JsonProperty("destino_fondos") @NotBlank String fundsPurpose,
        @JsonProperty("garantia_letra_cambio") boolean billOfExchangeGuarantee,
        @JsonProperty("fecha_vencimiento_garantia") String guaranteeDueDate,
        @JsonProperty("gastos_administrativos") String administrativeExpenses,
        @JsonProperty("domicilio_especial") @NotBlank String specialDomicile,
        @JsonProperty("lugar_firma") @NotBlank String signingPlace,
        @JsonProperty("departamento_firma") @NotBlank String signingState,
        @JsonProperty("fecha_firma") @NotBlank String signingDate,
        @JsonProperty("hora_firma") @NotBlank String signingTime,
        @JsonProperty("identifica_deudor") @NotBlank String identifiesDebtor,
        @JsonProperty("identifica_acreedor") @NotBlank String identifiesCreditor,
        @JsonProperty("monto_numerico") BigDecimal numericAmount,
        @JsonProperty("modalidad_plazo") MutualTermMode termMode,
        @JsonProperty("cantidad_plazo") Integer termQuantity,
        @JsonProperty("unidad_plazo") MutualTermUnit termUnit,
        @JsonProperty("fecha_inicio") LocalDate startDate,
        @JsonProperty("fecha_vencimiento_iso") LocalDate dueDateIso,
        @JsonProperty("numero_cuotas_numerico") Integer numericInstallmentCount,
        @JsonProperty("tasa_interes_mensual") BigDecimal numericMonthlyInterest,
        @JsonProperty("tipo_instrumento") MutualInstrumentType instrumentType,
        @JsonProperty("tipo_garantia") MutualGuaranteeType guaranteeType,
        @JsonProperty("numero_escritura") Integer deedNumber,
        @JsonProperty("numero_escritura_texto") String deedNumberText,
        @JsonProperty("detalles_garantia") String guaranteeDetails) {

    public MutualTerms(String amount, String term, String dueDate, String installmentCount,
                       String installmentAmount, String paymentBank, String paymentAccount,
                       String monthlyInterest, String defaultInterest, String fundsPurpose,
                       boolean billOfExchangeGuarantee, String guaranteeDueDate,
                       String administrativeExpenses, String specialDomicile, String signingPlace,
                       String signingState, String signingDate, String signingTime,
                       String identifiesDebtor, String identifiesCreditor) {
        this(amount, term, dueDate, installmentCount, installmentAmount, paymentBank, paymentAccount,
                monthlyInterest, defaultInterest, fundsPurpose, billOfExchangeGuarantee,
                guaranteeDueDate, administrativeExpenses, specialDomicile, signingPlace,
                signingState, signingDate, signingTime, identifiesDebtor, identifiesCreditor,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public boolean hasStructuredCalculation() {
        return numericAmount != null || termMode != null || numericInstallmentCount != null;
    }

    public MutualInstrumentType effectiveInstrumentType() {
        return instrumentType == null ? MutualInstrumentType.PRIVATE_AUTHENTICATED : instrumentType;
    }

    public MutualGuaranteeType effectiveGuaranteeType() {
        if (guaranteeType != null) return guaranteeType;
        return billOfExchangeGuarantee ? MutualGuaranteeType.MOVABLE : MutualGuaranteeType.NONE;
    }
}
