package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static DateTimeParser dateTimeParser;

    private static final String SOURCE_LINK = "https://career.habr.com";

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    /**
     * Метод принимает ссылку на страницу с вакансиями, проходится по DOM дереву и собирает ссылки на отдельные вакансии
     *
     * @param link ссылка на общий список вакансий
     * @return список ссылок на отдельные вакансии
     * @throws IOException если произошла ошибка получение DOM дерева
     */

    private static List<String> getVacancyLinks(String link) throws IOException {
        List<String> result = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");

        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            result.add(vacLink);
        });

        return result;
    }

    /**
     * Метод принимает ссылку на конкретную вакансию, проходится по DOM дереву и собирает данные,
     * которые помещаются в список
     *
     * @param vacLink ссылка на вакансию
     * @return список содержащий заголовок, описание и время публикации вакансии
     * @throws IOException если произошла ошибка получение DOM дерева
     */

    private static List<String> retrieveVacancy(String vacLink) throws IOException {
        List<String> result = new ArrayList<>();
        Connection connection = Jsoup.connect(vacLink);
        Document document = connection.get();
        Elements title = document.select(".page-title__title");
        Elements description = document.select(".vacancy-description__text");
        Element dateElement = document.select(".vacancy-header__date").first();
        Element fullDateElement = dateElement.child(0).child(0);
        String dateTimeString = fullDateElement.attr("datetime");
        result.add(title.text());
        result.add(description.text());
        result.add(dateTimeString);
        return result;
    }

    /**
     * Метод принимает ссылку на конкретную вакансию и, с помощью метода retrieveVacancy, формирует объект Post.
     *
     * @param vacLink ссылка на вакансию
     * @return объект типа Post
     * @throws IOException если произошла ошибка получение DOM дерева
     * @throws ParseException если произошла ошибка парсинга даты
     * @see HabrCareerParse#retrieveVacancy(String)
     * @see HabrCareerParse#dateTimeParser
     * @see Post
     */
    
    private static Post postBuilder(String vacLink) throws IOException, ParseException {
        Post result = new Post();
        List<String> fullDesc = retrieveVacancy(vacLink);
        result.setLink(vacLink);
        result.setTitle(fullDesc.get(0));
        result.setDescription(fullDesc.get(1));
        result.setCreated(dateTimeParser.parse(fullDesc.get(2)));
        return result;
    }

    /**
     * Метод принимает ссылку на список вакансий и с помощью вспомогательных методов формирует список объектов Post
     *
     * @param link ссылка на страницу со списком вакансий
     * @return список объектов типа Post, сформированных из списка вакансий
     * @see HabrCareerParse#getVacancyLinks(String)
     * @see HabrCareerParse#postBuilder(String)
     * @see Post
     */

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        List<String> allLinks;
        try {
            allLinks = new ArrayList<>(getVacancyLinks(link));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String vacLink : allLinks) {
            try {
                result.add(postBuilder(vacLink));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
