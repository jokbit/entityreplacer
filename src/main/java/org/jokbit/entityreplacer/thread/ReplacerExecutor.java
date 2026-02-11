package org.jokbit.entityreplacer.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReplacerExecutor {
    private static ExecutorService service;

    private static volatile ReplacerExecutor INSTANCE;

    private ReplacerExecutor() {
        service = Executors.newSingleThreadExecutor();
    }

    public static ReplacerExecutor getInstance() {
        if (INSTANCE == null) {
            synchronized (ReplacerExecutor.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ReplacerExecutor();
                }
            }
        }
        return INSTANCE;
    }

    public void submit(Runnable runnable) {
        service.submit(runnable);
    }
}
