package com.roamsys.sepa.pain.jaxb.converter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts between {@link OffsetDateTime} and <code>xs:dateTime</code>.
 */
public class OffsetDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {

    @Override
    public OffsetDateTime unmarshal(final String dateTime) {
        return dateTime != null ? DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(dateTime, OffsetDateTime::from) : null;
    }

    @Override
    public String marshal(final OffsetDateTime dateTime) {
        return dateTime != null ? DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime) : null;
    }
}