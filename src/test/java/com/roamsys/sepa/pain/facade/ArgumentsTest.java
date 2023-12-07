package com.roamsys.sepa.pain.facade;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test of {@link Arguments}.
 */
class ArgumentsTest {

    @Test
    void testRequireNotNegative_PositiveValue() {
        assertDoesNotThrow(() -> Arguments.requireNotNegative(BigDecimal.ONE));
    }

    @Test
    void testRequireNotNegative_ZeroValue() {
        assertDoesNotThrow(() -> Arguments.requireNotNegative(BigDecimal.ZERO));
    }

    @Test
    void testRequireNotNegative_NegativeValue() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Arguments.requireNotNegative(BigDecimal.ONE.negate()));
        assertEquals("The value must not be negative", exception.getMessage());
    }

    @Test
    void testRequireLength_ValidLength() {
        assertDoesNotThrow(() -> Arguments.requireLength("ValidString", 5, 12));
    }

    @Test
    void testRequireLength_InvalidLength() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Arguments.requireLength("Short", 6, 15));
        assertEquals("The length of the string must be between 6 and 15 characters", exception.getMessage());
    }

    @Test
    void testRequireNotEmpty_ValidSize() {
        assertDoesNotThrow(() -> Arguments.requireNotEmpty(List.of(1, 2, 3)));
    }

    @Test
    void testRequireMinSize_InvalidSize() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Arguments.requireNotEmpty(List.of()));
        assertEquals("The collection must not be empty", exception.getMessage());
    }

    @Test
    void testRequireMatches_ValidRegex() {
        assertDoesNotThrow(() -> Arguments.requireMatches("12345", "\\d+"));
    }

    @Test
    void testRequireMatches_InvalidRegex() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Arguments.requireMatches("abc", "\\d+"));
        assertEquals("'abc' does not matcht the expected pattern '\\d+'", exception.getMessage());
    }
}
