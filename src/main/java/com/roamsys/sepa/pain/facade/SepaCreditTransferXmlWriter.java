package com.roamsys.sepa.pain.facade;

import com.roamsys.sepa.pain.jaxb.model.AccountIdentification4Choice;
import com.roamsys.sepa.pain.jaxb.model.ActiveOrHistoricCurrencyAndAmount;
import com.roamsys.sepa.pain.jaxb.model.AmountType3Choice;
import com.roamsys.sepa.pain.jaxb.model.BranchAndFinancialInstitutionIdentification4;
import com.roamsys.sepa.pain.jaxb.model.CashAccount16;
import com.roamsys.sepa.pain.jaxb.model.ChargeBearerType1Code;
import com.roamsys.sepa.pain.jaxb.model.CreditTransferTransactionInformation10;
import com.roamsys.sepa.pain.jaxb.model.CustomerCreditTransferInitiationV03;
import com.roamsys.sepa.pain.jaxb.model.Document;
import com.roamsys.sepa.pain.jaxb.model.FinancialInstitutionIdentification7;
import com.roamsys.sepa.pain.jaxb.model.GroupHeader32;
import com.roamsys.sepa.pain.jaxb.model.ObjectFactory;
import com.roamsys.sepa.pain.jaxb.model.PartyIdentification32;
import com.roamsys.sepa.pain.jaxb.model.PaymentIdentification1;
import com.roamsys.sepa.pain.jaxb.model.PaymentInstructionInformation3;
import com.roamsys.sepa.pain.jaxb.model.PaymentMethod3Code;
import com.roamsys.sepa.pain.jaxb.model.PaymentTypeInformation19;
import com.roamsys.sepa.pain.jaxb.model.RemittanceInformation5;
import com.roamsys.sepa.pain.jaxb.model.ServiceLevel8Choice;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.Writer;
import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Creates SEPA credit transfer PAIN files (pain.001.001.03).
 *
 * @author AndreasK
 */
public class SepaCreditTransferXmlWriter {

    private static final String EURO_CURRENCY = "EUR";
    private static final String SCHEMA_LOCATION = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03 pain.001.001.03.xsd";
    private static JAXBContext context = null;

