package ru.job4j.grabber.utils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {
    @Override
    public LocalDateTime parse(String parse) throws ParseException {
        return LocalDateTime.parse(parse, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
