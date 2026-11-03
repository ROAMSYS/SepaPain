package com.roamsys.sepa.pain.facade;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Contains informations about a SEPA credit transfer transaction.
 *
 * @param creditor the creditor
 * @param amount the amount of this transaction in EUR (0.01 - 999999999.99, at most 2 fraction digits)
 * @param message the message of this transaction
 */
public record SepaTransactionInfo(SepaBankAccount creditor, BigDecimal amount, String message) {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");

    public SepaTransactionInfo {
        Objects.requireNonNull(creditor);
        Arguments.requireBetween(amount, MIN_AMOUNT, MAX_AMOUNT);
        Arguments.requireMaxFractionDigits(amount, 2);
        Arguments.requireLength(message, 1, 140);
    }
}
