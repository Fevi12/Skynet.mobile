package no.skynet.mobile.runtime;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SkynetEventBus implements AutoCloseable {
    public interface Listener {
        void onEvent(Event event);
    }

    public static final class Event {
        public final String type, source, detail;
        public final long timestamp;

        Event(String t, String s, String d) {
            type=t; source=s; detail=d;
            timestamp=System.currentTimeMillis();
        }
    }

    private final CopyOnWriteArrayList<Listener> listeners =
        new CopyOnWriteArrayList<>();

    private final ExecutorService queue =
        Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "skynet-events");
            t.setDaemon(true);
            return t;
        });

    private volatile boolean closed;

    public void subscribe(Listener l) {
        if (l != null && !closed) listeners.addIfAbsent(l);
    }

    public void publish(String type, String source, String detail) {
        if (closed) return;
        Event e = new Event(type, source, detail);
        queue.execute(() -> {
            for (Listener l : listeners) {
                try { l.onEvent(e); }
                catch (RuntimeException ignored) {}
            }
        });
    }

    @Override public void close() {
        closed=true;
        listeners.clear();
        queue.shutdownNow();
    }
}
