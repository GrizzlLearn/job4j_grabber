package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection cnn;
    private final static String TABLE_NAME = "post";

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        cnn = DriverManager.getConnection(
                 cfg.getProperty("url"),
                 cfg.getProperty("username"),
                 cfg.getProperty("password")
        );
    }

    /**
     * Метод принимает объект Post и добавляет его в БД
     * @param post объект Post
     */
    @Override
    public void save(Post post) {
        String sql = String.format("INSERT INTO %s(name, text, link, created) VALUES(?, ?, ?, ?) %s %s %s",
                TABLE_NAME,
                "ON CONFLICT ON CONSTRAINT",
                String.format("%s_link_key", TABLE_NAME),
                "DO NOTHING");

        try (PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод возвращает список всех объектов Post из ЬД
     * @return список объектов Post
     */
    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s", TABLE_NAME);
        try (PreparedStatement ps = cnn.prepareStatement(sql); ResultSet resultSet = ps.executeQuery()) {
            while (resultSet.next()) {
                Post post = new Post();
                post.setId(resultSet.getInt(1));
                post.setTitle(resultSet.getString(2));
                post.setDescription(resultSet.getString(3));
                post.setLink(resultSet.getString(4));
                post.setCreated(resultSet.getTimestamp(5).toLocalDateTime().withNano(0));
                result.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Метод принимает id объект и производит его поиск в БД
     * @param id Объекта Post
     * @return найденный объект Post или null, если объект не найден
     */
    @Override
    public Post findById(int id) {
        Post result = null;
        String sql = String.format("SELECT * FROM %s WHERE id = ?", TABLE_NAME);

        try (PreparedStatement ps = cnn.prepareStatement(sql); ) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    result = new Post();
                    result.setId(resultSet.getInt(1));
                    result.setTitle(resultSet.getString(2));
                    result.setDescription(resultSet.getString(3));
                    result.setLink(resultSet.getString(4));
                    result.setCreated(resultSet.getTimestamp(5).toLocalDateTime().withNano(0));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        Properties cfg = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("postgreSQL.properties")) {
            cfg.load(in);
            PsqlStore store = new PsqlStore(cfg);
            Post post = new Post();
            post.setLink("test link");
            post.setTitle("test title");
            post.setDescription("test description");
            post.setCreated(LocalDateTime.now().withNano(0));
            store.save(post);
            System.out.println(store.getAll().toString());
            System.out.println(System.lineSeparator());
            System.out.println(store.findById(1));
            System.out.println(System.lineSeparator());
            System.out.println(store.findById(2));
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
