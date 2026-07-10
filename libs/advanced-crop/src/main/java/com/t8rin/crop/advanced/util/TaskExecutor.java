package com.t8rin.crop.advanced.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TaskExecutor {

    private static final ExecutorService BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private TaskExecutor() {
    }

    public static void execute(Runnable backgroundWork) {
        BACKGROUND_EXECUTOR.execute(backgroundWork);
    }

    public static void postToMain(Runnable work) {
        MAIN_HANDLER.post(work);
    }
}
