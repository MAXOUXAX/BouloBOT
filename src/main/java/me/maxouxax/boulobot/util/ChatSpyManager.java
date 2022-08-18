package me.maxouxax.boulobot.util;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.database.DatabaseManager;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ChatSpyManager {

    private final BOT bot;
    private final ArrayList<String> stringsToPost = new ArrayList<>();
    private final ArrayList<String> ignoredUsers = new ArrayList<>();
    private final ScheduledFuture scheduleChatSpy;

    public ChatSpyManager() {
        this.bot = BOT.getInstance();
        this.scheduleChatSpy = bot.getScheduler().scheduleAtFixedRate(this::postMessages, 30, 30, TimeUnit.SECONDS);
        try {
            loadData();
        } catch (SQLException e) {
            bot.getErrorHandler().handleException(e);
        }
    }

    public void loadData() throws SQLException {
        Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ignored_users");

        final ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String username = resultSet.getString("username");
            ignoredUsers.add(username);
        }
        connection.close();
    }

    public void editIgnoredUsers(String username, boolean add) {
        try {
            Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
            if (add) {
                ignoredUsers.add(username);
                PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO ignored_users (username) VALUES (?)");
                insertPreparedStatement.setString(1, username);
                insertPreparedStatement.execute();
            } else {
                ignoredUsers.remove(username);
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ignored_users WHERE username = ?");
                preparedStatement.setString(1, username);
                preparedStatement.execute();
            }
            connection.close();
        } catch (SQLException e) {
            BOT.getInstance().getErrorHandler().handleException(e);
        }
    }

    private void postMessages() {
        if (!stringsToPost.isEmpty()) {
            ArrayList<StringBuilder> stringBuilderArrayList = new ArrayList<>();
            stringBuilderArrayList.add(new StringBuilder());
            AtomicReference<Integer> listIndex = new AtomicReference<>(0);
            stringsToPost.forEach(s -> {
                if (stringBuilderArrayList.get(listIndex.get()).length() + s.length() > 2000) {
                    stringBuilderArrayList.add(new StringBuilder());
                    listIndex.set(listIndex.get() + 1);
                }
                stringBuilderArrayList.get(listIndex.get()).append(s).append("\n");
            });

            stringBuilderArrayList.forEach(stringBuilder -> {
                TextChannel channel = Objects.requireNonNull(bot.getJda().getGuildById(bot.getConfigurationManager().getStringValue("guildId"))).getTextChannelById(bot.getConfigurationManager().getStringValue("chatSpyTextChannelId"));
                if (channel != null) {
                    channel.sendMessage(stringBuilder.toString()).queue();
                }
            });
        }
        stringsToPost.clear();
    }

    public void stopSpying() {
        scheduleChatSpy.cancel(false);
    }

    public void addMessage(String userName, String string) {
        if (!isIgnored(userName)) {
            String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
            stringsToPost.add("`" + date + "` • " + string);
        }
    }

    public boolean isIgnored(String userName) {
        for (String s : ignoredUsers) {
            if (s.equalsIgnoreCase(userName)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getIgnoredUsers() {
        return ignoredUsers;
    }
}
