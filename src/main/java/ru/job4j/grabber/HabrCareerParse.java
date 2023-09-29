package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String PAGE_NUMBER = String.format(PAGE_LINK + "%s", "?page=");

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(String.format(PAGE_NUMBER + "%s", i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");

            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element fullDateElement = dateElement.child(0);
                String dateTimeString = fullDateElement.attr("datetime");
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                HabrCareerDateTimeParser hcdtp = new HabrCareerDateTimeParser();

                try {
                    LocalDateTime dateTimeParser = hcdtp.parse(dateTimeString);
                    System.out.printf("%s %s %s%n", vacancyName, link, dateTimeParser);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    System.out.println(retrieveDescription(link));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements body = document.select(".vacancy-description__text");
        return body.text();
    }
}
