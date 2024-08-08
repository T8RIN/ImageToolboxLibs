#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <android/log.h>
#include <android/bitmap.h>


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_t8rin_qoi_1coder_QOIEncoder_encodeQOIBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {
    // TODO: implement encodeQOIBitmap()
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_qoi_1coder_QOIDecoder_decodeJP2ByteArray(JNIEnv *env, jobject thiz,
                                                        jbyteArray data) {
    // TODO: implement decodeJP2ByteArray()
}