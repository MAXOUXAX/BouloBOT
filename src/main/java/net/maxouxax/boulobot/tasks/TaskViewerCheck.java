package net.maxouxax.boulobot.tasks;

import com.github.twitch4j.helix.domain.Stream;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.util.Session;

public class TaskViewerCheck implements Runnable {

    private BOT botDiscord;
    private Stream stream;
    private Session session;

    public TaskViewerCheck(BOT botDiscord, Stream stream) {
        this.botDiscord = botDiscord;
        this.stream = stream;
        this.session = botDiscord.getSessionManager().getCurrentSession();
    }

    @Override
    public void run() {
        int viewerCount = stream.getViewerCount();
        if(viewerCount > session.getMaxViewers()){
            session.setMaxViewers(viewerCount);
        }
        botDiscord.getSessionManager().addViewerCount(viewerCount);
    }

}
