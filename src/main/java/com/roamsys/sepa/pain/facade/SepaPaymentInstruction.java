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
 * @author AndreasK
 */
public record SepaPaymentInstruction(String reference, SepaBankAccount debtor, LocalDate requestedExecutionDate, List<SepaTransactionInfo> transactions) {
    public SepaPaymentInstruction {
        Arguments.requireLength(reference, 1, 35);
        Objects.requireNonNull(debtor);
        Objects.requireNonNull(requestedExecutionDate);
        Arguments.requireNotEmpty(transactions);
    }

    BigDecimal getTransactionAmountSum() {
        return transactions.stream()
                .map(SepaTransactionInfo::amount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.valueOf(0, 2));
    }
}