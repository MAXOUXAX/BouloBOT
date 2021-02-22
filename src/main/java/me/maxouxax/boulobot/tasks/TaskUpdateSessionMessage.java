package me.maxouxax.boulobot.tasks;

import com.github.twitch4j.helix.domain.Stream;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.sessions.Session;

public class TaskUpdateSessionMessage implements Runnable {

    private final BOT bot;
    private Stream stream;
    private final Session session;
    private final String channelId;

    public TaskUpdateSessionMessage(String channelId) {
        this.bot = BOT.getInstance();
        this.channelId = channelId;
        this.session = bot.getSessionManager().getCurrentSession();
    }

    @Override
    public void run() {
        refreshStreamObject();
        int viewerCount = stream.getViewerCount();
        if(viewerCount > session.getMaxViewers()){
            session.setMaxViewers(viewerCount);
        }
        bot.getSessionManager().addViewerCount(viewerCount);
        session.updateMessage();
    }

    private void refreshStreamObject() {
        this.stream = Session.getStream(bot, channelId);
    }

}
