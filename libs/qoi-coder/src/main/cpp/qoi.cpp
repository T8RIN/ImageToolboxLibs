//
// Created by malik on 08.08.2024.
//

#include "qoi.h" {

#include <stdlib.h>
#include <string.h>

#ifndef QOI_MALLOC
#define QOI_MALLOC(sz) malloc(sz)
#define QOI_FREE(p)    free(p)
#endif
#ifndef QOI_ZEROARR
#define QOI_ZEROARR(a) memset((a),0,sizeof(a))
#endif

#define QOI_OP_INDEX  0x00 /* 00xxxxxx */
#define QOI_OP_DIFF   0x40 /* 01xxxxxx */
#define QOI_OP_LUMA   0x80 /* 10xxxxxx */
#define QOI_OP_RUN    0xc0 /* 11xxxxxx */
#define QOI_OP_RGB    0xfe /* 11111110 */
#define QOI_OP_RGBA   0xff /* 11111111 */

#define QOI_MASK_2    0xc0 /* 11000000 */

#define QOI_COLOR_HASH(C) (C.rgba.r*3 + C.rgba.g*5 + C.rgba.b*7 + C.rgba.a*11)
#define QOI_MAGIC \
    (((unsigned int)'q') << 24 | ((unsigned int)'o') << 16 | \
     ((unsigned int)'i') <<  8 | ((unsigned int)'f'))
#define QOI_HEADER_SIZE 14

/* 2GB is the max file size that this implementation can safely handle. We guard
against anything larger than that, assuming the worst case with 5 bytes per
pixel, rounded down to a nice clean value. 400 million pixels ought to be
enough for anybody. */
#define QOI_PIXELS_MAX ((unsigned int)400000000)

typedef union {
    struct {
        unsigned char r, g, b, a;
    } rgba;
    unsigned int v;
} qoi_rgba_t;

static const unsigned char qoi_padding[8] = {0, 0, 0, 0, 0, 0, 0, 1};

static void qoi_write_32(unsigned char *bytes, int *p, unsigned int v) {
    bytes[(*p)++] = (0xff000000 & v) >> 24;
    bytes[(*p)++] = (0x00ff0000 & v) >> 16;
    bytes[(*p)++] = (0x0000ff00 & v) >> 8;
    bytes[(*p)++] = (0x000000ff & v);
}

static unsigned int qoi_read_32(const unsigned char *bytes, int *p) {
    unsigned int a = bytes[(*p)++];
    unsigned int b = bytes[(*p)++];
    unsigned int c = bytes[(*p)++];
    unsigned int d = bytes[(*p)++];
    return a << 24 | b << 16 | c << 8 | d;
}

