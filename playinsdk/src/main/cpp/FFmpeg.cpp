//
// Created by zhangliucheng on 2019-09-09.
//

#include "FFmpeg.h"
#include "PILog.h"

FFmpeg::FFmpeg() {
};


FFmpeg::~FFmpeg() {
}

int FFmpeg::init(JNIEnv *env, jobject instance, jint width, jint height, jobject surface) {

    av_register_all();
    pCodec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if (pCodec == NULL) {
//        LOGE("%s","无法解码");
        return -1;
    }
    pCodecCtx = avcodec_alloc_context3(pCodec);
    pCodecCtx->frame_number = 1;
    pCodecCtx->width = width;
    pCodecCtx->height = height;
    pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;

    int ret = avcodec_open2(pCodecCtx, pCodec, NULL);
    if (ret < 0) {
//        LOGE("%s","打开解码器失败  " + ret);
        return -1;
    }

    ANativeWindow *nw = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_setBuffersGeometry(nw, pCodecCtx->width, pCodecCtx->height,
                                     WINDOW_FORMAT_RGBA_8888);
    nativeWindow = nw;

    sws_ctx = sws_getContext(pCodecCtx->width,
                             pCodecCtx->height,
                             AV_PIX_FMT_YUV420P,
                             pCodecCtx->width,
                             pCodecCtx->height,
                             AV_PIX_FMT_RGBA,
                             SWS_BILINEAR,
                             NULL,
                             NULL,
                             NULL);
    return 0;
}

int FFmpeg::decoding(JNIEnv *env, jobject instance, jbyteArray data) {
    jsize len = env->GetArrayLength(data);
    jbyte *jbarray = env->GetByteArrayElements(data, 0);
    AVPacket packet;
    av_new_packet(&packet, len);
    memcpy(packet.data, jbarray, len);

    int videoWidth = pCodecCtx->width;
    int videoHeight = pCodecCtx->height;

    AVFrame *pFrame = av_frame_alloc();
    AVFrame *pFrameRGBA = av_frame_alloc();
    if (pFrameRGBA == NULL || pFrame == NULL) {
        return -1;
    }

    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, pCodecCtx->width, pCodecCtx->height, 1);
    uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize, buffer, AV_PIX_FMT_RGBA,
                         pCodecCtx->width, pCodecCtx->height, 1);

    avcodec_decode_video2(pCodecCtx, pFrame, &got_frame, &packet);

    if (got_frame && nativeWindow != NULL) {
        ANativeWindow_lock(nativeWindow, &windowBuffer, 0);
        sws_scale(sws_ctx, (uint8_t const *const *) pFrame->data,
                  pFrame->linesize, 0, pCodecCtx->height,
                  pFrameRGBA->data, pFrameRGBA->linesize);

        uint8_t *dst = (uint8_t *) windowBuffer.bits;
        int dstStride = windowBuffer.stride * 4;
        uint8_t *src = (uint8_t *) (pFrameRGBA->data[0]);
        int srcStride = pFrameRGBA->linesize[0];

        int h;
        for (h = 0; h < videoHeight; h++) {
            memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
        }

        ANativeWindow_unlockAndPost(nativeWindow);

    }
    av_free(buffer);
    av_free(pFrameRGBA);
    av_free(pFrame);
    av_free_packet(&packet);
    env->ReleaseByteArrayElements(data, jbarray, 0);
    return got_frame;
}

void FFmpeg::close() {
    if (NULL != pCodecCtx) {
        avcodec_close(pCodecCtx);
        avcodec_free_context(&pCodecCtx);
    }
    if (NULL != sws_ctx) {
        sws_freeContext(sws_ctx);
    }

    if (NULL != nativeWindow) {
        ANativeWindow_release(nativeWindow);
    }
}

void FFmpeg::updateSurface(JNIEnv *env, jobject surface) {
    ANativeWindow *nw = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_setBuffersGeometry(nw, pCodecCtx->width, pCodecCtx->height,
                                     WINDOW_FORMAT_RGBA_8888);
    nativeWindow = nw;
}
