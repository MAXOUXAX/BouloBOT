package me.maxouxax.boulobot.commands.register.console;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.ConsoleCommand;
import me.maxouxax.boulobot.database.DatabaseManager;
import me.maxouxax.boulobot.roles.Grade;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ConsoleMigrate {

    private final BOT bot;
    private final CommandMap commandMap;

    public ConsoleMigrate(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @ConsoleCommand(name = "migrate", help = "migrate", description = "Permet de migrer les données stockées en fichier json sur la base de données")
    public void migrate(String[] args) throws SQLException {
        if (args.length == 0) {
            bot.getLogger().log(Level.WARNING, "Vous devez spécifier au moins un argument\nknown_users ou roles");
        } else {
            String arg1 = args[0];
            if (arg1.equalsIgnoreCase("known_users")) {
                Map<String, java.util.Date> knownUsers = commandMap.getUserIds();
                Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
                AtomicInteger success = new AtomicInteger();
                bot.getLogger().log(Level.WARNING, "Migration de l'ensemble des known_users... (" + knownUsers.size() + ")");
                knownUsers.forEach((s, s2) -> {
                    try {
                        PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO known_users (user_id, updated_at) VALUES (?, ?)");
                        insertPreparedStatement.setInt(1, Integer.parseInt(s));
                        insertPreparedStatement.setDate(2, new Date(s2.getTime()));
                        success.addAndGet(insertPreparedStatement.executeUpdate());
                    } catch (SQLException e) {
                        bot.getErrorHandler().handleException(e);
                    }
                });
                connection.close();
                bot.getLogger().log(Level.WARNING, "Migration terminée, connexion à la base de données fermée!");
                bot.getLogger().log(Level.INFO, "Récapitulatif...\n(" + success.get() + "/" + knownUsers.size() + ")");
            } else if (arg1.equalsIgnoreCase("roles")) {
                Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
                AtomicInteger success = new AtomicInteger();
                List<Grade> grades = bot.getRolesManager().getGrades();
                bot.getLogger().log(Level.WARNING, "Migration de l'ensemble des roles... (" + grades.size() + ")");
                grades.forEach(grade -> {
                    try {
                        PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO roles (displayname, description, id, emote_id) VALUES (?, ?, ?, ?)");
                        insertPreparedStatement.setString(1, grade.getDisplayName());
                        insertPreparedStatement.setString(2, grade.getDescription());
                        insertPreparedStatement.setString(3, grade.getRole().getId());
                        insertPreparedStatement.setString(4, grade.getEmoteId());
                        success.addAndGet(insertPreparedStatement.executeUpdate());
                    } catch (SQLException e) {
                        bot.getErrorHandler().handleException(e);
                    }
                });
                connection.close();
                bot.getLogger().log(Level.WARNING, "Migration terminée, connexion à la base de données fermée!");
                bot.getLogger().log(Level.INFO, "Récapitulatif...\n(" + success.get() + "/" + grades.size() + ")");
            }
        }
    }
}
