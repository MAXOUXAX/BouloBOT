package me.maxouxax.boulobot.util;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    private final Map<String, String> configKeys = new HashMap<>();

    public ConfigurationManager() {
        try {
            loadData();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void loadData() throws SQLException {
        Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM settings");

        final ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String key = resultSet.getString("name");
            String value = resultSet.getString("value");
            configKeys.put(key, value);
        }
        connection.close();
    }

    public void setValue(String key, String value) {
        try {
            configKeys.put(key, value);
            Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE settings SET value = ? WHERE name = ?");
            preparedStatement.setString(1, value);
            preparedStatement.setString(2, key);

            final int updateCount = preparedStatement.executeUpdate();

            if (updateCount < 1) {
                PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO settings (name, value) VALUES (?, ?)");
                insertPreparedStatement.setString(1, value);
                insertPreparedStatement.setString(2, key);
                insertPreparedStatement.execute();
            }
            connection.close();
        } catch (SQLException e) {
            BOT.getInstance().getErrorHandler().handleException(e);
        }
    }

    public String getStringValue(String key) {
        return configKeys.getOrDefault(key, "");
    }

    public Long getLongValue(String key) {
        return Long.valueOf(configKeys.getOrDefault(key, "0"));
    }

}
