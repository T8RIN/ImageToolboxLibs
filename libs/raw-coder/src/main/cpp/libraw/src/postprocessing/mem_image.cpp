/* -*- C++ -*-
 * Copyright 2019-2025 LibRaw LLC (info@libraw.org)
 *
 LibRaw is free software; you can redistribute it and/or modify
 it under the terms of the one of two licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

 */

#include "../../internal/libraw_cxx_defs.h"

libraw_processed_image_t *LibRaw::dcraw_make_mem_thumb(int *errcode) {
    if (!T.thumb) {
        if (!ID.toffset && !(imgdata.thumbnail.tlength > 0 &&
                load_raw == &LibRaw::broadcom_load_raw) // RPi
                ) {
            if (errcode)
                *errcode = LIBRAW_NO_THUMBNAIL;
        } else {
            if (errcode)
                *errcode = LIBRAW_OUT_OF_ORDER_CALL;
        }
        return NULL;
    }

    if (T.tlength < 64u) {
        if (errcode)
            *errcode = EINVAL;
        return NULL;
    }

    if (INT64(T.tlength) > 1024ULL * 1024ULL * LIBRAW_MAX_THUMBNAIL_MB) {
        if (errcode)
            *errcode = LIBRAW_TOO_BIG;
        return NULL;
    }

    if (T.tformat == LIBRAW_THUMBNAIL_BITMAP) {
        libraw_processed_image_t *ret = (libraw_processed_image_t *) ::malloc(
                sizeof(libraw_processed_image_t) + T.tlength);

        if (!ret) {
            if (errcode)
                *errcode = ENOMEM;
            return NULL;
        }

        memset(ret, 0, sizeof(libraw_processed_image_t));
        ret->type = LIBRAW_IMAGE_BITMAP;
        ret->height = T.theight;
        ret->width = T.twidth;
        if (T.tcolors > 0 && T.tcolors < 4)
            ret->colors = T.tcolors;
        else
            ret->colors = 3; // defaults
        ret->bits = 8;
        ret->data_size = T.tlength;
        memmove(ret->data, T.thumb, T.tlength);
        if (errcode)
            *errcode = 0;
        return ret;
    } else if (T.tformat == LIBRAW_THUMBNAIL_JPEG) {
        ushort exif[5];
        int mk_exif = 0;
        if (memcmp(T.thumb + 6, "Exif\0", 5))
            mk_exif = 1;

        int dsize = T.tlength + mk_exif * (sizeof(exif) + sizeof(tiff_hdr));

        libraw_processed_image_t *ret = (libraw_processed_image_t *) ::malloc(
                sizeof(libraw_processed_image_t) + dsize);

        if (!ret) {
            if (errcode)
                *errcode = ENOMEM;
            return NULL;
        }

        memset(ret, 0, sizeof(libraw_processed_image_t));

        ret->type = LIBRAW_IMAGE_JPEG;
        ret->data_size = dsize;

        ret->data[0] = 0xff;
        ret->data[1] = 0xd8;
        if (mk_exif) {
            struct tiff_hdr th;
            memcpy(exif, "\xff\xe1  Exif\0\0", 10);
            exif[1] = htons(8 + sizeof th);
            memmove(ret->data + 2, exif, sizeof(exif));
            tiff_head(&th, 0);
            memmove(ret->data + (2 + sizeof(exif)), &th, sizeof(th));
            memmove(ret->data + (2 + sizeof(exif) + sizeof(th)), T.thumb + 2,
                    T.tlength - 2);
        } else {
            memmove(ret->data + 2, T.thumb + 2, T.tlength - 2);
        }
        if (errcode)
            *errcode = 0;
        return ret;
    } else if (T.tformat == LIBRAW_THUMBNAIL_H265 || T.tformat == LIBRAW_THUMBNAIL_JPEGXL) {
        int dsize = T.tlength;
        libraw_processed_image_t *ret = (libraw_processed_image_t *) ::malloc(sizeof(libraw_processed_image_t) + dsize);
        if (!ret) {
            if (errcode)
                *errcode = ENOMEM;
            return NULL;
        }
        memset(ret, 0, sizeof(libraw_processed_image_t));
        ret->type = T.tformat == LIBRAW_THUMBNAIL_H265 ? LIBRAW_IMAGE_H265 : LIBRAW_IMAGE_JPEGXL;
        ret->data_size = dsize;
        memmove(ret->data, T.thumb, dsize);
        if (errcode)
            *errcode = 0;
        return ret;
    } else {
        if (errcode)
            *errcode = LIBRAW_UNSUPPORTED_THUMBNAIL;
        return NULL;
    }
}

// jlb
// macros for copying pixels to either BGR or RGB formats
#define FORBGR for (c = P1.colors - 1; c >= 0; c--)
#define FORRGB for (c = 0; c < P1.colors; c++)

