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
    void testRequireBetween_ValueInRange() {
        assertDoesNotThrow(() -> Arguments.requireBetween(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN));
    }

    @Test
    void testRequireBetween_ValueEqualsBounds() {
        assertDoesNotThrow(() -> Arguments.requireBetween(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN));
        assertDoesNotThrow(() -> Arguments.requireBetween(BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN));
    }

    @Test
    void testRequireBetween_ValueTooSmall() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Arguments.requireBetween(new BigDecimal("0.00"), new BigDecimal("0.01"), BigDecimal.TEN));
        assertEquals("The value must be between 0.01 and 10", exception.getMessage());
    }

    @Test
    void testRequireBetween_ValueTooLarge() {
        assertThrows(IllegalArgumentException.class, () -> Arguments.requireBetween(new BigDecimal("10.01"), BigDecimal.ZERO, BigDecimal.TEN));
    }

    @Test
    void testRequireMaxFractionDigits_ValidValue() {
        assertDoesNotThrow(() -> Arguments.requireMaxFractionDigits(new BigDecimal("1.23"), 2));
        assertDoesNotThrow(() -> Arguments.requireMaxFractionDigits(new BigDecimal("1.2300"), 2));
    }

    @Test
    void testRequireMaxFractionDigits_TooManyFractionDigits() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Arguments.requireMaxFractionDigits(new BigDecimal("1.234"), 2));
        assertEquals("The value must not have more than 2 fraction digits", exception.getMessage());
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
        assertEquals("'abc' does not match the expected pattern '\\d+'", exception.getMessage());
    }
}
