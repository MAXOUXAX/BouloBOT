package net.maxouxax.boulobot.tasks;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.util.Session;

import java.util.Collections;
import java.util.logging.Level;

public class TaskViewerCheck implements Runnable {

    private BOT botDiscord;
    private Stream stream;
    private Session session;

    public TaskViewerCheck(BOT botDiscord, String channelId) {
        StreamList streamResultList = botDiscord.getTwitchClient().getHelix().getStreams(botDiscord.getConfigurationManager().getStringValue("oauth2Token"), "", "", null, null, null, null, Collections.singletonList(channelId), null).execute();
        final Stream[] currentStream = new Stream[1];
        streamResultList.getStreams().forEach(stream -> {
            currentStream[0] = stream;
        });
        this.botDiscord = botDiscord;
        this.stream = currentStream[0];
        this.session = botDiscord.getSessionManager().getCurrentSession();
    }

    @Override
    public void run() {
        int viewerCount = stream.getViewerCount();
        botDiscord.getLogger().log(Level.INFO, "viewerCount = "+viewerCount);
        if(viewerCount > session.getMaxViewers()){
            session.setMaxViewers(viewerCount);
        }
        botDiscord.getSessionManager().addViewerCount(viewerCount);
    }

}
