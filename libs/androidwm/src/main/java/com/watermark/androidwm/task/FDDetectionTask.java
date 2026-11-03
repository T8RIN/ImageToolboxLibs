/*
 *    Copyright 2018 Yizheng Huang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.watermark.androidwm.task;

import java.util.concurrent.Future;

import static com.watermark.androidwm.utils.Constant.ERROR_BITMAP_NULL;
import static com.watermark.androidwm.utils.Constant.ERROR_DETECT_FAILED;
import static com.watermark.androidwm.utils.StringUtils.extractFdWatermark;

import android.graphics.Bitmap;

import com.watermark.androidwm.listener.DetectFinishListener;
import com.watermark.androidwm.utils.BitmapUtils;

public class FDDetectionTask {

    private final DetectFinishListener listener;

    public FDDetectionTask(DetectFinishListener listener) {
        this.listener = listener;
    }

    private DetectionReturnValue doInBackground(Bitmap... bitmaps) {
        Bitmap markedBitmap = bitmaps[0];
        if (markedBitmap == null) {
            listener.onFailure(ERROR_BITMAP_NULL);
            return null;
        }

        DetectionReturnValue result = new DetectionReturnValue();
        String textWatermark = extractFdWatermark(markedBitmap, false);
        if (textWatermark != null) {
            result.setWatermarkString(textWatermark);
        } else {
            String imageWatermark = extractFdWatermark(markedBitmap, true);
            if (imageWatermark == null) {
                return result;
            }
            result.setWatermarkBitmap(BitmapUtils.stringToBitmap(imageWatermark));
        }
        return result;
    }

    private void onPostExecute(DetectionReturnValue result) {
        if (result == null || (result.getWatermarkString() == null && result.getWatermarkBitmap() == null)) {
            listener.onFailure(ERROR_DETECT_FAILED);
        } else {
            listener.onSuccess(result);
        }
    }

    public Future<?> execute(Bitmap... bitmaps) {
        return TaskExecutor.execute(() -> {
            DetectionReturnValue result = doInBackground(bitmaps);
            if (Thread.currentThread().isInterrupted()) return;
            TaskExecutor.postToMain(() -> onPostExecute(result));
        });
    }
}
