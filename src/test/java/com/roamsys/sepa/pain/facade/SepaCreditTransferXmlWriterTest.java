package com.roamsys.sepa.pain.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * Test of {@link SepaCreditTransferXmlWriter}.
 */
class SepaCreditTransferXmlWriterTest {

    private static final String SCHEMA_FILE = "xsd/pain.001.001.09_GBIC_5.xsd";

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
                        new SepaTransactionInfo(creditor3, new BigDecimal("27.53"), "Contract 3 - Nov 2023")), false);
        final var sepaCreditTransfer = new SepaCreditTransfer("COBADEFFXXX0020230716110719", "Roamsys S.A.", List.of(paymentInstruction));
        assertXmlEquals("oneInstructionManyTransactions.xml", sepaCreditTransfer);
    }

    @Test
    void testBicOptional() throws IOException, URISyntaxException {
        final var debtor = new SepaBankAccount("Roamsys S.A.", null, "DE02100100100006820101");
        final var creditor1 = new SepaBankAccount("John Doe", null, "NL50INGB4362244417");
        final var paymentInstruction = new SepaPaymentInstruction("ABCDEFG", debtor, LocalDate.of(2023, 11, 15),
                List.of(new SepaTransactionInfo(creditor1, new BigDecimal("27.53"), "Contract 1 - Nov 2023")), true);
        final var sepaCreditTransfer = new SepaCreditTransfer("COBADEFFXXX0020230716110719", "Roamsys S.A.", List.of(paymentInstruction));
        assertXmlEquals("bicOptional.xml", sepaCreditTransfer);
    }

    @Test
    void testManyInstructions() throws IOException, URISyntaxException {
        final var debtor = new SepaBankAccount("Roamsys S.A.", "COBADEFFXXX", "DE02100100100006820101");
        final var creditor = new SepaBankAccount("John Doe", "INGBNL2AXXX", "NL50INGB4362244417");
        final var instruction1 = new SepaPaymentInstruction("ABCDEFG", debtor, LocalDate.of(2023, 11, 15),
                List.of(new SepaTransactionInfo(creditor, new BigDecimal("27.53"), "Nov 2023")), true);
        final var instruction2 = new SepaPaymentInstruction("12345678", debtor, LocalDate.of(2023, 12, 15),
                List.of(new SepaTransactionInfo(creditor, new BigDecimal("27.53"), "Dec 2023")), true);
        final var instruction3 = new SepaPaymentInstruction("12345678", debtor, LocalDate.of(2024, 1, 15),
                List.of(new SepaTransactionInfo(creditor, new BigDecimal("27.53"), "Jan 2024")), true);
        final var sepaCreditTransfer = new SepaCreditTransfer("COBADEFFXXX0020230716110719", "Company 1 S.A.", List.of(instruction1, instruction2, instruction3));
        assertXmlEquals("manyInstructions.xml", sepaCreditTransfer);
    }

    /**
     * Rebuilds the example of the german banking industry (ebicsExample001.001.09.xml). The facade always writes "NOTPROVIDED" as end-to-end ID.
     */
    @Test
    void testEbicsExample() throws IOException, URISyntaxException {
        final var debtor = new SepaBankAccount("Debtor Name", "BANKDEFFXXX", "DE87200500001234567890");
        final var creditor1 = new SepaBankAccount("Firma Musterland GmbH", "SPUEDE2UXXX", "DE21500500009876543210");
        final var creditor2 = new SepaBankAccount("Other Creditor Name", "SPUEDE2UXXX", "DE21500500001234567897");
        final var paymentInstruction = new SepaPaymentInstruction("Payment-Information-ID-4711", debtor, LocalDate.of(2025, 10, 10),
                List.of(new SepaTransactionInfo(creditor1, new BigDecimal("6543.14"), "Unstructured Remittance Information"),
                        new SepaTransactionInfo(creditor2, new BigDecimal("112.72"), "Haribo Bonn/Fruchtgummiweg 13/2025-10-02T11:37/ - hier noch weitere Infos ")),
                true);
        final var sepaCreditTransfer = new SepaCreditTransfer("Message-ID-4711", "Initiator Name", List.of(paymentInstruction));
        final Clock utcClock = Clock.fixed(Instant.parse("2025-10-07T09:30:47Z"), ZoneOffset.UTC);
        assertXmlEquals("ebicsExample001.001.09.xml", sepaCreditTransfer, utcClock);
    }

    private void assertXmlEquals(final String fileName, final SepaCreditTransfer sepaCreditTransfer) throws IOException, URISyntaxException {
        assertXmlEquals(fileName, sepaCreditTransfer, clock);
    }

    private void assertXmlEquals(final String fileName, final SepaCreditTransfer sepaCreditTransfer, final Clock writerClock) throws IOException, URISyntaxException {
        final StringWriter writer = new StringWriter();
        new SepaCreditTransferXmlWriter(writerClock).write(sepaCreditTransfer, writer);
        String expected = Files.readString(Paths.get(getClass().getClassLoader().getResource("expectedXml/" + fileName).toURI()));
        expected = expected.replaceAll("\r", "");
        assertEquals(expected, writer.toString());
        assertSchemaValid(writer.toString());
    }

    private void assertSchemaValid(final String xml) throws IOException {
        try {
            final Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(getClass().getClassLoader().getResource(SCHEMA_FILE));
            schema.newValidator().validate(new StreamSource(new StringReader(xml)));
        } catch (final SAXException e) {
            throw new AssertionError("The XML document does not match the schema " + SCHEMA_FILE, e);
        }
    }
}
