package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HabrCareerParse implements Parse {
    private final DateTimeParser dateTimeParser;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String PAGE_NUMBER = String.format(PAGE_LINK + "%s", "?page=");

    HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    /*public static void main(String[] args) throws IOException {
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
    }*/


    public static void main(String[] args) throws IOException {
        HabrCareerParse hcp = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> result = new ArrayList<>();
        List<String> linkResult = new ArrayList<>();
        getVacancyLinks();

        for (Post post : result) {
            System.out.println(post.toString());
        }
    }

    private static List<String> getVacancyLinks() {
        List<String> result = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String sourceLink = String.format(PAGE_NUMBER + "%s", i);
            Connection connection = Jsoup.connect(sourceLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");

            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                result.add(link);
            });
        }
        return result;
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements body = document.select(".vacancy-description__text");
        return body.text();
    }

    private static List<Post> postBuilder() throws IOException {
        List<Post> result = new ArrayList<>();
        List<String> tmp = getVacancyLinks();
        for (String link : tmp) {
            Post post = new Post();
            post.setLink(link);
            post.setDescription(retrieveDescription(link));
            result.add(post);
        }

        return result;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        try {
            //result.add(postBuilder(link));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
