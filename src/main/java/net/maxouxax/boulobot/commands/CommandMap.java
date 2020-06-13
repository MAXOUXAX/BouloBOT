package net.maxouxax.boulobot.commands;

import com.github.twitch4j.common.enums.CommandPermission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.register.discord.*;
import net.maxouxax.boulobot.commands.register.twitch.*;
import net.maxouxax.boulobot.music.MusicCommand;
import net.maxouxax.boulobot.util.JSONReader;
import net.maxouxax.boulobot.util.JSONWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public final class CommandMap {

    private final BOT bot;

    private final Map<Long, Integer> powers = new HashMap<>();

    private final List<String> userIds = new ArrayList<>();
    private final List<String> giveawayUsersIds = new ArrayList<>();

    private final Map<String, SimpleCommand> discordCommands = new HashMap<>();
    private final Map<String, SimpleTwitchCommand> twitchCommands = new HashMap<>();
    private final String discordTag = ".";
    private final String twitchTag = "&";

    public CommandMap() {
        this.bot = BOT.getInstance();

        registerCommands(new CommandDefault(this), new RoleCommand(this), new HelpCommand(this), new MusicCommand(this), new CommandWeather(this), new CommandNotif(this), new CommandChangelog(this), new CommandVersion(this), new CommandSession(this), new CommandOctogone(this), new CommandSay(this), new CommandEmbed(this), new CommandIgnore(this));
        registerTwitchCommands(new TwitchWeather(this), new TwitchHelp(this), new TwitchNotif(this), new TwitchVersion(this), new TwitchAquoijouer(this), new TwitchClipThat(this), new TwitchSCP(this), new TwitchJeparticipe(this));

        load();
    }

    private void load()
    {

        //Loading users powers

        File file = new File("userspower.json");
        if(!file.exists()) return;

        try{
            JSONReader reader = new JSONReader(file);
            JSONArray array = reader.toJSONArray();

            for(int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                powers.put(object.getLong("id"), object.getInt("power"));
            }

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }

        //Loading users ids

        File file3 = new File("userids.json");
        if(!file3.exists()) return;

        try{
            JSONReader reader = new JSONReader(file3);
            JSONArray array = reader.toJSONArray();

            for(int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                String id = object.getString("id");
                userIds.add(id);
            }

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }

        //Loading giveaway ids

        File file4 = new File("giveaway.json");
        if(!file4.exists()) return;

        try{
            JSONReader reader = new JSONReader(file4);
            JSONArray array = reader.toJSONArray();

            for(int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                String id = object.getString("id");
                giveawayUsersIds.add(id);
            }

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }
    }

    public void save()
    {

        //Users power

        JSONArray array = new JSONArray();

        for(Entry<Long, Integer> power : powers.entrySet())
        {
            JSONObject object = new JSONObject();
            object.accumulate("id", power.getKey());
            object.accumulate("power", power.getValue());
            array.put(object);
        }

        try(JSONWriter writter = new JSONWriter("userspower.json")){

            writter.write(array);
            writter.flush();

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }

        //FILE IDS

        JSONArray array3 = new JSONArray();

        for(String userId : userIds)
        {
            JSONObject object = new JSONObject();
            object.accumulate("id", userId);
            array3.put(object);
        }

        try(JSONWriter writter = new JSONWriter("userids.json")){

            writter.write(array3);
            writter.flush();

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }

        //GIVEAWAY

        JSONArray array4 = new JSONArray();

        for(String userId : giveawayUsersIds)
        {
            JSONObject object = new JSONObject();
            object.accumulate("id", userId);
            array4.put(object);
        }

        try(JSONWriter writter = new JSONWriter("giveaway.json")){

            writter.write(array4);
            writter.flush();

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }
    }

    public EmbedBuilder getHelpEmbed(String command) {
        try {
            SimpleCommand command1 = discordCommands.get(command);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Aide » "+discordTag + command);
            embedBuilder.setDescription(command1.getDescription());
            embedBuilder.addField("Utilisation:", discordTag+command1.getHelp(), true);
            embedBuilder.addField("Exemple:", discordTag+command1.getExemple(), true);
            return embedBuilder;
        }catch (Exception e){
            bot.getErrorHandler().handleException(e);
        }
        return new EmbedBuilder();
    }

    public String getTwitchHelpString(String command){
        try {
            SimpleTwitchCommand command1 = twitchCommands.get(command);
            return "Aide » "+ twitchTag + command+" | "+command1.getDescription()+ " | Utilisation:"+twitchTag+command1.getHelp()+ " | Exemple: "+twitchTag+command1.getExemple();
        }catch (Exception e){
            bot.getErrorHandler().handleException(e);
        }
        return "";
    }

    public void addKnownUser(String id){
        if(!userIds.contains(id)) userIds.add(id);
    }

    public boolean isKnown(String id){
        return userIds.contains(id);
    }

    public void addGiveawayUser(String id){
        if(!giveawayUsersIds.contains(id)) giveawayUsersIds.add(id);
    }

    public boolean isGiveawayKnown(String id){
        return giveawayUsersIds.contains(id);
    }

    public void addUserPower(User user, int power)
    {
        if(power == 0) removeUserPower(user);
        else powers.put(user.getIdLong(), power);
    }

    public void removeUserPower(User user)
    {
        powers.remove(user.getIdLong());
    }

    public int getPowerUser(Guild guild, User user)
    {
        if(guild.getMember(user).hasPermission(Permission.ADMINISTRATOR)) return 150;
        return powers.containsKey(user.getIdLong()) ? powers.get(user.getIdLong()) : 0;
    }

    public String getDiscordTag() {
        return discordTag;
    }

    public String getTwitchTag() {
        return twitchTag;
    }

    public Collection<SimpleCommand> getDiscordCommands(){
        return discordCommands.values();
    }

    public Collection<SimpleTwitchCommand> getTwitchCommands(){
        return twitchCommands.values();
    }

    public void registerCommands(Object...objects){
        for(Object object : objects){
            registerCommand(object);
        }
    }

    public void registerCommand(Object object){
        for(Method method : object.getClass().getDeclaredMethods()){
            if(method.isAnnotationPresent(Command.class)){
                Command command = method.getAnnotation(Command.class);
                method.setAccessible(true);
                SimpleCommand simpleCommand = new SimpleCommand(command.name(), command.description(), command.help(), command.example(), command.type(), object, method, command.power());
                discordCommands.put(command.name(), simpleCommand);
            }
        }
    }

    public void discordCommandConsole(String command){
        Object[] object = getDiscordCommand(command);
        if(object[0] == null || ((SimpleCommand)object[0]).getExecutorType() == Command.ExecutorType.USER){
            bot.getLogger().log(Level.WARNING,"Commande inconnue.");
            return;
        }
        try{
            executeDiscordCommand(((SimpleCommand)object[0]), command, (String[])object[1], null);
        }catch(Exception e){
            bot.getLogger().log(Level.SEVERE,"La commande "+command+" ne s'est pas exécutée à cause d'un problème sur la méthode "+((SimpleCommand)object[0]).getMethod().getName()+" qui ne s'est pas correctement exécutée.");
            bot.getErrorHandler().handleException(e);
        }
    }

    public boolean discordCommandUser(User user, String command, Message message){
        Object[] object = getDiscordCommand(command);
        if(object[0] == null || ((SimpleCommand)object[0]).getExecutorType() == Command.ExecutorType.CONSOLE) return false;

        if(((SimpleCommand) object[0]).getPower() > getPowerUser(message.getGuild(), message.getAuthor())) return false;

        try{
            executeDiscordCommand(((SimpleCommand)object[0]), command,(String[])object[1], message);
        }catch(Exception e){
            bot.getLogger().log(Level.SEVERE,"La methode "+((SimpleCommand)object[0]).getMethod().getName()+" n'est pas correctement initialisé. ("+e.getMessage()+")");
            bot.getErrorHandler().handleException(e);
        }
        return true;
    }

    private Object[] getDiscordCommand(String command){
        String[] commandSplit = command.split(" ");
        String[] args = new String[commandSplit.length-1];
        for(int i = 1; i < commandSplit.length; i++) args[i-1] = commandSplit[i];
        SimpleCommand simpleCommand = discordCommands.get(commandSplit[0]);
        return new Object[]{simpleCommand, args};
    }

    public SimpleCommand getDiscordSimpleCommand(String command){
        return discordCommands.get(command);
    }

    private void executeDiscordCommand(SimpleCommand simpleCommand, String command, String[] args, Message message) throws Exception{
        Parameter[] parameters = simpleCommand.getMethod().getParameters();
        Object[] objects = new Object[parameters.length];
        for(int i = 0; i < parameters.length; i++){
            if(parameters[i].getType() == String[].class) objects[i] = args;
            else if(parameters[i].getType() == User.class) objects[i] = message == null ? null : message.getAuthor();
            else if(parameters[i].getType() == TextChannel.class) objects[i] = message == null ? null : message.getTextChannel();
            else if(parameters[i].getType() == PrivateChannel.class) objects[i] = message == null ? null : message.getPrivateChannel();
            else if(parameters[i].getType() == Guild.class) objects[i] = message == null ? null : message.getGuild();
            else if(parameters[i].getType() == String.class) objects[i] = command;
            else if(parameters[i].getType() == Message.class) objects[i] = message;
            else if(parameters[i].getType() == JDA.class) objects[i] = bot.getJda();
            else if(parameters[i].getType() == MessageChannel.class) objects[i] = message == null ? null : message.getChannel();
            else if(parameters[i].getType() == SimpleCommand.class) objects[i] = simpleCommand;
        }
        simpleCommand.getMethod().invoke(simpleCommand.getObject(), objects);
    }

    public void registerTwitchCommands(Object... objects){
        for(Object object : objects){
            registerTwitchCommand(object);
        }
    }

    private void registerTwitchCommand(Object object) {
        for(Method method : object.getClass().getDeclaredMethods()){
            if(method.isAnnotationPresent(TwitchCommand.class)){
                TwitchCommand command = method.getAnnotation(TwitchCommand.class);
                method.setAccessible(true);
                SimpleTwitchCommand simpleTwitchCommand = new SimpleTwitchCommand(command.name(), command.description(), command.help(), command.example(), command.rank(), object, method);
                twitchCommands.put(command.name(), simpleTwitchCommand);
            }
        }
    }

    public boolean twitchCommandUser(com.github.twitch4j.helix.domain.User user, String broadcaster, String broadcasterId, TwitchCommand.ExecutorRank executorRank, String command, Set<CommandPermission> commandPermissions){
        Object[] object = getTwitchCommand(command);
        if(object[0] == null || ((SimpleTwitchCommand)object[0]).getExecutorRank().getPower() > executorRank.getPower()) return false;
        try{
            executeTwitchCommand(((SimpleTwitchCommand)object[0]), broadcaster, broadcasterId, user, (String[])object[1], commandPermissions);
        }catch(Exception e){
            bot.getLogger().log(Level.SEVERE,"La methode "+((SimpleTwitchCommand)object[0]).getMethod().getName()+" n'est pas correctement initialisé.");
            bot.getErrorHandler().handleException(e);
        }
        return true;
    }

    private Object[] getTwitchCommand(String command){
        String[] commandSplit = command.split(" ");
        String[] args = new String[commandSplit.length-1];
        for(int i = 1; i < commandSplit.length; i++) args[i-1] = commandSplit[i];
        SimpleTwitchCommand simpleTwitchCommand = twitchCommands.get(commandSplit[0]);
        return new Object[]{simpleTwitchCommand, args};
    }

    public SimpleTwitchCommand getTwitchSimpleCommand(String command){
        return twitchCommands.get(command);
    }

    private void executeTwitchCommand(SimpleTwitchCommand simpleTwitchCommand, String broadcaster, String broadcasterId, com.github.twitch4j.helix.domain.User user, String[] args, Set<CommandPermission> commandPermissions) throws Exception{
        Parameter[] parameters = simpleTwitchCommand.getMethod().getParameters();
        Object[] objects = new Object[parameters.length];
        for(int i = 0; i < parameters.length; i++){
            if(parameters[i].getType() == String[].class) objects[i] = args;
            else if(parameters[i].getType() == com.github.twitch4j.helix.domain.User.class) objects[i] = user;
            else if(parameters[i].getType() == String.class) objects[i] = broadcaster;
            else if(parameters[i].getType() == Long.class) objects[i] = Long.valueOf(broadcasterId);
            else if(parameters[i].getType() == SimpleTwitchCommand.class) objects[i] = simpleTwitchCommand;
            else if(parameters[i].getType() == Set.class) objects[i] = commandPermissions;
        }
        simpleTwitchCommand.getMethod().invoke(simpleTwitchCommand.getObject(), objects);
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public List<String> getGiveawayUsersIds() {
        return giveawayUsersIds;
    }

    public void clearGiveaway(){
        giveawayUsersIds.clear();
    }

    public TwitchCommand.ExecutorRank getRank(Set<CommandPermission> permissions) {
        if(permissions.contains(CommandPermission.BROADCASTER))return TwitchCommand.ExecutorRank.OWNER;
        if(permissions.contains(CommandPermission.MODERATOR))return TwitchCommand.ExecutorRank.MOD;
        if(permissions.contains(CommandPermission.VIP))return TwitchCommand.ExecutorRank.VIP;
        if(permissions.contains(CommandPermission.SUBSCRIBER))return TwitchCommand.ExecutorRank.SUBSCRIBER;
        return TwitchCommand.ExecutorRank.EVERYONE;
    }
}