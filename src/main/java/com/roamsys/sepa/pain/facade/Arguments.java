package com.roamsys.sepa.pain.facade;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Helper class to validate method arguments.
 */
class Arguments {

    static void requireBetween(final BigDecimal value, final BigDecimal min, final BigDecimal max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new IllegalArgumentException(String.format("The value must be between %s and %s", min.toPlainString(), max.toPlainString()));
        }
    }

    static void requireMaxFractionDigits(final BigDecimal value, final int maxFractionDigits) {
        if (value.stripTrailingZeros().scale() > maxFractionDigits) {
            throw new IllegalArgumentException(String.format("The value must not have more than %d fraction digits", maxFractionDigits));
        }
    }

    static void requireLength(final String value, final int min, final int max) {
        if (value.length() < min || value.length() > max) {
            throw new IllegalArgumentException(String.format("The length of the string must be between %d and %d characters", min, max));
        }
    }

    static void requireNotEmpty(final Collection<?> value) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("The collection must not be empty");
        }
    }

    static void requireMatches(final String value, final String regex) {
        if (!value.matches(regex)) {
            throw new IllegalArgumentException(String.format("'%s' does not match the expected pattern '%s'", value, regex));
        }
    }
}