void LibRaw::get_mem_image_format(int *width, int *height, int *colors,
        int *bps) const {
    *width = S.width;
    *height = S.height;
    if (imgdata.progress_flags < LIBRAW_PROGRESS_FUJI_ROTATE) {
        if (O.use_fuji_rotate) {
            if (IO.fuji_width) {
                int fuji_width = (IO.fuji_width - 1 + IO.shrink) >> IO.shrink;
                *width = (ushort) (fuji_width / sqrt(0.5));
                *height = (ushort) ((*height - fuji_width) / sqrt(0.5));
            } else {
                if (S.pixel_aspect < 0.995)
                    *height = (ushort) (*height / S.pixel_aspect + 0.5);
                if (S.pixel_aspect > 1.005)
                    *width = (ushort) (*width * S.pixel_aspect + 0.5);
            }
        }
    }
    if (S.flip & 4) {
        std::swap(*width, *height);
    }
    *colors = P1.colors;
    *bps = O.output_bps;
}

int LibRaw::copy_mem_image(void *scan0, int stride, int bgr) {
    // the image memory pointed to by scan0 is assumed to be in the format
    // returned by get_mem_image_format
    if ((imgdata.progress_flags & LIBRAW_PROGRESS_THUMB_MASK) <
            LIBRAW_PROGRESS_PRE_INTERPOLATE)
        return LIBRAW_OUT_OF_ORDER_CALL;

    if (libraw_internal_data.output_data.histogram) {
        int perc, val, total, t_white = 0x2000, c;
        perc = int(S.width * S.height * O.auto_bright_thr);
        if (IO.fuji_width)
            perc /= 2;
        if (!((O.highlight & ~2) || O.no_auto_bright))
            for (t_white = c = 0; c < P1.colors; c++) {
                for (val = 0x2000, total = 0; --val > 32;)
                    if ((total += libraw_internal_data.output_data.histogram[c][val]) >
                            perc)
                        break;
                if (t_white < val)
                    t_white = val;
            }
        gamma_curve(O.gamm[0], O.gamm[1], 2, int((t_white << 3) / O.bright));
    }

    int s_iheight = S.iheight;
    int s_iwidth = S.iwidth;
    int s_width = S.width;
    int s_hwight = S.height;

    S.iheight = S.height;
    S.iwidth = S.width;

    if (S.flip & 4) SWAP(S.height, S.width);
    uchar *ppm;
    ushort *ppm2;
    int c, row, col, soff, rstep, cstep;

    soff = flip_index(0, 0);
    cstep = flip_index(0, 1) - soff;
    rstep = flip_index(1, 0) - flip_index(0, S.width);

    for (row = 0; row < S.height; row++, soff += rstep) {
        uchar *bufp = ((uchar *) scan0) + size_t(row) * size_t(stride);
        ppm2 = (ushort *) (ppm = bufp);
        // keep trivial decisions in the outer loop for speed
        if (bgr) {
            if (O.output_bps == 8) {
                for (col = 0; col < S.width; col++, soff += cstep)
                    FORBGR *ppm++ = imgdata.color.curve[imgdata.image[soff][c]] >> 8;
            } else {
                for (col = 0; col < S.width; col++, soff += cstep)
                    FORBGR *ppm2++ = imgdata.color.curve[imgdata.image[soff][c]];
            }
        } else {
            if (O.output_bps == 8) {
                for (col = 0; col < S.width; col++, soff += cstep)
                    FORRGB *ppm++ = imgdata.color.curve[imgdata.image[soff][c]] >> 8;
            } else {
                for (col = 0; col < S.width; col++, soff += cstep)
                    FORRGB *ppm2++ = imgdata.color.curve[imgdata.image[soff][c]];
            }
        }

        //            bufp += stride;           // go to the next line
    }

    S.iheight = s_iheight;
    S.iwidth = s_iwidth;
    S.width = s_width;
    S.height = s_hwight;

    return 0;
}

