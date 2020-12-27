package me.maxouxax.boulobot.tasks;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.sessions.Session;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class TaskViewerCheck implements Runnable {

    private final BOT bot;
    private Stream stream;
    private final Session session;
    private final String channelId;

    public TaskViewerCheck(String channelId) {
        this.bot = BOT.getInstance();
        this.channelId = channelId;
        this.session = bot.getSessionManager().getCurrentSession();
    }

    @Override
    public void run() {
        refreshStreamObject();
        int viewerCount = stream.getViewerCount();
        bot.getLogger().log(Level.INFO, "viewerCount = "+viewerCount);
        if(viewerCount > session.getMaxViewers()){
            session.setMaxViewers(viewerCount);
        }
        bot.getSessionManager().addViewerCount(viewerCount);
    }

    private void refreshStreamObject() {
        StreamList streamResultList = bot.getTwitchClient().getHelix().getStreams(bot.getConfigurationManager().getStringValue("oauth2Token"), "", "", null, null, null, Collections.singletonList(channelId), null).execute();
        final AtomicReference<Stream> currentStream = new AtomicReference<>();
        streamResultList.getStreams().forEach(currentStream::set);
        this.stream = currentStream.get();
    }

}
