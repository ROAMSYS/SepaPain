package com.roamsys.sepa.pain.facade;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Helper class to validate method arguments.
 */
class Arguments {

    static void requireNotNegative(final BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The value must not be negative");
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
            throw new IllegalArgumentException(String.format("'%s' does not matcht the expected pattern '%s'", value, regex));
        }
    }
}
