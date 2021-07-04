package me.maxouxax.boulobot.commands;

import com.github.twitch4j.common.enums.CommandPermission;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.interactions.SearchCommand;
import me.maxouxax.boulobot.commands.register.console.ConsoleMigrate;
import me.maxouxax.boulobot.commands.register.discord.*;
import me.maxouxax.boulobot.commands.register.twitch.*;
import me.maxouxax.boulobot.commands.slashannotations.InteractionListener;
import me.maxouxax.boulobot.commands.slashannotations.SimpleInteraction;
import me.maxouxax.boulobot.database.DatabaseManager;
import me.maxouxax.boulobot.music.MusicCommand;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public final class CommandMap {

    private final BOT bot;

    private final Map<String, Long> powers = new HashMap<>();

    private final Map<String, java.util.Date> userIds = new HashMap<>();
    private final List<String> giveawayUsersIds = new ArrayList<>();

    private final Map<String, SimpleCommand> discordCommands = new HashMap<>();
    private final Map<String, SimpleTwitchCommand> twitchCommands = new HashMap<>();
    private final Map<String, SimpleConsoleCommand> consoleCommands = new HashMap<>();
    private final Map<String, SimpleInteraction> interactions = new HashMap<>();
    private final String discordTag = ".";
    private final String twitchTag = "&";

    public CommandMap() {
        this.bot = BOT.getInstance();

        registerCommands(
                new ConsoleMigrate(this),
                new CommandDefault(this),
                new RoleCommand(this),
                new HelpCommand(this),
                new MusicCommand(this),
                new CommandWeather(this),
                new CommandNotif(this),
                //new CommandChangelog(this),
                new CommandVersion(this),
                //new CommandSession(this),
                //new CommandOctogone(this),
                new CommandSay(this),
                new CommandEmbed(this),
                new CommandIgnore(this),
                //new CommandLock(),
                new TwitchWeather(this),
                new TwitchHelp(this),
                new TwitchNotif(this),
                new TwitchVersion(this),
                new TwitchAquoijouer(this),
                new TwitchClipThat(this),
                new TwitchSCP(this),
                new TwitchJeparticipe(this));

        registerInteraction(new SearchCommand(this));

        load();
    }

    private void load() {
        try {
            Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users");

            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                long power = resultSet.getLong("power");
                powers.put(id, power);
            }

            PreparedStatement preparedStatementKnownUsers = connection.prepareStatement("SELECT * FROM known_users");

            final ResultSet resultSetKnownUsers = preparedStatementKnownUsers.executeQuery();

            while (resultSetKnownUsers.next()) {
                int userId = resultSetKnownUsers.getInt("user_id");
                java.util.Date date = resultSetKnownUsers.getDate("updated_at");
                userIds.put(String.valueOf(userId), date);
            }

            PreparedStatement preparedStatementGiveaways = connection.prepareStatement("SELECT * FROM giveaway");

            final ResultSet resultSetGiveaways = preparedStatementGiveaways.executeQuery();

            while (resultSetGiveaways.next()) {
                int userId = resultSetGiveaways.getInt("user_id");
                giveawayUsersIds.add(String.valueOf(userId));
            }
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void savePower(String id, long power) throws SQLException {
        Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
        if (power > 0) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET power = ?, updated_at = ? WHERE id = ?");
            preparedStatement.setLong(1, power);
            preparedStatement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            preparedStatement.setString(3, id);

            final int updateCount = preparedStatement.executeUpdate();

            if (updateCount < 1) {
                PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO users (id, power, updated_at) VALUES (?, ?, ?)");
                insertPreparedStatement.setString(1, id);
                insertPreparedStatement.setLong(2, power);
                insertPreparedStatement.setDate(3, new Date(System.currentTimeMillis()));
                insertPreparedStatement.execute();
            }
        } else {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id = ?");
            preparedStatement.setString(1, id);

            preparedStatement.execute();
        }
        connection.close();
    }

    public MessageEmbed getHelpEmbed(String command) {
        try {
            SimpleCommand command1 = discordCommands.get(command);
            EmbedCrafter embedCrafter = new EmbedCrafter();
            embedCrafter.setTitle("Aide » " + discordTag + command)
                    .setDescription(command1.getDescription())
                    .addField("Utilisation:", discordTag + command1.getHelp(), true)
                    .addField("Exemple:", discordTag + command1.getExemple(), true);
            return embedCrafter.build();
        } catch (Exception e) {
            bot.getErrorHandler().handleException(e);
        }
        return new EmbedBuilder().build();
    }

    public String getTwitchHelpString(String command) {
        try {
            SimpleTwitchCommand command1 = twitchCommands.get(command);
            return "Aide » " + twitchTag + command + " | " + command1.getDescription() + " | Utilisation:" + twitchTag + command1.getHelp() + " | Exemple: " + twitchTag + command1.getExemple();
        } catch (Exception e) {
            bot.getErrorHandler().handleException(e);
        }
        return "";
    }

    public void addKnownUser(String id) {
        if (!userIds.containsKey(id)) {
            try {
                userIds.put(id, new java.util.Date());
                Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
                PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO known_users (user_id, updated_at) VALUES (?, ?)");
                insertPreparedStatement.setInt(1, Integer.parseInt(id));
                insertPreparedStatement.setDate(2, new Date(System.currentTimeMillis()));
                insertPreparedStatement.execute();
                connection.close();
            } catch (SQLException e) {
                bot.getErrorHandler().handleException(e);
            }
        }
    }

    public boolean isKnown(String id) {
        return userIds.containsKey(id);
    }

    public void addGiveawayUser(String id) {
        if (!giveawayUsersIds.contains(id)) {
            try {
                giveawayUsersIds.add(id);
                Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
                PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO giveaway (user_id, updated_at) VALUES (?, ?)");
                insertPreparedStatement.setInt(1, Integer.parseInt(id));
                insertPreparedStatement.setDate(2, new Date(System.currentTimeMillis()));
                insertPreparedStatement.execute();
                connection.close();
            } catch (SQLException e) {
                bot.getErrorHandler().handleException(e);
            }
        }
    }

    public boolean isGiveawayKnown(String id) {
        return giveawayUsersIds.contains(id);
    }

    public void setUserPower(User user, long power) {
        if (power == 0) {
            powers.remove(user.getId());
        } else {
            powers.put(user.getId(), power);
        }
        try {
            savePower(user.getId(), power);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public long getPowerUser(Guild guild, User user) {
        if (guild.getMember(user).hasPermission(Permission.ADMINISTRATOR)) return 150;
        return powers.getOrDefault(user.getId(), 0L);
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public String getTwitchTag() {
        return twitchTag;
    }

    public Collection<SimpleCommand> getDiscordCommands() {
        return discordCommands.values();
    }

    public Collection<SimpleConsoleCommand> getConsoleCommands() {
        return consoleCommands.values();
    }

    public Collection<SimpleTwitchCommand> getTwitchCommands() {
        return twitchCommands.values();
    }

    public void registerCommands(Object... objects) {
        for (Object object : objects) {
            registerCommand(object);
        }
    }

    public void registerCommand(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                Command command = method.getAnnotation(Command.class);
                method.setAccessible(true);
                SimpleCommand simpleCommand = new SimpleCommand(command.name(), command.description(), command.help(), command.example(), object, method, command.power(), command.guildOnly());
                discordCommands.put(command.name(), simpleCommand);
            } else if (method.isAnnotationPresent(TwitchCommand.class)) {
                TwitchCommand command = method.getAnnotation(TwitchCommand.class);
                method.setAccessible(true);
                SimpleTwitchCommand simpleTwitchCommand = new SimpleTwitchCommand(command.name(), command.description(), command.help(), command.example(), command.rank(), object, method);
                twitchCommands.put(command.name(), simpleTwitchCommand);
            } else if (method.isAnnotationPresent(ConsoleCommand.class)) {
                ConsoleCommand command = method.getAnnotation(ConsoleCommand.class);
                method.setAccessible(true);
                SimpleConsoleCommand simpleConsoleCommand = new SimpleConsoleCommand(command.name(), command.description(), command.help(), object, method);
                consoleCommands.put(command.name(), simpleConsoleCommand);
            }
        }
    }

    public void registerInteraction(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(InteractionListener.class)) {
                InteractionListener interactionListener = method.getAnnotation(InteractionListener.class);
                method.setAccessible(true);
                SimpleInteraction simpleInteraction = new SimpleInteraction(interactionListener.id(), object, method);
                interactions.put(interactionListener.id(), simpleInteraction);
            }
        }
    }

    private Object[] getConsoleCommand(String command) {
        String[] commandSplit = command.split(" ");
        String[] args = new String[commandSplit.length - 1];
        for (int i = 1; i < commandSplit.length; i++) args[i - 1] = commandSplit[i];
        SimpleConsoleCommand simpleConsoleCommand = consoleCommands.get(commandSplit[0]);
        return new Object[]{simpleConsoleCommand, args};
    }

    public void consoleCommand(String command) {
        SimpleConsoleCommand simpleConsoleCommand = (SimpleConsoleCommand) getConsoleCommand(command)[0];

        try {
            executeConsoleCommand(simpleConsoleCommand, (String[]) getConsoleCommand(command)[1]);
        } catch (Exception e) {
            bot.getLogger().log(Level.SEVERE, "La methode " + simpleConsoleCommand.getMethod().getName() + " n'est pas correctement initialisé. (" + e.getMessage() + ")");
            bot.getErrorHandler().handleException(e);
        }
    }

    private void executeConsoleCommand(SimpleConsoleCommand simpleConsoleCommand, String[] args) throws Exception {
        Parameter[] parameters = simpleConsoleCommand.getMethod().getParameters();
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == String[].class) objects[i] = args;
            else if (parameters[i].getType() == String.class) objects[i] = simpleConsoleCommand.getName();
            else if (parameters[i].getType() == JDA.class) objects[i] = bot.getJda();
            else if (parameters[i].getType() == SimpleCommand.class) objects[i] = simpleConsoleCommand;
        }
        simpleConsoleCommand.getMethod().invoke(simpleConsoleCommand.getObject(), objects);
    }

    public void discordCommandUser(String command, SlashCommandEvent slashCommandEvent) {
        SimpleCommand simpleCommand = (SimpleCommand) getDiscordCommand(command)[0];

        if (simpleCommand.isGuildOnly() && !slashCommandEvent.isFromGuild() || simpleCommand.isGuildOnly() && slashCommandEvent.isFromGuild() && simpleCommand.getPower() > getPowerUser(slashCommandEvent.getGuild(), slashCommandEvent.getUser())) {
            slashCommandEvent.reply("Vous ne pouvez pas utiliser cette commande.").setEphemeral(true).queue();
            return;
        }

        try {
            executeDiscordCommand(simpleCommand, slashCommandEvent.getOptions(), slashCommandEvent);
        } catch (Exception e) {
            bot.getLogger().log(Level.SEVERE, "La methode " + simpleCommand.getMethod().getName() + " n'est pas correctement initialisé. (" + e.getMessage() + ")");
            bot.getErrorHandler().handleException(e);
        }
    }

    private Object[] getDiscordCommand(String command) {
        String[] commandSplit = command.split(" ");
        String[] args = new String[commandSplit.length - 1];
        for (int i = 1; i < commandSplit.length; i++) args[i - 1] = commandSplit[i];
        SimpleCommand simpleCommand = discordCommands.get(commandSplit[0]);
        return new Object[]{simpleCommand, args};
    }

    public SimpleCommand getDiscordSimpleCommand(String command) {
        return discordCommands.get(command);
    }

    private void executeDiscordCommand(SimpleCommand simpleCommand, List<OptionMapping> args, SlashCommandEvent slashCommandEvent) throws Exception {
        Parameter[] parameters = simpleCommand.getMethod().getParameters();
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == List[].class) objects[i] = args;
            else if (parameters[i].getType() == User.class) objects[i] = slashCommandEvent.getUser();
            else if (parameters[i].getType() == Member.class) objects[i] = slashCommandEvent.getMember();
            else if (parameters[i].getType() == TextChannel.class) objects[i] = slashCommandEvent.getTextChannel();
            else if (parameters[i].getType() == PrivateChannel.class)objects[i] = slashCommandEvent.getPrivateChannel();
            else if (parameters[i].getType() == Guild.class) objects[i] = slashCommandEvent.getGuild();
            else if (parameters[i].getType() == String.class) objects[i] = slashCommandEvent.getName();
            else if (parameters[i].getType() == SlashCommandEvent.class) objects[i] = slashCommandEvent;
            else if (parameters[i].getType() == JDA.class) objects[i] = bot.getJda();
            else if (parameters[i].getType() == SimpleCommand.class) objects[i] = simpleCommand;
        }
        simpleCommand.getMethod().invoke(simpleCommand.getObject(), objects);
    }

    public boolean twitchCommandUser(com.github.twitch4j.helix.domain.User user, String broadcaster, String broadcasterId, TwitchCommand.ExecutorRank executorRank, String command, Set<CommandPermission> commandPermissions) {
        Object[] object = getTwitchCommand(command);
        if (object[0] == null || ((SimpleTwitchCommand) object[0]).getExecutorRank().getPower() > executorRank.getPower())
            return false;
        try {
            executeTwitchCommand(((SimpleTwitchCommand) object[0]), broadcaster, broadcasterId, user, (String[]) object[1], commandPermissions);
        } catch (Exception e) {
            bot.getLogger().log(Level.SEVERE, "La methode " + ((SimpleTwitchCommand) object[0]).getMethod().getName() + " n'est pas correctement initialisé.");
            bot.getErrorHandler().handleException(e);
        }
        return true;
    }

    private Object[] getTwitchCommand(String command) {
        String[] commandSplit = command.split(" ");
        String[] args = new String[commandSplit.length - 1];
        for (int i = 1; i < commandSplit.length; i++) args[i - 1] = commandSplit[i];
        SimpleTwitchCommand simpleTwitchCommand = twitchCommands.get(commandSplit[0]);
        return new Object[]{simpleTwitchCommand, args};
    }

    public SimpleTwitchCommand getTwitchSimpleCommand(String command) {
        return twitchCommands.get(command);
    }

    private void executeTwitchCommand(SimpleTwitchCommand simpleTwitchCommand, String broadcaster, String broadcasterId, com.github.twitch4j.helix.domain.User user, String[] args, Set<CommandPermission> commandPermissions) throws Exception {
        Parameter[] parameters = simpleTwitchCommand.getMethod().getParameters();
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == String[].class) objects[i] = args;
            else if (parameters[i].getType() == com.github.twitch4j.helix.domain.User.class) objects[i] = user;
            else if (parameters[i].getType() == String.class) objects[i] = broadcaster;
            else if (parameters[i].getType() == Long.class) objects[i] = Long.valueOf(broadcasterId);
            else if (parameters[i].getType() == SimpleTwitchCommand.class) objects[i] = simpleTwitchCommand;
            else if (parameters[i].getType() == Set.class) objects[i] = commandPermissions;
        }
        simpleTwitchCommand.getMethod().invoke(simpleTwitchCommand.getObject(), objects);
    }

    public Map<String, java.util.Date> getUserIds() {
        return userIds;
    }

    public List<String> getGiveawayUsersIds() {
        return giveawayUsersIds;
    }

    public void clearGiveaway() {
        try {
            giveawayUsersIds.clear();
            Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
            PreparedStatement insertPreparedStatement = connection.prepareStatement("TRUNCATE TABLE known_users");
            insertPreparedStatement.execute();
            connection.close();
        } catch (SQLException e) {
            bot.getErrorHandler().handleException(e);
        }
    }

    public TwitchCommand.ExecutorRank getRank(Set<CommandPermission> permissions) {
        if (permissions.contains(CommandPermission.BROADCASTER)) return TwitchCommand.ExecutorRank.OWNER;
        if (permissions.contains(CommandPermission.MODERATOR)) return TwitchCommand.ExecutorRank.MOD;
        if (permissions.contains(CommandPermission.VIP)) return TwitchCommand.ExecutorRank.VIP;
        if (permissions.contains(CommandPermission.SUBSCRIBER)) return TwitchCommand.ExecutorRank.SUBSCRIBER;
        return TwitchCommand.ExecutorRank.EVERYONE;
    }

    public void updateCommands() {
        List<CommandData> commands = new ArrayList<>();
        discordCommands.forEach((s, simpleCommand) -> {
            CommandData commandData = new CommandData(simpleCommand.getName(), simpleCommand.getDescription());
            if (simpleCommand.getOptionsData().length != 0) commandData.addOptions(simpleCommand.getOptionsData());
            if (simpleCommand.getSubcommandsData().length != 0)
                commandData.addSubcommands(simpleCommand.getSubcommandsData());
            if (simpleCommand.getSubcommandsGroups().length != 0)
                commandData.addSubcommandGroups(simpleCommand.getSubcommandsGroups());

            commands.add(commandData);
        });
        bot.getJda().updateCommands().addCommands(commands).queue();
    }

    public void discordInteraction(String id, ButtonClickEvent buttonClickEvent) {
        try {
            SimpleInteraction simpleInteraction = interactions.get(id);
            Parameter[] parameters = simpleInteraction.getMethod().getParameters();
            Object[] objects = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getType() == User.class) objects[i] = buttonClickEvent.getUser();
                else if (parameters[i].getType() == TextChannel.class) objects[i] = buttonClickEvent.getTextChannel();
                else if (parameters[i].getType() == PrivateChannel.class)objects[i] = buttonClickEvent.getPrivateChannel();
                else if (parameters[i].getType() == Guild.class) objects[i] = buttonClickEvent.getGuild();
                else if (parameters[i].getType() == ButtonClickEvent.class) objects[i] = buttonClickEvent;
                else if (parameters[i].getType() == JDA.class) objects[i] = bot.getJda();
                else if (parameters[i].getType() == SimpleInteraction.class) objects[i] = simpleInteraction;
            }
            simpleInteraction.getMethod().invoke(simpleInteraction.getObject(), objects);
        } catch (Exception e) {
            bot.getErrorHandler().handleException(e);
        }
    }

}