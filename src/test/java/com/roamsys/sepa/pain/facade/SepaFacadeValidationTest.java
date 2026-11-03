package com.roamsys.sepa.pain.facade;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the validations of the facade records against the restrictions of pain.001.001.09 (GBIC_5).
 *
 * @author AndreasK
 */
class SepaFacadeValidationTest {

    private static final String NAME_70 = "N".repeat(70);

    private final SepaBankAccount account = new SepaBankAccount("John Doe", "INGBNL2AXXX", "NL50INGB4362244417");

    @Test
    void testSepaBankAccount_MaxLengthName() {
        assertDoesNotThrow(() -> new SepaBankAccount(NAME_70, null, "NL50INGB4362244417"));
    }

    @Test
    void testSepaBankAccount_Validation_MaxLengthExceeded() {
        assertThrows(IllegalArgumentException.class, () -> new SepaBankAccount(NAME_70 + "N", null, "NL50INGB4362244417"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"COBADEFF", "COBADEFFXXX", "1234DEFF", "SOGEDEFF100"})
    void testSepaBankAccount_ValidBic(final String bic) {
        assertDoesNotThrow(() -> new SepaBankAccount("John Doe", bic, "NL50INGB4362244417"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"COBADEF", "COBA1EFFXXX", "cobadeffxxx", "COBADEFFXX"})
    void testSepaBankAccount_Validation_InvalidBic(final String bic) {
        assertThrows(IllegalArgumentException.class, () -> new SepaBankAccount("John Doe", bic, "NL50INGB4362244417"));
    }

    @Test
    void testSepaCreditTransfer_MaxLengthInitiatingPartyName() {
        assertDoesNotThrow(() -> new SepaCreditTransfer("MSG-1", NAME_70, List.of(createInstruction("REF-1"))));
    }

    @Test
    void testSepaCreditTransfer_Validation_InitiatingPartyNameMaxLengthExceeded() {
        assertThrows(IllegalArgumentException.class, () -> new SepaCreditTransfer("MSG-1", NAME_70 + "N", List.of(createInstruction("REF-1"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Message-ID-4711", "a+?/-:().,'| z", "12345678901234567890123456789012345"})
    void testSepaCreditTransfer_ValidMessageId(final String messageId) {
        assertDoesNotThrow(() -> new SepaCreditTransfer(messageId, "Roamsys S.A.", List.of(createInstruction("REF-1"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Message_ID", "Nachricht-Ä", "123456789012345678901234567890123456"})
    void testSepaCreditTransfer_Validation_InvalidMessageId(final String messageId) {
        assertThrows(IllegalArgumentException.class, () -> new SepaCreditTransfer(messageId, "Roamsys S.A.", List.of(createInstruction("REF-1"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "REF#1", "REF_1", "123456789012345678901234567890123456"})
    void testSepaPaymentInstruction_Validation_InvalidReference(final String reference) {
        assertThrows(IllegalArgumentException.class, () -> createInstruction(reference));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "27.53", "27.5", "27.500", "999999999.99"})
    void testSepaTransactionInfo_ValidAmount(final String amount) {
        assertDoesNotThrow(() -> new SepaTransactionInfo(account, new BigDecimal(amount), "Message"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1", "0.009", "27.531", "1000000000.00"})
    void testSepaTransactionInfo_Validation_InvalidAmount(final String amount) {
        assertThrows(IllegalArgumentException.class, () -> new SepaTransactionInfo(account, new BigDecimal(amount), "Message"));
    }

    private SepaPaymentInstruction createInstruction(final String reference) {
        return new SepaPaymentInstruction(reference, account, LocalDate.of(2025, 10, 10),
                List.of(new SepaTransactionInfo(account, BigDecimal.TEN, "Message")), false);
    }
}
