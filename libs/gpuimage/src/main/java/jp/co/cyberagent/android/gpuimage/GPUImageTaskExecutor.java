package jp.co.cyberagent.android.gpuimage;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class GPUImageTaskExecutor {

    private static final ExecutorService BACKGROUND_EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private GPUImageTaskExecutor() {
    }

    static void execute(Runnable backgroundWork) {
        BACKGROUND_EXECUTOR.execute(backgroundWork);
    }

    static void postToMain(Runnable work) {
        MAIN_HANDLER.post(work);
    }
}
