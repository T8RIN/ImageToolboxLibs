package com.watermark.androidwm.task;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class TaskExecutor {

    private static final ExecutorService BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private TaskExecutor() {
    }

    static Future<?> execute(Runnable backgroundWork) {
        return BACKGROUND_EXECUTOR.submit(backgroundWork);
    }

    static void postToMain(Runnable work) {
        MAIN_HANDLER.post(work);
    }
}