void *qoi_encode(const void *data, const qoi_desc *desc, int *out_len) {
    int i, max_size, p, run;
    int px_len, px_end, px_pos, channels;
    unsigned char *bytes;
    const unsigned char *pixels;
    qoi_rgba_t index[64];
    qoi_rgba_t px, px_prev;

    if (
            data == NULL || out_len == NULL || desc == NULL ||
            desc->width == 0 || desc->height == 0 ||
            desc->channels < 3 || desc->channels > 4 ||
            desc->colorspace > 1 ||
            desc->height >= QOI_PIXELS_MAX / desc->width
            ) {
        return NULL;
    }

    max_size =
            desc->width * desc->height * (desc->channels + 1) +
            QOI_HEADER_SIZE + sizeof(qoi_padding);

    p = 0;
    bytes = (unsigned char *) QOI_MALLOC(max_size);
    if (!bytes) {
        return NULL;
    }

    qoi_write_32(bytes, &p, QOI_MAGIC);
    qoi_write_32(bytes, &p, desc->width);
    qoi_write_32(bytes, &p, desc->height);
    bytes[p++] = desc->channels;
    bytes[p++] = desc->colorspace;


    pixels = (const unsigned char *) data;

    QOI_ZEROARR(index);

    run = 0;
    px_prev.rgba.r = 0;
    px_prev.rgba.g = 0;
    px_prev.rgba.b = 0;
    px_prev.rgba.a = 255;
    px = px_prev;

    px_len = desc->width * desc->height * desc->channels;
    px_end = px_len - desc->channels;
    channels = desc->channels;

    for (px_pos = 0; px_pos < px_len; px_pos += channels) {
        px.rgba.r = pixels[px_pos + 0];
        px.rgba.g = pixels[px_pos + 1];
        px.rgba.b = pixels[px_pos + 2];

        if (channels == 4) {
            px.rgba.a = pixels[px_pos + 3];
        }

        if (px.v == px_prev.v) {
            run++;
            if (run == 62 || px_pos == px_end) {
                bytes[p++] = QOI_OP_RUN | (run - 1);
                run = 0;
            }
        } else {
            int index_pos;

            if (run > 0) {
                bytes[p++] = QOI_OP_RUN | (run - 1);
                run = 0;
            }

            index_pos = QOI_COLOR_HASH(px) % 64;

            if (index[index_pos].v == px.v) {
                bytes[p++] = QOI_OP_INDEX | index_pos;
            } else {
                index[index_pos] = px;

                if (px.rgba.a == px_prev.rgba.a) {
                    signed char vr = px.rgba.r - px_prev.rgba.r;
                    signed char vg = px.rgba.g - px_prev.rgba.g;
                    signed char vb = px.rgba.b - px_prev.rgba.b;

                    signed char vg_r = vr - vg;
                    signed char vg_b = vb - vg;

                    if (
                            vr > -3 && vr < 2 &&
                            vg > -3 && vg < 2 &&
                            vb > -3 && vb < 2
                            ) {
                        bytes[p++] = QOI_OP_DIFF | (vr + 2) << 4 | (vg + 2) << 2 | (vb + 2);
                    } else if (
                            vg_r > -9 && vg_r < 8 &&
                            vg > -33 && vg < 32 &&
                            vg_b > -9 && vg_b < 8
                            ) {
                        bytes[p++] = QOI_OP_LUMA | (vg + 32);
                        bytes[p++] = (vg_r + 8) << 4 | (vg_b + 8);
                    } else {
                        bytes[p++] = QOI_OP_RGB;
                        bytes[p++] = px.rgba.r;
                        bytes[p++] = px.rgba.g;
                        bytes[p++] = px.rgba.b;
                    }
                } else {
                    bytes[p++] = QOI_OP_RGBA;
                    bytes[p++] = px.rgba.r;
                    bytes[p++] = px.rgba.g;
                    bytes[p++] = px.rgba.b;
                    bytes[p++] = px.rgba.a;
                }
            }
        }
        px_prev = px;
    }

    for (i = 0; i < (int) sizeof(qoi_padding); i++) {
        bytes[p++] = qoi_padding[i];
    }

    *out_len = p;
    return bytes;
}