int LibRaw::copy_mem_image_rgba(void *scan0, int stride, int half_float,
        int target_width, int target_height, int orientation) {
    if ((imgdata.progress_flags & LIBRAW_PROGRESS_THUMB_MASK) <
            LIBRAW_PROGRESS_PRE_INTERPOLATE)
        return LIBRAW_OUT_OF_ORDER_CALL;
    if (P1.colors != 1 && (P1.colors < 3 || P1.colors > 4))
        return LIBRAW_FILE_UNSUPPORTED;
    if (target_width < 1 || target_height < 1 || orientation < 0 || orientation > 7)
        return EINVAL;

    if (libraw_internal_data.output_data.histogram) {
        int perc, val, total, t_white = 0x2000;
        perc = int(S.width * S.height * O.auto_bright_thr);
        if (IO.fuji_width)
            perc /= 2;
        if (!((O.highlight & ~2) || O.no_auto_bright))
            for (int c = 0; c < P1.colors; c++) {
                for (val = 0x2000, total = 0; --val > 32;)
                    if ((total += libraw_internal_data.output_data.histogram[c][val]) >
                            perc)
                        break;
                if (t_white < val)
                    t_white = val;
            }
        gamma_curve(O.gamm[0], O.gamm[1], 2, int((t_white << 3) / O.bright));
    }

    const int source_width = S.width;
    const int source_height = S.height;
    const int oriented_width = orientation & 4 ? source_height : source_width;
    const int oriented_height = orientation & 4 ? source_width : source_height;
    const auto source_coordinates = [source_width, source_height, orientation](
            float x, float y, float &source_x, float &source_y) {
        switch (orientation) {
            case 1:
                source_x = source_width - 1 - x;
                source_y = y;
                break;
            case 2:
                source_x = x;
                source_y = source_height - 1 - y;
                break;
            case 3:
                source_x = source_width - 1 - x;
                source_y = source_height - 1 - y;
                break;
            case 4:
                source_x = y;
                source_y = x;
                break;
            case 5:
                source_x = source_width - 1 - y;
                source_y = x;
                break;
            case 6:
                source_x = y;
                source_y = source_height - 1 - x;
                break;
            case 7:
                source_x = source_width - 1 - y;
                source_y = source_height - 1 - x;
                break;
            default:
                source_x = x;
                source_y = y;
                break;
        }
    };
    const auto sample = [this, source_width, source_height](
            float x, float y, int component) {
        if (P1.colors == 1) component = 0;
        x = LIM(x, 0.0f, static_cast<float>(source_width - 1));
        y = LIM(y, 0.0f, static_cast<float>(source_height - 1));
        const int x0 = static_cast<int>(x);
        const int y0 = static_cast<int>(y);
        const int x1 = MIN(x0 + 1, source_width - 1);
        const int y1 = MIN(y0 + 1, source_height - 1);
        const float x_weight = x - x0;
        const float y_weight = y - y0;
        const auto value = [this, source_width, component](int px, int py) {
            return static_cast<float>(
                    imgdata.color.curve[imgdata.image[py * source_width + px][component]]);
        };
        const float top = value(x0, y0) + (value(x1, y0) - value(x0, y0)) * x_weight;
        const float bottom = value(x0, y1) +
                (value(x1, y1) - value(x0, y1)) * x_weight;
        return top + (bottom - top) * y_weight;
    };

    for (int row = 0; row < target_height; ++row) {
        auto *target8 = static_cast<uchar *>(scan0) + size_t(row) * size_t(stride);
        auto *target16 = reinterpret_cast<ushort *>(target8);
        for (int column = 0; column < target_width; ++column) {
            const float oriented_x = (column + 0.5f) * oriented_width / target_width - 0.5f;
            const float oriented_y = (row + 0.5f) * oriented_height / target_height - 0.5f;
            float source_x;
            float source_y;
            source_coordinates(oriented_x, oriented_y, source_x, source_y);
            if (half_float) {
                for (int component = 0; component < 3; ++component) {
                    const _Float16 value = static_cast<_Float16>(
                            sample(source_x, source_y, component) / 65535.0f);
                    memcpy(target16++, &value, sizeof(value));
                }
                const _Float16 alpha = static_cast<_Float16>(1.0f);
                memcpy(target16++, &alpha, sizeof(alpha));
            } else {
                for (int component = 0; component < 3; ++component)
                    *target8++ = static_cast<ushort>(
                            sample(source_x, source_y, component) + 0.5f) >> 8;
                *target8++ = 255;
            }
        }
    }
    return LIBRAW_SUCCESS;
}

#undef FORBGR
#undef FORRGB

libraw_processed_image_t *LibRaw::dcraw_make_mem_image(int *errcode) {
    int width, height, colors, bps;
    get_mem_image_format(&width, &height, &colors, &bps);
    int stride = width * (bps / 8) * colors;
    INT64 ds = INT64(height) * INT64(stride);
    libraw_processed_image_t *ret = (libraw_processed_image_t *) ::malloc(
            sizeof(libraw_processed_image_t) + ds);
    if (!ret) {
        if (errcode)
            *errcode = ENOMEM;
        return NULL;
    }
    memset(ret, 0, sizeof(libraw_processed_image_t));

    // metadata init
    ret->type = LIBRAW_IMAGE_BITMAP;
    ret->height = height;
    ret->width = width;
    ret->colors = colors;
    ret->bits = bps;
    ret->data_size = ds;
    copy_mem_image(ret->data, stride, 0);

    return ret;
}

void LibRaw::dcraw_clear_mem(libraw_processed_image_t *p) {
    if (p)
        ::free(p);
}
