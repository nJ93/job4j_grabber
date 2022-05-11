package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

  private static final String SOURCE_LINK = "https://career.habr.com";

  private final DateTimeParser dateTimeParser;

  public HabrCareerParse(DateTimeParser dateTimeParser) {
    this.dateTimeParser = dateTimeParser;
  }

  @Override
  public List<Post> list(String link) {
    List<Post> postList = new ArrayList<>();
    try {
      Connection connection = Jsoup.connect(link);
      Document document = connection.get();
      Elements rows = document.select(".vacancy-card__inner");
      rows.forEach(row -> {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element dateElement = row.select(".vacancy-card__date").first();
        Element linkElement = titleElement.child(0);
        Element dateInnerElement = dateElement.child(0);
        String titleName = titleElement.text();
        String dateStr = dateInnerElement.attr("datetime");
        String linkStr = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String postInfo = retrieveDescription(linkStr);
        postList.add(new Post(titleName, linkStr, postInfo, dateTimeParser.parse(dateStr)));
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
    return postList;
  }

  private String retrieveDescription(String link) {
    String result = null;
    try {
      Connection connection = Jsoup.connect(link);
      Document document = connection.get();
      Element elem = document.select(".job_show_description__vacancy_description").first();
      result = elem.text();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
}