package com.roamsys.sepa.pain.facade;

/**
 * Indicates that the XML file creation failed.
 */
public class SepaCreditTransferXmllWriterException extends RuntimeException {

    public SepaCreditTransferXmllWriterException(final Exception cause) {
        super(cause);
    }
}
