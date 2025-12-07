package com.leawsic.autoupdate.data;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

public class TimeFormatter {
    private static final DateTimeFormatter LOCAL_TIME=new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral('.')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral('.')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();
    public static final DateTimeFormatter LOCAL_DATE_TIME=new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('_')
            .append(LOCAL_TIME)
            .toFormatter();
}
