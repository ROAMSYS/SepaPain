package com.roamsys.sepa.pain.facade;

import com.roamsys.sepa.pain.jaxb.model.AccountIdentification4Choice;
import com.roamsys.sepa.pain.jaxb.model.ActiveOrHistoricCurrencyAndAmount;
import com.roamsys.sepa.pain.jaxb.model.AmountType4Choice;
import com.roamsys.sepa.pain.jaxb.model.BranchAndFinancialInstitutionIdentification6;
import com.roamsys.sepa.pain.jaxb.model.CashAccount38;
import com.roamsys.sepa.pain.jaxb.model.ChargeBearerType1Code;
import com.roamsys.sepa.pain.jaxb.model.CreditTransferTransaction34;
import com.roamsys.sepa.pain.jaxb.model.CustomerCreditTransferInitiationV09;
import com.roamsys.sepa.pain.jaxb.model.DateAndDateTime2Choice;
import com.roamsys.sepa.pain.jaxb.model.DocumentSCTSCTinst;
import com.roamsys.sepa.pain.jaxb.model.FinancialInstitutionIdentification18;
import com.roamsys.sepa.pain.jaxb.model.GenericFinancialIdentification1;
import com.roamsys.sepa.pain.jaxb.model.GroupHeader85;
import com.roamsys.sepa.pain.jaxb.model.ObjectFactory;
import com.roamsys.sepa.pain.jaxb.model.PartyIdentification135;
import com.roamsys.sepa.pain.jaxb.model.PaymentIdentification6;
import com.roamsys.sepa.pain.jaxb.model.PaymentInstruction30;
import com.roamsys.sepa.pain.jaxb.model.PaymentMethod3Code;
import com.roamsys.sepa.pain.jaxb.model.PaymentTypeInformation26;
import com.roamsys.sepa.pain.jaxb.model.RemittanceInformation16;
import com.roamsys.sepa.pain.jaxb.model.ServiceLevel8Choice;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.Writer;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Creates SEPA credit transfer PAIN files (pain.001.001.09).
 *
 * @author AndreasK
 */
public class SepaCreditTransferXmlWriter {

    /**
     * A constant that may be used in some special cases according to the DFÜ agreement
     * of the german banking industry committee, if no information is transferred.
     */
    private static final String NOT_PROVIDED = "NOTPROVIDED";

    private static final String EURO_CURRENCY = "EUR";
    private static final String SCHEMA_LOCATION = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09 pain.001.001.09.xsd";
    private static JAXBContext context = null;

    private static synchronized JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(DocumentSCTSCTinst.class);
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
     * Writes a SEPA credit transfer PAIN file (pain.001.001.09).
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

