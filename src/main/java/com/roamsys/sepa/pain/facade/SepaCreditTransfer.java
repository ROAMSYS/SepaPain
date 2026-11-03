package com.roamsys.sepa.pain.facade;

import java.math.BigDecimal;
import java.util.List;

/**
 * The root data class to create SEPA credit transfer pain file.
 *
 * @param messageId a unique identifier (1-35 characters, see {@link SepaBankAccount#SEPA_ID_REGEX})
 * @param initiatingPartyName the name of the party who initiates the transactions (1-70 characters)
 * @param paymentInstructions the payment transaction
 */
public record SepaCreditTransfer(String messageId, String initiatingPartyName, List<SepaPaymentInstruction> paymentInstructions) {

    public SepaCreditTransfer {
        Arguments.requireMatches(messageId, SepaBankAccount.SEPA_ID_REGEX);
        Arguments.requireLength(initiatingPartyName, 1, 70);
        Arguments.requireNotEmpty(paymentInstructions);
    }
    int getTotalTransactionCount() {
        return paymentInstructions.stream()
                .mapToInt(x -> x.transactions().size())
                .reduce(Integer::sum)
                .orElse(0);
    }

    BigDecimal getTransactionAmountSum() {
        return paymentInstructions.stream()
                .map(SepaPaymentInstruction::getTransactionAmountSum)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.valueOf(0, 2));
    }
}