package com.roamsys.sepa.pain.facade;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Contains informations about a SEPA credit transfer transaction.
 *
 * @param creditor the creditor
 * @param amount the amount of this transaction
 * @param message the message of this transaction
 */
public record SepaTransactionInfo(SepaBankAccount creditor, BigDecimal amount, String message) {
        public SepaTransactionInfo {
            Objects.requireNonNull(creditor);
            Arguments.requireNotNegative(amount);
            Arguments.requireLength(message, 1, 140);
        }
    }