    private JAXBElement<DocumentSCTSCTinst> createDocument(final SepaCreditTransfer sepaData) {
        final DocumentSCTSCTinst document = new DocumentSCTSCTinst();
        final CustomerCreditTransferInitiationV09 initiation = new CustomerCreditTransferInitiationV09();
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

    private GroupHeader85 createHeader(final SepaCreditTransfer sepaData) {
        final GroupHeader85 header = new GroupHeader85();
        header.setMsgId(sepaData.messageId());
        header.setCreDtTm(OffsetDateTime.now(clock));
        header.setNbOfTxs(Integer.toString(sepaData.getTotalTransactionCount()));
        header.setCtrlSum(sepaData.getTransactionAmountSum());
        header.setInitgPty(createPartyIdentification(sepaData.initiatingPartyName()));
        return header;
    }

    private PartyIdentification135 createPartyIdentification(final String partyName) {
        final PartyIdentification135 paryIdentification = new PartyIdentification135();
        paryIdentification.setNm(partyName);
        return paryIdentification;
    }

    private PaymentInstruction30 createPaymentInstruction(final SepaPaymentInstruction data) {
        final PaymentInstruction30 payment = new PaymentInstruction30();
        payment.setPmtInfId(data.reference());
        payment.setPmtMtd(PaymentMethod3Code.TRF);
        payment.setBtchBookg(data.batchBooking());
        payment.setNbOfTxs(Integer.toString(data.transactions().size()));
        payment.setCtrlSum(data.getTransactionAmountSum());
        payment.setPmtTpInf(createPaymentTypeInfo());
        payment.setReqdExctnDt(createDate(data.requestedExecutionDate()));
        payment.setDbtr(createPartyIdentification(data.debtor().owner()));
        payment.setDbtrAcct(createCashAccount(data.debtor().iban()));
        payment.setDbtrAgt(createFinancialInstitut(data.debtor().bic()));
        payment.setChrgBr(ChargeBearerType1Code.SLEV);
        data.transactions().stream()
                .map(this::createTransactionInfo)
                .forEach(payment.getCdtTrfTxInf()::add);
        return payment;

    }

    private CreditTransferTransaction34 createTransactionInfo(final SepaTransactionInfo data) {
        final CreditTransferTransaction34 transactionInfo = new CreditTransferTransaction34();

        final PaymentIdentification6 value = new PaymentIdentification6();
        value.setEndToEndId(NOT_PROVIDED);
        transactionInfo.setPmtId(value);
        transactionInfo.setAmt(createAmount(data));
        if (data.creditor().bic() != null) {
            transactionInfo.setCdtrAgt(createFinancialInstitut(data.creditor().bic()));
        }
        transactionInfo.setCdtr(createPartyIdentification(data.creditor().owner()));
        transactionInfo.setCdtrAcct(createCashAccount(data.creditor().iban()));
        transactionInfo.setRmtInf(createRemittanceInfo(data.message()));
        return transactionInfo;
    }

    private PaymentTypeInformation26 createPaymentTypeInfo() {
        final PaymentTypeInformation26 info = new PaymentTypeInformation26();
        final ServiceLevel8Choice choice = new ServiceLevel8Choice();
        choice.setCd("SEPA");
        info.getSvcLvl().add(choice);
        return info;
    }

    private DateAndDateTime2Choice createDate(final LocalDate date) {
        final DateAndDateTime2Choice choice = new DateAndDateTime2Choice();
        choice.setDt(date);
        return choice;
    }

    private CashAccount38 createCashAccount(final String iban) {
        final CashAccount38 debtorAccount = new CashAccount38();
        final AccountIdentification4Choice debtorAccountInfo = new AccountIdentification4Choice();
        debtorAccountInfo.setIBAN(iban);
        debtorAccount.setId(debtorAccountInfo);
        return debtorAccount;
    }

    private BranchAndFinancialInstitutionIdentification6 createFinancialInstitut(final String bic) {
        final BranchAndFinancialInstitutionIdentification6 institut = new BranchAndFinancialInstitutionIdentification6();
        final FinancialInstitutionIdentification18 identification = new FinancialInstitutionIdentification18();
        if (bic != null) {
            identification.setBICFI(bic);
        } else {
            final GenericFinancialIdentification1 other = new GenericFinancialIdentification1();
            other.setId(NOT_PROVIDED);
            identification.setOthr(other);
        }
        institut.setFinInstnId(identification);
        return institut;
    }

    private RemittanceInformation16 createRemittanceInfo(final String message) {
        final RemittanceInformation16 remittanceInfo = new RemittanceInformation16();
        remittanceInfo.getUstrd().add(message);
        return remittanceInfo;
    }

    private AmountType4Choice createAmount(final SepaTransactionInfo data) {
        final AmountType4Choice amount = new AmountType4Choice();
        final ActiveOrHistoricCurrencyAndAmount currencyAndAmount = new ActiveOrHistoricCurrencyAndAmount();
        currencyAndAmount.setCcy(EURO_CURRENCY);
        currencyAndAmount.setValue(data.amount());
        amount.setInstdAmt(currencyAndAmount);
        return amount;
    }
}
