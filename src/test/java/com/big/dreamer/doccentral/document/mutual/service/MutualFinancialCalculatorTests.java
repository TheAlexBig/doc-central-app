package com.big.dreamer.doccentral.document.mutual.service;

import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidationException;
import com.big.dreamer.doccentral.document.mutual.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class MutualFinancialCalculatorTests {
    private final MutualFinancialCalculator calculator = new MutualFinancialCalculator();

    @Test
    void calculatesSixtyDaysInFourInstallmentsWithInterest() {
        MutualFinancialPlan plan = calculator.calculate(terms(LocalDate.of(2026, 1, 1),
                MutualTermMode.DURATION, 60, MutualTermUnit.DAYS, null, 4, "1000", "2"));
        assertThat(plan.dueDate()).isEqualTo("2026-03-02");
        assertThat(plan.installments()).extracting(MutualFinancialPlan.Installment::dueDate)
                .containsExactly(LocalDate.parse("2026-01-16"), LocalDate.parse("2026-01-31"),
                        LocalDate.parse("2026-02-15"), LocalDate.parse("2026-03-02"));
        assertThat(plan.interest()).isEqualByComparingTo("25.13");
        assertThat(plan.total()).isEqualByComparingTo("1025.13");
        assertThat(plan.installments().getFirst().capital()).isEqualByComparingTo("246.28");
        assertThat(plan.installments().getLast().capital()).isEqualByComparingTo("253.75");
        assertThat(plan.periodicity()).isEqualTo("Cada 15 días");
    }

    @Test
    void amortizesInterestOverTheOutstandingBalance() {
        MutualFinancialPlan plan = calculator.calculate(terms(LocalDate.of(2026, 9, 4),
                MutualTermMode.DURATION, 12, MutualTermUnit.MONTHS, null, 6, "1234", "3"));

        assertThat(plan.interest()).isEqualByComparingTo("271.71");
        assertThat(plan.total()).isEqualByComparingTo("1505.71");
        assertThat(plan.installments().getFirst().interest()).isEqualByComparingTo("74.04");
        assertThat(plan.installments().getLast().interest()).isEqualByComparingTo("14.21");
        assertThat(plan.installments().getFirst().capital())
                .isLessThan(plan.installments().getLast().capital());
    }

    @Test
    void keepsMonthlyCadenceAcrossDifferentMonthLengths() {
        MutualFinancialPlan plan = calculator.calculate(terms(MutualTermMode.DURATION, 12,
                MutualTermUnit.MONTHS, null, 12, "1200", "0"));
        assertThat(plan.installments().get(0).dueDate()).isEqualTo("2026-02-28");
        assertThat(plan.installments().get(1).dueDate()).isEqualTo("2026-03-31");
        assertThat(plan.installments().getLast().dueDate()).isEqualTo("2027-01-31");
        assertThat(plan.interest()).isZero();
        assertThat(plan.periodicity()).isEqualTo("Cada 1 mes");
    }

    @Test
    void supportsTwoYearsAndLeapDay() {
        MutualFinancialPlan twoYears = calculator.calculate(terms(MutualTermMode.DURATION, 2,
                MutualTermUnit.YEARS, null, 8, "800", "1"));
        assertThat(twoYears.dueDate()).isEqualTo("2028-01-31");
        assertThat(twoYears.installments()).hasSize(8);
        MutualFinancialPlan leap = calculator.calculate(terms(LocalDate.of(2024, 2, 29),
                MutualTermMode.DURATION, 1, MutualTermUnit.YEARS, null, 1, "100", "0"));
        assertThat(leap.dueDate()).isEqualTo("2025-02-28");
    }

    @Test
    void supportsSpecificDateAndNonDivisiblePeriodsWithoutDrift() {
        MutualFinancialPlan plan = calculator.calculate(terms(MutualTermMode.SPECIFIC_DATE, null,
                null, LocalDate.parse("2026-02-10"), 3, "300", "0"));
        assertThat(plan.installments()).extracting(MutualFinancialPlan.Installment::dueDate)
                .containsExactly(LocalDate.parse("2026-02-03"), LocalDate.parse("2026-02-06"),
                        LocalDate.parse("2026-02-10"));
        assertThat(plan.installments()).extracting(MutualFinancialPlan.Installment::total)
                .allSatisfy(value -> assertThat(value).isEqualByComparingTo("100.00"));
    }

    @Test
    void rejectsInvalidEndDateAndInstallmentCount() {
        assertThatThrownBy(() -> calculator.calculate(terms(MutualTermMode.SPECIFIC_DATE, null,
                null, LocalDate.parse("2026-01-31"), 1, "100", "0")))
                .isInstanceOf(CarSaleRequestValidationException.class);
        assertThatThrownBy(() -> calculator.calculate(terms(MutualTermMode.DURATION, 10,
                MutualTermUnit.DAYS, null, 0, "100", "0")))
                .isInstanceOf(CarSaleRequestValidationException.class);
    }

    private MutualTerms terms(MutualTermMode mode, Integer quantity, MutualTermUnit unit,
                              LocalDate dueDate, int count, String amount, String interest) {
        return terms(LocalDate.of(2026, 1, 31), mode, quantity, unit, dueDate, count, amount, interest);
    }

    private MutualTerms terms(LocalDate start, MutualTermMode mode, Integer quantity,
                              MutualTermUnit unit, LocalDate dueDate, int count,
                              String amount, String interest) {
        return new MutualTerms(amount, "PLAZO", "FECHA", String.valueOf(count), "CUOTA",
                "BANCO", "CUENTA", interest, "", "CONSUMO", false, "", "",
                "", "SANTA TECLA", "LA LIBERTAD", "FECHA", "HORA", "No", "No",
                new BigDecimal(amount), mode, quantity, unit, start, dueDate, count,
                new BigDecimal(interest), MutualInstrumentType.PRIVATE_AUTHENTICATED,
                MutualGuaranteeType.NONE, null, "", "");
    }
}
