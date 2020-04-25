package net.maxouxax.boulobot.commands.register.twitch;

import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TwitchKappa {

    private final CommandMap commandMap;
    private final BOT bot;
    private final String kappaSongURL = "https://www.youtube.com/watch?v=33E5P5gd9CY";

    public TwitchKappa(BOT bot, CommandMap commandMap) {
        this.bot = bot;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name="kappa",rank = TwitchCommand.ExecutorRank.VIP,description="Ajoute la KappaSong au songrequest", help = "&kappa", example = "&kappa")
    private void kappa(String broadcaster, Long broadcasterId) throws IOException {
        System.out.println(broadcaster);
        System.out.println(broadcasterId);
        bot.getTwitchClient().getChat().sendMessage(broadcaster, "Attention, le tchat va devenir indisponible pendant la Kappa Song, gardez votre calme et BALANCEZ VOS KAPPAS.");
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://api.streamelements.com/kappa/v2/songrequest/"+broadcasterId+"/queue");

        List<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("video", kappaSongURL));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httppost.setParams(new BasicHttpParams().setParameter("video", kappaSongURL));
        httppost.addHeader("accept", "application/json");
        httppost.addHeader("content-type", "application/json");
        httppost.addHeader("Authorization", "Bearer "+bot.getConfigurationManager().getStringValue("streamelementsApiKey"));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                System.out.println(response.getStatusLine().getStatusCode());
                System.out.println(response.getStatusLine().getProtocolVersion());
                System.out.println(response.getStatusLine().getReasonPhrase());
                System.out.println(instream);
            }
        }
    }

}