void *qoi_decode(const void *data, int size, qoi_desc *desc, int channels) {
    const unsigned char *bytes;
    unsigned int header_magic;
    unsigned char *pixels;
    qoi_rgba_t index[64];
    qoi_rgba_t px;
    int px_len, chunks_len, px_pos;
    int p = 0, run = 0;

    if (
            data == NULL || desc == NULL ||
            (channels != 0 && channels != 3 && channels != 4) ||
            size < QOI_HEADER_SIZE + (int) sizeof(qoi_padding)
            ) {
        return NULL;
    }

    bytes = (const unsigned char *) data;

    header_magic = qoi_read_32(bytes, &p);
    desc->width = qoi_read_32(bytes, &p);
    desc->height = qoi_read_32(bytes, &p);
    desc->channels = bytes[p++];
    desc->colorspace = bytes[p++];

    if (
            desc->width == 0 || desc->height == 0 ||
            desc->channels < 3 || desc->channels > 4 ||
            desc->colorspace > 1 ||
            header_magic != QOI_MAGIC ||
            desc->height >= QOI_PIXELS_MAX / desc->width
            ) {
        return NULL;
    }

    if (channels == 0) {
        channels = desc->channels;
    }

    px_len = desc->width * desc->height * channels;
    pixels = (unsigned char *) QOI_MALLOC(px_len);
    if (!pixels) {
        return NULL;
    }

    QOI_ZEROARR(index);
    px.rgba.r = 0;
    px.rgba.g = 0;
    px.rgba.b = 0;
    px.rgba.a = 255;

    chunks_len = size - (int) sizeof(qoi_padding);
    for (px_pos = 0; px_pos < px_len; px_pos += channels) {
        if (run > 0) {
            run--;
        } else if (p < chunks_len) {
            int b1 = bytes[p++];

            if (b1 == QOI_OP_RGB) {
                px.rgba.r = bytes[p++];
                px.rgba.g = bytes[p++];
                px.rgba.b = bytes[p++];
            } else if (b1 == QOI_OP_RGBA) {
                px.rgba.r = bytes[p++];
                px.rgba.g = bytes[p++];
                px.rgba.b = bytes[p++];
                px.rgba.a = bytes[p++];
            } else if ((b1 & QOI_MASK_2) == QOI_OP_INDEX) {
                px = index[b1];
            } else if ((b1 & QOI_MASK_2) == QOI_OP_DIFF) {
                px.rgba.r += ((b1 >> 4) & 0x03) - 2;
                px.rgba.g += ((b1 >> 2) & 0x03) - 2;
                px.rgba.b += (b1 & 0x03) - 2;
            } else if ((b1 & QOI_MASK_2) == QOI_OP_LUMA) {
                int b2 = bytes[p++];
                int vg = (b1 & 0x3f) - 32;
                px.rgba.r += vg - 8 + ((b2 >> 4) & 0x0f);
                px.rgba.g += vg;
                px.rgba.b += vg - 8 + (b2 & 0x0f);
            } else if ((b1 & QOI_MASK_2) == QOI_OP_RUN) {
                run = (b1 & 0x3f);
            }

            index[QOI_COLOR_HASH(px) % 64] = px;
        }

        pixels[px_pos + 0] = px.rgba.r;
        pixels[px_pos + 1] = px.rgba.g;
        pixels[px_pos + 2] = px.rgba.b;

        if (channels == 4) {
            pixels[px_pos + 3] = px.rgba.a;
        }
    }

    return pixels;
}

#ifndef QOI_NO_STDIO

#include <stdio.h>

#endif

int qoi_write(const char *filename, const void *data, const qoi_desc *desc) {
    FILE *f = fopen(filename, "wb");
    int size, err;
    void *encoded;

    if (!f) {
        return 0;
    }

    encoded = qoi_encode(data, desc, &size);
    if (!encoded) {
        fclose(f);
        return 0;
    }

    fwrite(encoded, 1, size, f);
    fflush(f);
    err = ferror(f);
    fclose(f);

    QOI_FREE(encoded);
    return err ? 0 : size;
}

void *qoi_read(const char *filename, qoi_desc *desc, int channels) {
    FILE *f = fopen(filename, "rb");
    int size, bytes_read;
    void *pixels, *data;

    if (!f) {
        return NULL;
    }

    fseek(f, 0, SEEK_END);
    size = ftell(f);
    if (size <= 0 || fseek(f, 0, SEEK_SET) != 0) {
        fclose(f);
        return NULL;
    }

    data = QOI_MALLOC(size);
    if (!data) {
        fclose(f);
        return NULL;
    }

    bytes_read = fread(data, 1, size, f);
    fclose(f);
    pixels = (bytes_read != size) ? NULL : qoi_decode(data, bytes_read, desc, channels);
    QOI_FREE(data);
    return pixels;
}
