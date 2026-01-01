package com.mastering.mega.util;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@UtilityClass
public class NumberUtil {

    private static final NumberFormat SPACE_GROUPING_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');

        SPACE_GROUPING_FORMAT = NumberFormat.getIntegerInstance(Locale.ROOT);
        ((java.text.DecimalFormat) SPACE_GROUPING_FORMAT).setDecimalFormatSymbols(symbols);
        SPACE_GROUPING_FORMAT.setParseIntegerOnly(true);
    }

    public static boolean isValidIntegerWithSpaces(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }

        String trimmed = str.trim();

        try {
            Integer.parseInt(trimmed);
            return true;
        } catch (NumberFormatException ignored) {}

        String normalized = trimmed.replace('\u00A0', ' ');

        try {
            SPACE_GROUPING_FORMAT.parse(normalized);
            return true;
        } catch (ParseException ignored) {}

        return false;
    }

    public static Integer tryParseIntWithSpaces(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }

        String trimmed = str.trim();

        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {}

        String normalized = trimmed.replace('\u00A0', ' ');
        try {
            Number number = SPACE_GROUPING_FORMAT.parse(normalized);
            long value = number.longValue();
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return (int) value;
            }
        } catch (ParseException ignored) {}

        return null;
    }

    public static int parseIntWithSpaces(String str) throws NumberFormatException {
        Integer result = tryParseIntWithSpaces(str);
        if (result == null) {
            throw new NumberFormatException("Cannot parse to integer: \"" + str + "\"");
        }
        return result;
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
