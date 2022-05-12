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
      for (int i = 1; i <= 5; i++) {
        Connection connection = Jsoup.connect(link + i);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> postList.add(retrievePost(row)));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return postList;
  }

  private Post retrievePost(Element postElement) {
    Element titleElement = postElement.select(".vacancy-card__title").first();
    Element dateElement = postElement.select(".vacancy-card__date").first().child(0);
    Element linkElement = titleElement.child(0);
    String titleName = titleElement.text();
    String dateStr = dateElement.attr("datetime");
    String linkStr = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
    String postInfo = retrieveDescription(linkStr);
    return new Post(titleName, linkStr, postInfo, dateTimeParser.parse(dateStr));
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