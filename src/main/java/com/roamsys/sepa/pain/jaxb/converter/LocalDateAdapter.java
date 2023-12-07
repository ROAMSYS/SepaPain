package com.roamsys.sepa.pain.jaxb.converter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Converts between {@link LocalDate} and <code>xs:date</code>.
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public LocalDate unmarshal(final String inputDate) {
        return inputDate != null ? DateTimeFormatter.ISO_DATE.parse(inputDate, LocalDate::from) : null;
    }

    @Override
    public String marshal(final LocalDate inputDate) {
        return inputDate != null ? DateTimeFormatter.ISO_DATE.format(inputDate) : null;
    }
}