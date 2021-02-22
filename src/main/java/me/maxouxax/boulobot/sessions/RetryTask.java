package me.maxouxax.boulobot.sessions;

import me.maxouxax.boulobot.BOT;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RetryTask {

    private Consumer<Session> sessionConsumer;
    private Session currentSession;

    public RetryTask(Session currentSession) {
        this.currentSession = currentSession;
    }

    public void setCallbackOnEachTry(Consumer<Session> sessionConsumer) {
        this.sessionConsumer = sessionConsumer;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    public void success(boolean successful) {
        if(!successful){
            retryIn(10, TimeUnit.SECONDS);
        }
    }

    public void retryIn(int delay, TimeUnit timeUnit) {
        BOT.getInstance().getScheduler().schedule(() -> sessionConsumer.accept(currentSession), delay, timeUnit);
    }

}
