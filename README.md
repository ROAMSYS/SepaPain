# SepaPain
SEPA is an initiative of the European Union that aims to standardise and facilitate payment transactions within Europe. The PAIN format is used specifically for data exchange in the area of payment transactions.
This library creates "pain.001.001.09" XML files for SEPA Credit Transfer Initiation according to the Technical
Validation Subset (TVS) of the German Banking Industry Committee (`pain.001.001.09_GBIC_5.xsd`).
It is used to transmit information about a credit transfer from a payer to a payee.

### Versions

| Version | PAIN format              |
|---------|--------------------------|
| 3.x     | pain.001.001.09 (GBIC_5) |
| 2.x     | pain.001.001.03          |

The facade API of 3.x is compatible with 2.x. The validations follow the restrictions of the new schema:

### Example usage: 
```java
    final StringWriter writer = new StringWriter();
    final var debtor = new SepaBankAccount("Max Mustermann", "COBADEFFXXX", "DE40850400611005507328");
    final var creditor = new SepaBankAccount("John Doe", "INGBNL2AXXX", "NL50INGB4362244417");
    final var paymentInstruction = new SepaPaymentInstruction("1234567890", debtor, LocalDate.of(2023, 11, 16),
            List.of(new SepaTransactionInfo(creditor, new BigDecimal("27.53"), "Project 1 - Jan 2023")), false);
    final var sepaCreditTransfer = new SepaCreditTransfer("COBADEFFXXX0020230716110719", "Roamsys S.A.", List.of(paymentInstruction));
    new

SepaCreditTransferXmlWriter().

write(sepaCreditTransfer, writer);
    System.out.println(writer.toString());
```
