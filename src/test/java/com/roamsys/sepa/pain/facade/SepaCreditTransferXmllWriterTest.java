package com.roamsys.sepa.pain.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test of {@link SepaCreditTransferXmllWriter}.
 */
class SepaCreditTransferXmllWriterTest {

    private final Clock clock = Clock.fixed(Instant.parse("2023-11-01T12:30:15.8+01:00"), ZoneId.of("CET"));
    @Test
    void testOneInstructionManyTransactions() throws IOException, URISyntaxException {
        final var debtor = new SepaBankAccount("Roamsys S.A.", "COBADEFFXXX", "DE02100100100006820101");
        final var creditor1 = new SepaBankAccount("John Doe", "INGBNL2AXXX", "NL50INGB4362244417");
        final var creditor2 = new SepaBankAccount("Erika Mustermann", "FOTNLULLXXX", "LU450103584193959868");
        final var creditor3 = new SepaBankAccount("Max Mustermann", "FOTNLULLXXX", "DE02300606010002474689");
        final var paymentInstruction = new SepaPaymentInstruction("ABCDEFG", debtor, LocalDate.of(2023, 11, 15),
                List.of(new SepaTransactionInfo(creditor1, new BigDecimal("27.53"), "Contract 1 - Nov 2023"),
                        new SepaTransactionInfo(creditor2, new BigDecimal("82.6"), "Contract 2 - Nov 2023"),
                        new SepaTransactionInfo(creditor3, new BigDecimal("27.53"), "Contract 3 - Nov 2023")));
        final var sepaCreditTransfer = new SepaCreditTransfer("COBADEFFXXX0020230716110719", "Roamsys S.A.", List.of(paymentInstruction));
        assertXmlEquals("oneInstructionManyTransactions.xml", sepaCreditTransfer);
    }

    @Test
    void tesManyInstructions() throws IOException, URISyntaxException {
        final var debtor = new SepaBankAccount("Roamsys S.A.", "COBADEFFXXX", "DE02100100100006820101");
        final var creditor = new SepaBankAccount("John Doe", "INGBNL2AXXX", "NL50INGB4362244417");
        final var instruction1 = new SepaPaymentInstruction("ABCDEFG", debtor, LocalDate.of(2023, 11, 15),
                List.of(new SepaTransactionInfo(creditor, new BigDecimal("27.53"), "Nov 2023")));
        final var instruction2 = new SepaPaymentInstruction("12345678", debtor, LocalDate.of(2023, 12, 15),
                List.of(new SepaTransactionInfo(creditor, new BigDecimal("27.53"), "Dec 2023")));
        final var instruction3 = new SepaPaymentInstruction("12345678", debtor, LocalDate.of(2024, 1, 15),
                List.of(new SepaTransactionInfo(creditor, new BigDecimal("27.53"), "Jan 2024")));
        final var sepaCreditTransfer = new SepaCreditTransfer("COBADEFFXXX0020230716110719", "Company 1 S.A.", List.of(instruction1, instruction2, instruction3));
        assertXmlEquals("manyInstructions.xml", sepaCreditTransfer);
    }

    private void assertXmlEquals(final String fileName, final SepaCreditTransfer sepaCreditTransfer) throws IOException, URISyntaxException {
        final StringWriter writer = new StringWriter();
        new SepaCreditTransferXmllWriter(clock).write(sepaCreditTransfer, writer);
        final String expected = Files.readString(Paths.get(getClass().getClassLoader().getResource("expectedXml/" + fileName).toURI()));
        assertEquals(expected, writer.toString());
    }
}
