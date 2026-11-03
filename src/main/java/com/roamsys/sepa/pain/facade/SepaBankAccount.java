package com.roamsys.sepa.pain.facade;

/**
 * A bank account.
 *
 * @param owner the name of the owner of the account
 * @param bic the optional business identifier code (BIC)
 * @param iban the international bank account number (IBAN)
 */
public record SepaBankAccount(String owner, String bic, String iban) {

    public static final String BIC_REGEX = "[A-Z0-9]{4,4}[A-Z]{2,2}[A-Z0-9]{2,2}([A-Z0-9]{3,3}){0,1}";
    public static final String IBAN_REGEX = "[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}";
    /**
     * Allowed characters for SEPA identifiers like the message ID or the payment information ID (1-35 characters).
     * The '|' is allowed because the GBIC XSD lists it within the character class.
     */
    public static final String SEPA_ID_REGEX = "[A-Za-z0-9+?/\\-:().,'| ]{1,35}";

    public SepaBankAccount {
        Arguments.requireLength(owner, 1, 70);
        if (bic != null) {
            Arguments.requireMatches(bic, BIC_REGEX);
        }
        Arguments.requireMatches(iban, IBAN_REGEX);
    }
}
