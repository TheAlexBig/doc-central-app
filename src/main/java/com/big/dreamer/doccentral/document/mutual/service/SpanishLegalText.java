package com.big.dreamer.doccentral.document.mutual.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SpanishLegalText {
    private static final String[] UNITS = {
            "CERO", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"
    };
    private static final String[] SPECIAL = {
            "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISÉIS", "DIECISIETE",
            "DIECIOCHO", "DIECINUEVE", "VEINTE", "VEINTIUNO", "VEINTIDÓS", "VEINTITRÉS",
            "VEINTICUATRO", "VEINTICINCO", "VEINTISÉIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"
    };
    private static final String[] TENS = {
            "", "", "", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
    };
    private static final String[] HUNDREDS = {
            "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
    };
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private SpanishLegalText() {
    }

    static String money(BigDecimal value) {
        BigDecimal normalized = value.setScale(2, RoundingMode.HALF_UP);
        long whole = normalized.longValue();
        int cents = normalized.remainder(BigDecimal.ONE).movePointRight(2).abs().intValue();
        String dollars = whole == 1 ? "UN DÓLAR" : apocopate(number(whole)) + " DÓLARES";
        if (cents == 0) return dollars + " DE LOS ESTADOS UNIDOS DE AMÉRICA";
        String centText = cents == 1 ? "UN CENTAVO" : apocopate(number(cents)) + " CENTAVOS";
        return dollars + " CON " + centText + " DE DÓLAR DE LOS ESTADOS UNIDOS DE AMÉRICA";
    }

    static String date(LocalDate value) {
        return number(value.getDayOfMonth()) + " DE " + month(value.getMonth())
                + " DE " + number(value.getYear());
    }

    static String replaceDigits(String value) {
        Matcher matcher = DIGITS.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(number(Long.parseLong(matcher.group()))));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    static String number(long value) {
        if (value < 0) return "MENOS " + number(-value);
        if (value < 1_000) return belowThousand((int) value);
        if (value < 1_000_000) {
            long thousands = value / 1_000;
            long rest = value % 1_000;
            String prefix = thousands == 1 ? "MIL" : apocopate(number(thousands)) + " MIL";
            return rest == 0 ? prefix : prefix + " " + number(rest);
        }
        if (value < 1_000_000_000_000L) {
            long millions = value / 1_000_000;
            long rest = value % 1_000_000;
            String prefix = millions == 1 ? "UN MILLÓN" : apocopate(number(millions)) + " MILLONES";
            return rest == 0 ? prefix : prefix + " " + number(rest);
        }
        return Long.toString(value).chars()
                .mapToObj(character -> UNITS[character - '0'])
                .reduce((left, right) -> left + " " + right).orElse("");
    }

    private static String belowThousand(int value) {
        if (value < 10) return UNITS[value];
        if (value < 30) return SPECIAL[value - 10];
        if (value < 100) {
            int unit = value % 10;
            return unit == 0 ? TENS[value / 10] : TENS[value / 10] + " Y " + UNITS[unit];
        }
        if (value == 100) return "CIEN";
        int rest = value % 100;
        return rest == 0 ? HUNDREDS[value / 100] : HUNDREDS[value / 100] + " " + belowThousand(rest);
    }

    private static String apocopate(String value) {
        return value.replaceFirst("VEINTIUNO$", "VEINTIÚN")
                .replaceFirst(" Y UNO$", " Y UN")
                .replaceFirst("UNO$", "UN");
    }

    private static String month(Month month) {
        return switch (month) {
            case JANUARY -> "ENERO";
            case FEBRUARY -> "FEBRERO";
            case MARCH -> "MARZO";
            case APRIL -> "ABRIL";
            case MAY -> "MAYO";
            case JUNE -> "JUNIO";
            case JULY -> "JULIO";
            case AUGUST -> "AGOSTO";
            case SEPTEMBER -> "SEPTIEMBRE";
            case OCTOBER -> "OCTUBRE";
            case NOVEMBER -> "NOVIEMBRE";
            case DECEMBER -> "DICIEMBRE";
        };
    }
}
