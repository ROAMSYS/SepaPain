package com.roamsys.sepa.pain.facade;

/**
 * A bank account.
 *
 * @param owner the name of the owner of the account
 * @param bic the business identifier code (BIC)
 * @param iban the international bank account number (IBAN)
 */
public record SepaBankAccount(String owner, String bic, String iban) {
    public SepaBankAccount {
        if (owner != null) {
            Arguments.requireLength(owner, 0, 140);
        }
        Arguments.requireMatches(bic, "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}");
        Arguments.requireMatches(iban, "[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}");
    }
}
