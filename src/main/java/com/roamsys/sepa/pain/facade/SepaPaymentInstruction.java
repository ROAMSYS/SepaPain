package com.roamsys.sepa.pain.facade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Information about a SEPA payment instruction.
 *
 * @param reference unique reference for the payment (1-35 characters)
 * @param debtor the debtor account
 * @param requestedExecutionDate the requested execution date
 * @param transactions the transaction list
 * @param batchBooking <code>true</code> consolidates all transactions in the SEPA file into a single total amount entry on the bank statement
 * @author AndreasK
 */
public record SepaPaymentInstruction(String reference, SepaBankAccount debtor, LocalDate requestedExecutionDate, List<SepaTransactionInfo> transactions, boolean batchBooking) {
    public SepaPaymentInstruction {
        Arguments.requireLength(reference, 1, 35);
        Objects.requireNonNull(debtor);
        Objects.requireNonNull(requestedExecutionDate);
        Arguments.requireNotEmpty(transactions);
        Objects.requireNonNull(batchBooking);
    }

    BigDecimal getTransactionAmountSum() {
        return transactions.stream()
                .map(SepaTransactionInfo::amount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.valueOf(0, 2));
    }
}