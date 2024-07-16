package com.t8rin.imagetoolbox.app;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Arrays;

public class Extractor {

    private static final int PALETTE_SIZE = 5;

    private final int ngrid = 16;
    private final int grid_size;
    private final double step_size;

    public Extractor() {
        grid_size = ngrid * ngrid * ngrid;
        step_size = 255.0 / (ngrid - 1);
    }

    public int[] extract(Bitmap source) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(source, 200, 200, true);

        int width = resizedBitmap.getWidth();
        int height = resizedBitmap.getHeight();

        int[] sampleCount = new int[grid_size];
        double[][] sampleSum = new double[grid_size][3];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = resizedBitmap.getPixel(x, y);
                double[] rgbPixel = {Color.red(pixel), Color.green(pixel), Color.blue(pixel)};
                double[] labPixel = rgbToLab(rgbPixel);

                long bin1 = Math.round(rgbPixel[0] / step_size);
                long bin2 = Math.round(rgbPixel[1] / step_size);
                long bin3 = Math.round(rgbPixel[2] / step_size);

                int bint = (int) (bin1 * ngrid * ngrid + bin2 * ngrid + bin3);

                sampleCount[bint] += 1;
                sampleSum[bint][0] += labPixel[0];
                sampleSum[bint][1] += labPixel[1];
                sampleSum[bint][2] += labPixel[2];
            }
        }

        int total = 0;
        for (int i = 0; i < grid_size; i++) {
            if (sampleCount[i] > 0) {
                total += 1;
            }
        }

        double[][] D = new double[total][3];
        total = 0;
        for (int i = 0; i < grid_size; i++) {
            if (sampleCount[i] > 0) {
                D[total][0] = sampleSum[i][0] / sampleCount[i];
                D[total][1] = sampleSum[i][1] / sampleCount[i];
                D[total][2] = sampleSum[i][2] / sampleCount[i];
                sampleCount[total] = sampleCount[i];
                total += 1;
            }
        }

        double[][] centers = new double[PALETTE_SIZE + 1][3];
        int[] pickCount = Arrays.copyOf(sampleCount, total);

        for (int i = 0; i < PALETTE_SIZE; i++) {
            int idx = 0;
            for (int j = 0; j < total; j++) {
                if (pickCount[j] > pickCount[idx]) {
                    idx = j;
                }
            }

            centers[i] = D[idx];
            for (int j = 0; j < total; j++) {
                double dis = 0;
                for (int k = 0; k < 3; k++) {
                    dis += Math.pow(D[idx][k] - D[j][k], 2);
                }
                dis = dis / Math.pow(80, 2);
                pickCount[j] *= 1 - Math.exp(-dis);
            }
        }

        centers[PALETTE_SIZE] = new double[]{0, 0, 0};

        int[] count = new int[PALETTE_SIZE + 1];
        double[][] sumD = new double[PALETTE_SIZE + 1][3];
        for (int iter = 0; iter < 20; iter++) {
            for (int i = 0; i < total; i++) {
                int minId = -1;
                double min_v = Double.MAX_VALUE;
                for (int j = 0; j < PALETTE_SIZE; j++) {
                    double r = 0;
                    for (int k = 0; k < 3; k++) {
                        r += Math.pow(D[i][k] - centers[j][k], 2);
                    }
                    if (r < min_v) {
                        min_v = r;
                        minId = j;
                    }
                }

                count[minId] += sampleCount[i];
                sumD[minId][0] += sampleCount[i] * D[i][0];
                sumD[minId][1] += sampleCount[i] * D[i][1];
                sumD[minId][2] += sampleCount[i] * D[i][2];
            }

            for (int i = 0; i < PALETTE_SIZE; i++) {
                if (count[i] > 0) {
                    centers[i][0] = sumD[i][0] / count[i];
                    centers[i][1] = sumD[i][1] / count[i];
                    centers[i][2] = sumD[i][2] / count[i];
                }
            }
        }

        int[] rgbCenters = new int[PALETTE_SIZE + 1];
        for (int i = 0; i < PALETTE_SIZE + 1; i++) {
            rgbCenters[i] = labToRgb(centers[i]);
        }
        return rgbCenters;
    }

    private int labToRgb(double[] lab) {
        double y = (lab[0] + 16) / 116.0;
        double x = lab[1] / 500.0 + y;
        double z = y - lab[2] / 200.0;

        double xyzThreshold = 0.206893034422;
        if (y > xyzThreshold) {
            y = Math.pow(y, 3);
        } else {
            y = (y - 16.0 / 116) / 7.787;
        }
        if (x > xyzThreshold) {
            x = Math.pow(x, 3);
        } else {
            x = (x - 16.0 / 116) / 7.787;
        }
        if (z > xyzThreshold) {
            z = Math.pow(z, 3);
        } else {
            z = (z - 16.0 / 116) / 7.787;
        }

        x = 95.047 * x / 100.0;
        y = 100 * y / 100.0;
        z = 108.883 * z / 100.0;

        double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
        double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
        double b = x * 0.0557 + y * -0.2040 + z * 1.0570;

        double rgbThreshold = 0.0031308;
        if (r > rgbThreshold) {
            r = 1.055 * Math.pow(r, 1 / 2.4) - 0.055;
        } else {
            r = 12.92 * r;
        }
        if (g > rgbThreshold) {
            g = 1.055 * Math.pow(g, 1 / 2.4) - 0.055;
        } else {
            g = 12.92 * g;
        }
        if (b > rgbThreshold) {
            b = 1.055 * Math.pow(b, 1 / 2.4) - 0.055;
        } else {
            b = 12.92 * b;
        }

        r = r * 255;
        g = g * 255;
        b = b * 255;

        return Color.rgb((int) r, (int) g, (int) b);
    }

    private double[] rgbToLab(double[] rgb) {
        double r = rgb[0] / 255.0;
        double g = rgb[1] / 255.0;
        double b = rgb[2] / 255.0;

        double rgbThreshold = 0.004045;
        if (r > rgbThreshold) {
            r = Math.pow((r + 0.055) / 1.055, 2.4);
        } else {
            r = r / 12.92;
        }
        if (g > rgbThreshold) {
            g = Math.pow((g + 0.055) / 1.055, 2.4);
        } else {
            g = g / 12.92;
        }
        if (b > rgbThreshold) {
            b = Math.pow((b + 0.055) / 1.055, 2.4);
        } else {
            b = b / 12.92;
        }

        r = r * 100;
        g = g * 100;
        b = b * 100;

        double x = r * 0.4124 + g * 0.3576 + b * 0.1805;
        double y = r * 0.2126 + g * 0.7152 + b * 0.0722;
        double z = r * 0.0193 + g * 0.1192 + b * 0.9505;

        x = x / 95.047;
        y = y / 100.0;
        z = z / 108.883;

        double xyzThreshold = 0.008856;
        if (x > xyzThreshold) {
            x = Math.pow(x, 1.0 / 3);
        } else {
            x = (7.787 * x) + (16.0 / 116);
        }
        if (y > xyzThreshold) {
            y = Math.pow(y, 1.0 / 3);
        } else {
            y = (7.787 * y) + (16.0 / 116);
        }
        if (z > xyzThreshold) {
            z = Math.pow(z, 1.0 / 3);
        } else {
            z = (7.787 * z) + (16.0 / 116);
        }

        double L = (116 * y) - 16;
        double A = 500 * (x - y);
        double B = 200 * (y - z);

        return new double[]{L, A, B};
    }

}
