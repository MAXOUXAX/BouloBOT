package net.maxx.boulobot.commands.register.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.Command;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.commands.SimpleCommand;
import net.maxx.boulobot.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class CommandChangelog {

    private final BOT botDiscord;
    private final CommandMap commandMap;
    private Changelog toPost;

    public CommandChangelog(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
        this.toPost = null;
    }

    @Command(name = "changelog",help = ".changelog",example = ".changelog", description = "Permet d'avoir de l'aide sur les commandes de changelogs", type = Command.ExecutorType.ALL, power = 100)
    public void changelog(TextChannel textChannel, String[] args, SimpleCommand simpleCommand) {
        if(args.length == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(simpleCommand.getName().toUpperCase(), Reference.WebsiteURL.getString());
            embedBuilder.addField(".changelog set", "Permet de définir les propriétés du changelog à poster", false);
            embedBuilder.addField(".changelog add", "Permet d'ajouter une modification au changelog à poster", false);
            embedBuilder.addField(".changelog remove", "Permet de supprimer une modification au changelog à poster", false);
            embedBuilder.addField(".changelog list", "Permet d'afficher la liste des modifications au changelog à poster", false);
            embedBuilder.addField(".changelog post", "Permet de poster le changelog", false);
            embedBuilder.setColor(3447003);
            embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
            textChannel.sendMessage(embedBuilder.build()).queue();
        }else{
            String arg1 = args[0];
            if(arg1.equalsIgnoreCase("set")){
                if(args.length == 1){
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Changelog » SET", Reference.WebsiteURL.getString());
                    embedBuilder.addField(".changelog set <name> ²² <oldver>", "Permet de définir les propriétés du changelog à poster", false);
                    embedBuilder.setColor(3447003);
                    embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
                    textChannel.sendMessage(embedBuilder.build()).queue();
                }else{
                    if (toPost != null) {
                        StringBuilder str = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            str.append(args[i]).append(" ");
                        }
                        if (!str.toString().contains("²²")) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog set").queue();
                            return;
                        }
                        String[] argsReal = str.toString().split(" ²² ");

                        if (argsReal.length == 1) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog set").queue();
                        } else if (argsReal.length == 2) {
                            String name = argsReal[0];
                            String oldver = argsReal[1];
                            toPost = new Changelog(botDiscord, name, oldver);
                            textChannel.sendMessage("Changelog crée !").queue();
                        }
                    }else{
                        StringBuilder str = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            str.append(args[i]).append(" ");
                        }
                        if (!str.toString().contains("²²")) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog set").queue();
                            return;
                        }
                        String[] argsReal = str.toString().split(" ²² ");

                        if (argsReal.length == 1) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog set").queue();
                        } else if (argsReal.length == 2) {
                            String name = argsReal[0];
                            String oldver = argsReal[1];
                            toPost = new Changelog(botDiscord, name, oldver);
                            textChannel.sendMessage("Changelog crée !").queue();
                        }
                    }
                }
            }else if(arg1.equalsIgnoreCase("add")){
                if(args.length == 1){
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Changelog » ADD", Reference.WebsiteURL.getString());
                    embedBuilder.addField(".changelog add <name> ²² <description> ²² <type> ²² <platform>", "Permet d'ajouter une modification au changelog à poster", false);
                    embedBuilder.addField("Liste des types", Arrays.toString(State.values()), false);
                    embedBuilder.addField("Liste des platformes", Arrays.toString(Platform.values()), false);
                    embedBuilder.setColor(3447003);
                    embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
                    textChannel.sendMessage(embedBuilder.build()).queue();
                }else {
                    if (toPost != null) {
                        StringBuilder str = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            str.append(args[i]).append(" ");
                        }
                        if (!str.toString().contains("²²")) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog add").queue();
                            return;
                        }
                        String[] argsReal = str.toString().split(" ²² ");

                        if (argsReal.length == 1) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog add").queue();
                        } else if (argsReal.length == 2) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog add").queue();
                        } else if (argsReal.length == 3) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog add").queue();
                        } else if (argsReal.length == 4) {
                            String name = argsReal[0];
                            String description = argsReal[1];
                            String type = argsReal[2];
                            String plat = argsReal[3];
                            textChannel.sendMessage("plat: "+plat).queue();
                            State state = State.getByName(type);
                            Platform platform = Platform.getByName(plat);
                            toPost.addModification(new Modifications(name, description, state), platform);
                            textChannel.sendMessage("Modification ajoutée, faites .changelog list pour voir la liste des modifications !").queue();
                        }
                    }else{
                        textChannel.sendMessage("Commencez par faire .changelog set").queue();
                    }
                }
            }else if(arg1.equalsIgnoreCase("remove")){
                if(args.length == 1){
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Changelog » REMOVE", Reference.WebsiteURL.getString());
                    embedBuilder.addField(".changelog remove <name> ²² <platform>", "Permet de supprimer une modification du changelog à poster", false);
                    embedBuilder.addField("Liste des platformes", Platform.values().toString(), false);
                    embedBuilder.setColor(3447003);
                    embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
                    textChannel.sendMessage(embedBuilder.build()).queue();
                }else {
                    if (toPost != null) {
                        StringBuilder str = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            str.append(args[i]).append(" ");
                        }
                        if (!str.toString().contains("²²")) {
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog remove").queue();
                            return;
                        }
                        String[] argsReal = str.toString().split(" ²² ");

                        if(argsReal.length == 1){
                            textChannel.sendMessage("Veuillez vous référer à l'aide disponible en faisant .changelog remove").queue();
                        }else if(argsReal.length == 2) {
                            String name = argsReal[0];
                            String plat = argsReal[1];
                            Platform platform = Platform.getByName(plat);

                            Optional<Modifications> optModif = toPost.getHashMap().get(platform).stream().filter(modifications -> modifications.getName().equalsIgnoreCase(name)).findFirst();
                            if (optModif.isPresent()) {
                                Modifications modif = optModif.get();
                                toPost.removeModification(modif, platform);
                                textChannel.sendMessage("Modification retirée, faites .changelog list pour voir la liste des modifications !").queue();
                            } else {
                                textChannel.sendMessage("Aucune modification avec le nom \"" + name + "\" trouvée...\nFaites .changelog list pour avoir la liste des modifications").queue();
                            }
                        }
                    } else {
                        textChannel.sendMessage("Commencez par faire .changelog set").queue();
                    }
                }
            }else if(arg1.equalsIgnoreCase("list")){
                if(toPost == null){
                    textChannel.sendMessage("Pour créer un changelog, utilisez la commande .changelog").queue();
                }else{
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    ArrayList<EmbedBuilder> embedBuilders = new ArrayList<>();
                    embedBuilder.setTitle("Changelog » Liste des modifications", Reference.WebsiteURL.getString());
                    embedBuilder.setColor(3447003);
                    embedBuilder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
                    if(toPost.getHashMap().isEmpty()){
                        embedBuilder.addField("Aucune modification", "Aucune modification", true);
                        textChannel.sendMessage(embedBuilder.build()).queue();
                    }else {
                        embedBuilder.setDescription("Voici la liste des modifications:");
                        for(Platform platform : Platform.values()){
                            textChannel.sendMessage("Platform: "+platform.getName()).queue();
                            textChannel.sendMessage(toPost.getHashMap().toString()).queue();
                            if(toPost.getHashMap().containsKey(platform)) {
                                textChannel.sendMessage("containsKey: "+platform.getName()).queue();
                                ArrayList<Modifications> arrayList = toPost.getHashMap().get(platform);
                                if (!arrayList.isEmpty()) {
                                    textChannel.sendMessage("ArrayList isn't empty").queue();
                                    EmbedBuilder embed = new EmbedBuilder();
                                    textChannel.sendMessage("Created new Embed").queue();
                                    embed.setTitle(platform.getName(), Reference.WebsiteURL.getString());
                                    textChannel.sendMessage("setTitle").queue();
                                    embed.setColor(platform.getEmbedColor());
                                    textChannel.sendMessage("setColor").queue();
                                    embedBuilder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
                                    textChannel.sendMessage("setFooter").queue();
                                    arrayList.forEach(modifications -> {
                                        textChannel.sendMessage("forEach").queue();
                                        embed.addField(modifications.getState().getName() + " | " + modifications.getName(), modifications.getDescription(), true);
                                        textChannel.sendMessage("forEach::addField").queue();
                                    });
                                    embedBuilders.add(embed);
                                    textChannel.sendMessage("added the embed to embedbuilders list").queue();
                                }else{
                                    textChannel.sendMessage("ArrayList empty").queue();
                                }
                            }
                        }
                        textChannel.sendMessage(embedBuilder.build()).queue();
                        textChannel.sendMessage("base embed").queue();
                        embedBuilders.forEach(embedBuilder1 -> {
                            textChannel.sendMessage("send embedbuilders").queue();
                            textChannel.sendMessage(embedBuilder1.build()).queue();
                        });
                    }
                }
            }else if(arg1.equalsIgnoreCase("post")){
                if(toPost == null){
                    textChannel.sendMessage("Pour créer un changelog, utilisez la commande .changelog").queue();
                }else{
                    Role changelog = botDiscord.getJda().getGuildById(Reference.GuildID.getString()).getRoleById(Reference.ChangelogRoleID.getString());

                    Integer nbOfModif = 0;
                    for (Platform platform : Platform.values()) {
                        if(toPost.getHashMap().containsKey(platform)) {
                            nbOfModif += toPost.getHashMap().get(platform).size();
                        }
                    }
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    ArrayList<EmbedBuilder> embedBuilders = new ArrayList<>();
                    embedBuilder.setTitle("Changelog » "+toPost.getName(), Reference.WebsiteURL.getString());
                    embedBuilder.setDescription(toPost.getOldVersion()+"» "+toPost.getVersion()+"\nCette nouvelle version ("+toPost.getVersion()+") comporte "+nbOfModif+" modifications !\nUUID: "+toPost.getUuid().toString());
                    embedBuilder.setColor(3447003);
                    embedBuilder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
                    if(toPost.getHashMap().isEmpty()){
                        embedBuilder.addField("Aucune modification", "Aucune modification", true);
                        textChannel.sendMessage(embedBuilder.build()).queue();
                    }else {
                        for (Platform platform : Platform.values()) {
                            if (toPost.getHashMap().containsKey(platform)) {
                                ArrayList<Modifications> arrayList = toPost.getHashMap().get(platform);
                                if (!arrayList.isEmpty()) {
                                    EmbedBuilder embed = new EmbedBuilder();
                                    embed.setTitle(platform.getName(), Reference.WebsiteURL.getString());
                                    embed.setColor(platform.getEmbedColor());
                                    embedBuilder.setFooter(Reference.EmbedFooter.getString(), Reference.EmbedIcon.getString());
                                    arrayList.forEach(modifications -> {
                                        embed.addField(modifications.getState().getName() + " | " + modifications.getName(), modifications.getDescription(), true);
                                    });
                                    embedBuilders.add(embed);
                                }
                            }
                        }
                        Message message = new MessageBuilder(changelog.getAsMention()).setEmbed(embedBuilder.build()).build();
                        textChannel.sendMessage(message).queue();
                        embedBuilders.forEach(embedBuilder1 -> {
                            textChannel.sendMessage(embedBuilder1.build()).queue();
                        });
                    }
                }
            }
        }
    }

}
