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

import static com.watermark.androidwm.utils.Constant.ERROR_BITMAP_NULL;
import static com.watermark.androidwm.utils.Constant.ERROR_DETECT_FAILED;
import static com.watermark.androidwm.utils.StringUtils.extractFdWatermark;
import static com.watermark.androidwm.utils.StringUtils.extractLsbWatermark;

import android.graphics.Bitmap;

import com.watermark.androidwm.listener.DetectFinishListener;
import com.watermark.androidwm.utils.BitmapUtils;

public class AutomaticDetectionTask {

    private final DetectFinishListener listener;

    public AutomaticDetectionTask(DetectFinishListener listener) {
        this.listener = listener;
    }

    private DetectionReturnValue doInBackground(Bitmap bitmap) {
        if (bitmap == null) {
            listener.onFailure(ERROR_BITMAP_NULL);
            return null;
        }
        DetectionReturnValue result = new DetectionReturnValue();
        if (setWatermark(result, extractLsbWatermark(bitmap, false), false)
                || setWatermark(result, extractLsbWatermark(bitmap, true), true)
                || setWatermark(result, extractFdWatermark(bitmap, false), false)
                || setWatermark(result, extractFdWatermark(bitmap, true), true)) {
            return result;
        }
        return result;
    }

    private boolean setWatermark(DetectionReturnValue result, String watermark, boolean isImage) {
        if (watermark == null) {
            return false;
        }
        if (isImage) {
            Bitmap bitmap = BitmapUtils.stringToBitmap(watermark);
            if (bitmap == null) {
                return false;
            }
            result.setWatermarkBitmap(bitmap);
        } else {
            result.setWatermarkString(watermark);
        }
        return true;
    }

    private void onPostExecute(DetectionReturnValue result) {
        if (result == null || (result.getWatermarkString() == null && result.getWatermarkBitmap() == null)) {
            listener.onFailure(ERROR_DETECT_FAILED);
        } else {
            listener.onSuccess(result);
        }
    }

    public void execute(Bitmap bitmap) {
        TaskExecutor.execute(() -> {
            DetectionReturnValue result = doInBackground(bitmap);
            TaskExecutor.postToMain(() -> onPostExecute(result));
        });
    }
}
