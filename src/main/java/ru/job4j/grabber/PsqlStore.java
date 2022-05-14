package ru.job4j.grabber;

import ru.job4j.grabber.model.Post;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

  private Connection cnn;

  public PsqlStore(Properties cfg) {
    try {
      Class.forName(cfg.getProperty("db.driver"));
      cnn = DriverManager.getConnection(
              cfg.getProperty("db.url"),
              cfg.getProperty("db.username"),
              cfg.getProperty("db.password")
      );
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void save(Post post) {
    try (PreparedStatement statement =
                 cnn.prepareStatement("INSERT INTO post (title, link, description, created) VALUES (?, ?, ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, post.getTitle());
      statement.setString(2, post.getLink());
      statement.setString(3, post.getDescription());
      statement.setTimestamp(4, Timestamp.valueOf(post.getLocalDateTime()));
      statement.execute();
      try (ResultSet resultSet = statement.getGeneratedKeys()) {
        if (resultSet.next()) {
          post.setId(resultSet.getInt(1));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<Post> getAll() {
    List<Post> posts = new ArrayList<>();
    try (PreparedStatement statement =
                 cnn.prepareStatement("SELECT p.id, p.title, p.link, p.description, p.created FROM post p")) {
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          posts.add(getPostFromRs(resultSet));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return posts;
  }

  @Override
  public Post findById(int id) {
    Post post = null;
    try (PreparedStatement statement =
                 cnn.prepareStatement("SELECT p.id, p.title, p.link, p.description, p.created FROM post p WHERE p.id = ?")) {
      statement.setInt(1, id);
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        post = getPostFromRs(resultSet);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return post;
  }

  private Post getPostFromRs(ResultSet resultSet) throws SQLException {
    return new Post(
            resultSet.getInt("id"),
            resultSet.getString("title"),
            resultSet.getString("link"),
            resultSet.getString("description"),
            resultSet.getTimestamp("created").toLocalDateTime()
    );
  }

  @Override
  public void close() throws Exception {
    if (cnn != null) {
      cnn.close();
    }
  }

  public static void main(String[] args) {
    try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
      Properties properties = new Properties();
      properties.load(in);
      try (PsqlStore store = new PsqlStore(properties)) {
        Post post = new Post("Post1", "/link/123123", "desc", LocalDateTime.now());
        System.out.println("Post before save: \n" + post);

        store.save(post);
        System.out.println("Post after save: \n" + post);

        Post getPost = store.findById(post.getId());
        System.out.println("Post byId: \n" + getPost);

        Post posTwo = new Post("Post2", "/link/3424234234", "desc2", LocalDateTime.now());
        Post posThree = new Post("Post3", "/link/0000", "desc3", LocalDateTime.now());
        store.save(posTwo);
        store.save(posThree);

        List<Post> postList = store.getAll();
        System.out.println("Get all posts");
        postList.forEach(System.out::println);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}