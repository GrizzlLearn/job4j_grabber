package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    @Test
    void testOne() throws ParseException {
        HabrCareerDateTimeParser hcdtp = new HabrCareerDateTimeParser();
        String timeBefore = "2023-09-21T22:12:19+03:00";
        String timeAfter = "2023-09-21T22:12:19";
        assertThat(hcdtp.parse(timeBefore).toString()).isEqualTo(timeAfter);
    }

    @Test
    void testTwo() throws ParseException {
        HabrCareerDateTimeParser hcdtp = new HabrCareerDateTimeParser();
        String timeBefore = "2023-09-21T12:54:33+03:00";
        String timeAfter = "2023-09-21T12:54:33";
        assertThat(hcdtp.parse(timeBefore).toString()).isEqualTo(timeAfter);
    }

    @Test
    void testThree() throws ParseException {
        HabrCareerDateTimeParser hcdtp = new HabrCareerDateTimeParser();
        String timeBefore = "2023-09-21T15:30:26+03:00";
        String timeAfter = "2023-09-21T15:30:26";
        assertThat(hcdtp.parse(timeBefore).toString()).isEqualTo(timeAfter);
    }

    @Test
    void testFour() throws ParseException {
        HabrCareerDateTimeParser hcdtp = new HabrCareerDateTimeParser();
        String timeBefore = "2023-09-21T14:42:29+03:00";
        String timeAfter = "2023-09-21T14:42:29";
        assertThat(hcdtp.parse(timeBefore).toString()).isEqualTo(timeAfter);
    }
}
