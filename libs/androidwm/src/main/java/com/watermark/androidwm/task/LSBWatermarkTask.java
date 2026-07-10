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
import static com.watermark.androidwm.utils.StringUtils.embedLsbWatermark;

import android.graphics.Bitmap;

import com.watermark.androidwm.bean.AsyncTaskParams;
import com.watermark.androidwm.bean.WatermarkText;
import com.watermark.androidwm.listener.BuildFinishListener;
import com.watermark.androidwm.utils.BitmapUtils;

/**
 * This is a background task for adding the specific invisible text
 * into the background image. We don't need to read every pixel's
 * RGB value, we just read the length values that can put our encrypted
 * text in.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class LSBWatermarkTask {

    private final BuildFinishListener<Bitmap> listener;

    public LSBWatermarkTask(BuildFinishListener<Bitmap> callback) {
        this.listener = callback;
    }

    private Bitmap doInBackground(AsyncTaskParams... params) {
        Bitmap backgroundBitmap = params[0].getBackgroundImg();
        WatermarkText watermarkText = params[0].getWatermarkText();
        Bitmap watermarkBitmap = params[0].getWatermarkImg();
        String watermarkString;

        if (backgroundBitmap == null) {
            listener.onFailure(ERROR_NO_BACKGROUND);
            return null;
        }

        // convert the watermark bitmap into a String.
        if (watermarkBitmap != null) {
            watermarkString = BitmapUtils.bitmapToString(watermarkBitmap);
        } else {
            watermarkString = watermarkText.getText();
        }

        if (watermarkString == null) {
            listener.onFailure(ERROR_NO_WATERMARKS);
            return null;
        }

        Bitmap outputBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        if (!embedLsbWatermark(backgroundBitmap, outputBitmap, watermarkString,
                watermarkBitmap != null)) {
            listener.onFailure(ERROR_PIXELS_NOT_ENOUGH);
            return null;
        }
        return outputBitmap;
    }

    private void onPostExecute(Bitmap resultBitmap) {
        if (listener != null) {
            if (resultBitmap != null) {
                listener.onSuccess(resultBitmap);
            } else {
                listener.onFailure(ERROR_CREATE_FAILED);
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
