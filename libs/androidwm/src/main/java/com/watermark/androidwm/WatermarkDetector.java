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
package com.watermark.androidwm;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.watermark.androidwm.listener.DetectFinishListener;
import com.watermark.androidwm.task.AutomaticDetectionTask;
import com.watermark.androidwm.task.FDDetectionTask;
import com.watermark.androidwm.task.LSBDetectionTask;

/**
 * This is for detecting the invisible watermark in one picture.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public final class WatermarkDetector {
    private final Bitmap imageWithWatermark;
    private final Boolean isLSB;

    private WatermarkDetector(
            @NonNull Bitmap imageWithWatermark,
            Boolean isLSB) {
        this.imageWithWatermark = imageWithWatermark;
        this.isLSB = isLSB;
    }

    /**
     * to get an instance form class.
     *
     * @return instance of {@link WatermarkDetector}
     */
    public static WatermarkDetector create(@NonNull Bitmap imageWithWatermark, boolean isLSB) {
        return new WatermarkDetector(imageWithWatermark, isLSB);
    }

    public static WatermarkDetector create(@NonNull Bitmap imageWithWatermark) {
        return new WatermarkDetector(imageWithWatermark, null);
    }

    /**
     * The method for watermark detecting.
     */
    public void detect(DetectFinishListener listener) {
        if (isLSB == null) {
            new AutomaticDetectionTask(listener).execute(imageWithWatermark);
        } else if (isLSB) {
            new LSBDetectionTask(listener).execute(imageWithWatermark);
        } else {
            new FDDetectionTask(listener).execute(imageWithWatermark);
        }
    }
}
