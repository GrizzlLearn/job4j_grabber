package ru.job4j.grabber.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class HabrCareerDateTimeParser implements DateTimeParser{
    @Override
    public LocalDateTime parse(String parse) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        return null;
    }
}
