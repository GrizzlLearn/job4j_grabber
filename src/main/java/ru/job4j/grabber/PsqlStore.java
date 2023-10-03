package ru.job4j.grabber;

import java.sql.*;
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
     *
     * @param post объект Post
     * @see Post
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
     * Метод возвращает список всех объектов Post из БД
     *
     * @return список объектов Post
     * @see List
     * @see Post
     */
    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s", TABLE_NAME);
        try (PreparedStatement ps = cnn.prepareStatement(sql); ResultSet resultSet = ps.executeQuery()) {
            if (resultSet.isBeforeFirst()) {
                result.addAll(createPostObjects(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Метод принимает объект ResultSet и из его данных создаёт объект Post и добавляет его список.
     *
     * @param resultSet объект ResultSet
     * @return список объектов Post
     * @throws SQLException если тип данных в ResultSet не может быть преобразован в запрашиваемый
     * @see ResultSet
     * @see List
     * @see Post
     */
    private List<Post> createPostObjects(ResultSet resultSet) throws SQLException {
        List<Post> result = new ArrayList<>();
        while (resultSet.next()) {
            Post tmp = new Post();
            tmp.setId(resultSet.getInt(1));
            tmp.setTitle(resultSet.getString(2));
            tmp.setDescription(resultSet.getString(3));
            tmp.setLink(resultSet.getString(4));
            tmp.setCreated(resultSet.getTimestamp(5).toLocalDateTime().withNano(0));
            result.add(tmp);
        }

        return result;
    }

    /**
     * Метод принимает id объект и производит его поиск в БД
     *
     * @param id Объекта Post
     * @return найденный объект Post или null, если объект не найден
     * @see Post
     */
    @Override
    public Post findById(int id) {
        Post result = null;
        String sql = String.format("SELECT * FROM %s WHERE id = ?", TABLE_NAME);

        try (PreparedStatement ps = cnn.prepareStatement(sql); ) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    result = createPostObjects(resultSet).get(0);
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

}
