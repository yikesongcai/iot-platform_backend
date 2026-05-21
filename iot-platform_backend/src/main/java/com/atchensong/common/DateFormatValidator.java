package com.atchensong.common;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DateFormatValidator {
    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    public static boolean isValidDateFormat(String input) {
        return Pattern.matches(DATE_PATTERN, input);
    }
}
