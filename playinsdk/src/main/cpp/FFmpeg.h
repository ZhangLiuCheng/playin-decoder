//
// Created by zhangliucheng on 2019-09-09.
//

#ifndef PLAYINDECODER_FFMPEG_H
#define PLAYINDECODER_FFMPEG_H

#include <android/native_window_jni.h>
#include <android/native_window.h>

extern "C" {
#include "ffmpeg/include/libavcodec/avcodec.h"
#include "ffmpeg/include/libavformat/avformat.h"
#include "ffmpeg/include/libswscale/swscale.h"
#include "ffmpeg/include/libavutil/avutil.h"
#include "ffmpeg/include/libavutil/frame.h"
#include "ffmpeg/include/libavutil/imgutils.h"
}

class FFmpeg {

public:
    ANativeWindow *nativeWindow;
    ANativeWindow_Buffer windowBuffer;
    AVCodec *pCodec;
    AVCodecContext *pCodecCtx;
    struct SwsContext *sws_ctx;
    int len, got_frame;

public:
    FFmpeg();
    ~FFmpeg();
    int init(JNIEnv *env, jobject instance, jint width, jint height, jobject surface);
    int decoding(JNIEnv *env, jobject instance, jbyteArray data);
    void close();
    void updateSurface(JNIEnv *env, jobject surface);
};


#endif //PLAYINDECODER_FFMPEG_H
