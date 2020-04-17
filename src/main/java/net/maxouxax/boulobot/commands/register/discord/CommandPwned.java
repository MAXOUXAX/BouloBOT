package net.maxouxax.boulobot.commands.register.discord;

import me.legrange.haveibeenpwned.Breach;
import me.legrange.haveibeenpwned.HaveIBeenPwndApi;
import me.legrange.haveibeenpwned.HaveIBeenPwndException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.util.Reference;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CommandPwned {

    private final CommandMap commandMap;

    public CommandPwned(CommandMap commandMap){
        this.commandMap = commandMap;
    }

    @Command(name="pwned",type = Command.ExecutorType.ALL, description = "Permet de récupérer les brèches trouvées via votre adresse e-mail", example = ".pwned example@test.com", help = ".pwned <adresse email>")
    private void pwned(TextChannel textChannel, User user, String[] args){
        EmbedBuilder helperEmbed = commandMap.getHelpEmbed("pwned");
        if(args.length == 0){
            textChannel.sendMessage(helperEmbed.build()).queue();
        }else if(args.length == 1){
            String email = args[0];
            textChannel.sendMessage("Fetching data...").queue();
            textChannel.sendTyping().queue();

            try {
                HaveIBeenPwndApi hibp = new HaveIBeenPwndApi("PwnedDiscordBotCommand");
                List<Breach> breaches = hibp.getAllBreachesForAccount(email);

                EmbedBuilder reply = new EmbedBuilder();
                reply.setTitle("';--have i been pwned?", "https://haveibeenpwned.com/")
                        .setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
                if(breaches.size() <= 0){
                    reply.setDescription("Houra !\n» Aucune brèche n'a été trouvée !")
                            .setColor(3066993);
                }else{
                    reply.setDescription("Aie !\n» "+breaches.size()+(breaches.size() == 1 ? " brèche a été trouvée !" : " brèches ont été trouvées !"))
                            .setColor(15158332);
                    breaches.forEach(breach -> {
                        reply.addField(new MessageEmbed.Field(
                                breach.getName(),
                                breach.getDomain()+"\n\n» "
                                        +breach.getDescription().replaceAll("\\<[^>]*>"
                                        ,"")+"\n\n» "
                                        +new SimpleDateFormat("EEE, d MMM yyyy")
                                        .format(breach.getBreachDate()), true));
                    });
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        textChannel.sendMessage(reply.build()).queue();
                    }
                }, 3000);
            } catch (HaveIBeenPwndException | NoSuchMethodError e) {
                textChannel.sendMessage("Failure!\nAPI returned error code: " + e.getMessage()).queue();
                e.printStackTrace();
            }
        }else{
            textChannel.sendMessage("Veuillez spécifier votre email.\n» `.pwned example@test.com`").queue();
        }
    }

}
