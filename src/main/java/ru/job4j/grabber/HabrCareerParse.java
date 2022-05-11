package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

  private static final String SOURCE_LINK = "https://career.habr.com";

  private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

  public static void main(String[] args) throws IOException {
    for (int i = 1; i <= 5; i++) {
      Connection connection = Jsoup.connect(PAGE_LINK + i);
      Document document = connection.get();
      Elements rows = document.select(".vacancy-card__inner");
      int pageNum = i;
      rows.forEach(row -> {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element dateElement = row.select(".vacancy-card__date").first();
        Element linkElement = titleElement.child(0);
        Element dateInnerElement = dateElement.child(0);
        String vacancyName = titleElement.text();
        String dateStr = dateInnerElement.attr("datetime");
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String postInfo = retrieveDescription(link);
        System.out.printf("Page - %s | Date - %s | %s; Info - %s | %s%n", pageNum, dateStr, vacancyName, postInfo, link);
      });
    }
  }

  private static String retrieveDescription(String link) {
    String result = null;
    try {
      Connection connection = Jsoup.connect(link);
      Document document = connection.get();
      Element elem = document.select(".job_show_description__vacancy_description").first();
      result = elem.text();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }
}