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

import static com.watermark.androidwm.utils.Constant.ERROR_CREATE_FAILED;
import static com.watermark.androidwm.utils.Constant.ERROR_NO_BACKGROUND;
import static com.watermark.androidwm.utils.Constant.ERROR_NO_WATERMARKS;
import static com.watermark.androidwm.utils.Constant.ERROR_PIXELS_NOT_ENOUGH;
import static com.watermark.androidwm.utils.StringUtils.embedFdWatermark;

import android.graphics.Bitmap;

import com.watermark.androidwm.bean.AsyncTaskParams;
import com.watermark.androidwm.bean.WatermarkText;
import com.watermark.androidwm.listener.BuildFinishListener;
import com.watermark.androidwm.utils.BitmapUtils;

public class FDWatermarkTask {

    private final BuildFinishListener<Bitmap> listener;
    private String failure;

    public FDWatermarkTask(BuildFinishListener<Bitmap> callback) {
        this.listener = callback;
    }

    private Bitmap doInBackground(AsyncTaskParams... params) {
        Bitmap backgroundBitmap = params[0].getBackgroundImg();
        Bitmap watermarkBitmap = params[0].getWatermarkImg();
        WatermarkText watermarkText = params[0].getWatermarkText();

        if (backgroundBitmap == null) {
            failure = ERROR_NO_BACKGROUND;
            return null;
        }
        String watermark;
        boolean isImage;
        if (watermarkBitmap != null) {
            watermark = BitmapUtils.bitmapToFdString(watermarkBitmap,
                    getPayloadCapacity(backgroundBitmap));
            isImage = true;
        } else if (watermarkText != null) {
            watermark = watermarkText.getText();
            isImage = false;
        } else {
            failure = ERROR_NO_WATERMARKS;
            return null;
        }

        if (watermark == null) {
            failure = ERROR_PIXELS_NOT_ENOUGH;
            return null;
        }
        Bitmap outputBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        if (!embedFdWatermark(backgroundBitmap, outputBitmap, watermark, isImage)) {
            failure = ERROR_PIXELS_NOT_ENOUGH;
            return null;
        }
        return outputBitmap;
    }

    private int getPayloadCapacity(Bitmap bitmap) {
        long capacityBits = (long) (bitmap.getWidth() / 8) * (bitmap.getHeight() / 8);
        return (int) Math.max(0, (capacityBits - 64) / 8);
    }

    private void onPostExecute(Bitmap bitmap) {
        if (listener != null) {
            if (bitmap != null) {
                listener.onSuccess(bitmap);
            } else {
                listener.onFailure(failure != null ? failure : ERROR_CREATE_FAILED);
            }
        }
    }

    public void execute(AsyncTaskParams... params) {
        TaskExecutor.execute(() -> {
            Bitmap result = doInBackground(params);
            TaskExecutor.postToMain(() -> onPostExecute(result));
        });
    }
}
