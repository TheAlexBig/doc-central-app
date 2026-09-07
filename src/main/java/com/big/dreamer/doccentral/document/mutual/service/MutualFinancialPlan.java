package com.big.dreamer.doccentral.document.mutual.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MutualFinancialPlan(
        BigDecimal capital,
        BigDecimal interest,
        BigDecimal total,
        int installmentCount,
        String periodicity,
        LocalDate startDate,
        LocalDate dueDate,
        List<Installment> installments) {

    public record Installment(int number, LocalDate dueDate, BigDecimal capital,
                              BigDecimal interest, BigDecimal total) {
    }
}
