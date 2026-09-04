package com.big.dreamer.doccentral.document.mutual.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

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
        @JsonProperty("identifica_acreedor") @NotBlank String identifiesCreditor) {
}