    private static synchronized JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(Document.class);
        }
        return context;
    }

    private final Clock clock;

    public SepaCreditTransferXmlWriter() {
        this(Clock.systemDefaultZone());
    }

    public SepaCreditTransferXmlWriter(final Clock clock) {
        this.clock = clock;
    }

    /**
     * Writes a SEPA credit transfer PAIN file (pain.001.001.03).
     *
     * @param sepaData the content of the XML document
     * @param writer the {@link Writer} to write to
     */
    public void write(final SepaCreditTransfer sepaData, final Writer writer) throws SepaCreditTransferXmllWriterException {
        try {
            createMarshaller().marshal(createDocument(sepaData), writer);
        } catch (final JAXBException e) {
            throw new SepaCreditTransferXmllWriterException(e);
        }
    }

    private JAXBElement<Document> createDocument(final SepaCreditTransfer sepaData) {
        final Document document = new Document();
        final CustomerCreditTransferInitiationV03 initiation = new CustomerCreditTransferInitiationV03();
        document.setCstmrCdtTrfInitn(initiation);
        initiation.setGrpHdr(createHeader(sepaData));
        sepaData.paymentInstructions().stream()
                .map(this::createPaymentInstruction)
                .forEach(initiation.getPmtInf()::add);
        return new ObjectFactory().createDocument(document);
    }

    private Marshaller createMarshaller() throws JAXBException {
        final Marshaller marshaller = getContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION);
        return marshaller;
    }

    private GroupHeader32 createHeader(final SepaCreditTransfer sepaData) {
        final GroupHeader32 header = new GroupHeader32();
        header.setMsgId(sepaData.messageId());
        header.setCreDtTm(OffsetDateTime.now(clock));
        header.setNbOfTxs(Integer.toString(sepaData.getTotalTransactionCount()));
        header.setCtrlSum(sepaData.getTransactionAmountSum());
        header.setInitgPty(createPartyIdentification(sepaData.initiatingPartyName()));
        return header;
    }

    private PartyIdentification32 createPartyIdentification(final String partyName) {
        final PartyIdentification32 paryIdentification = new PartyIdentification32();
        if (partyName != null && !partyName.isEmpty()) {
            paryIdentification.setNm(partyName);
        }
        return paryIdentification;
    }

    private PaymentInstructionInformation3 createPaymentInstruction(final SepaPaymentInstruction data) {
        final PaymentInstructionInformation3 payment = new PaymentInstructionInformation3();
        payment.setPmtInfId(data.reference());
        payment.setPmtMtd(PaymentMethod3Code.TRF);
        payment.setBtchBookg(true);
        payment.setNbOfTxs(Integer.toString(data.transactions().size()));
        payment.setCtrlSum(data.getTransactionAmountSum());
        payment.setPmtTpInf(createPaymentTypeInfo());
        payment.setReqdExctnDt(data.requestedExecutionDate());
        payment.setDbtr(createPartyIdentification(data.debtor().owner()));
        payment.setDbtrAcct(createCashAccount(data.debtor().iban()));
        payment.setDbtrAgt(createFinancialInstitut(data.debtor().bic()));
        payment.setChrgBr(ChargeBearerType1Code.SLEV);
        data.transactions().stream()
                .map(this::createTransactionInfo)
                .forEach(payment.getCdtTrfTxInf()::add);
        return payment;

    }

    private CreditTransferTransactionInformation10 createTransactionInfo(final SepaTransactionInfo data) {
        final CreditTransferTransactionInformation10 transactionInfo = new CreditTransferTransactionInformation10();

        final PaymentIdentification1 value = new PaymentIdentification1();
        value.setEndToEndId("NOTPROVIDED");
        transactionInfo.setPmtId(value);
        transactionInfo.setAmt(createAmount(data));
        transactionInfo.setCdtrAgt(createFinancialInstitut(data.creditor().bic()));
        transactionInfo.setCdtr(createPartyIdentification(data.creditor().owner()));
        transactionInfo.setCdtrAcct(createCashAccount(data.creditor().iban()));
        transactionInfo.setRmtInf(createRemittanceInfo(data.message()));
        return transactionInfo;
    }

    private PaymentTypeInformation19 createPaymentTypeInfo() {
        final PaymentTypeInformation19 info = new PaymentTypeInformation19();
        final ServiceLevel8Choice choice = new ServiceLevel8Choice();
        choice.setCd("SEPA");
        info.setSvcLvl(choice);
        return info;
    }

    private CashAccount16 createCashAccount(final String iban) {
        final CashAccount16 debtorAccount = new CashAccount16();
        final AccountIdentification4Choice debtorAccountInfo = new AccountIdentification4Choice();
        debtorAccountInfo.setIBAN(iban);
        debtorAccount.setId(debtorAccountInfo);
        return debtorAccount;
    }

    private BranchAndFinancialInstitutionIdentification4 createFinancialInstitut(final String bic) {
        final BranchAndFinancialInstitutionIdentification4 institut = new BranchAndFinancialInstitutionIdentification4();
        final FinancialInstitutionIdentification7 identification = new FinancialInstitutionIdentification7();
        identification.setBIC(bic);
        institut.setFinInstnId(identification);
        return institut;
    }

    private RemittanceInformation5 createRemittanceInfo(final String message) {
        final RemittanceInformation5 remittanceInfo = new RemittanceInformation5();
        remittanceInfo.getUstrd().add(message);
        return remittanceInfo;
    }

    private AmountType3Choice createAmount(final SepaTransactionInfo data) {
        final AmountType3Choice amount = new AmountType3Choice();
        final ActiveOrHistoricCurrencyAndAmount currencyAndAmount = new ActiveOrHistoricCurrencyAndAmount();
        currencyAndAmount.setCcy(EURO_CURRENCY);
        currencyAndAmount.setValue(data.amount());
        amount.setInstdAmt(currencyAndAmount);
        return amount;
    }
}
