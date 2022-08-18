package me.maxouxax.boulobot.sessions;

import me.maxouxax.boulobot.BOT;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RetryTask {

    private final Date startDate;
    private Consumer<Session> sessionConsumer;
    private Session currentSession;

    public RetryTask(Session currentSession) {
        this.currentSession = currentSession;
        this.startDate = new Date();
    }

    public void setCallbackOnEachTry(Consumer<Session> sessionConsumer) {
        this.sessionConsumer = sessionConsumer;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    public void success(boolean successful) {
        if (!successful) {
            if ((new Date().getTime() - startDate.getTime()) > 1000 * 60 * 10) {
                //Si on réessaie depuis plus de 10 minutes, alors on cancel la session
                BOT.getInstance().getSessionManager().cancelCurrentSession();
            } else {
                retryIn(10, TimeUnit.SECONDS);
            }
        } else {
            //La session a redémarrée
            BOT.getInstance().getSessionManager().taskUpdateSessionMessage();
        }
    }

    public void retryIn(int delay, TimeUnit timeUnit) {
        BOT.getInstance().getScheduler().schedule(() -> sessionConsumer.accept(currentSession), delay, timeUnit);
    }

}
