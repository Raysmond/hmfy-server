package com.shield.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtils {
    public static ZonedDateTime today() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault());
    }

    public static ZonedDateTime tomorrow() {
        return today().plusDays(1);
    }

    public static ZonedDateTime yesterday() {
        return today().minusDays(1);
    }
}
