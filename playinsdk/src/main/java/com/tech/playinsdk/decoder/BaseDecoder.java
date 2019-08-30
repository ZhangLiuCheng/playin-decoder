package com.tech.playinsdk.decoder;

import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BaseDecoder implements Runnable {

    public interface DecoderListener {
        void decoderSuccess();
    }


    private BlockingQueue<byte[]> videoQueue = new LinkedBlockingQueue<>(30);

    private int videoWidth;
    private int videoHeight;

    private boolean initCodec;
    private boolean decodeSuccess;
    private DecoderListener decoderListener;

    protected abstract boolean initDecoder(int videoWidth, int videoHeight, Surface surface);
    protected abstract void onFrame(byte[] buf, int offset, int length);
    protected abstract void releaseDecoder();

    public BaseDecoder(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    public void setDecoderListener(DecoderListener decoderListener) {
        this.decoderListener = decoderListener;
    }

    @Override
    public void run() {
        while (initCodec) {
            try {
                byte[] buf = videoQueue.take();
                onFrame(buf, 0, buf.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void setDisplayHolder(SurfaceHolder holder) {
        initDecoder(this.videoWidth, this.videoHeight, holder.getSurface());
    }

    public void sendVideoData(byte[] buf) {
//        int value = buf[4] & 0x0f;
        // 使用ios视频流调试
//        if (true) {
//            buf[0] = 0;
//            buf[1] = 0;
//            buf[2] = 0;
//            buf[3] = 1;
//            if (value == 7 || value == 8) {
//                buf[0] = 0;
//                buf[1] = 0;
//                buf[2] = 0;
//                buf[3] = 1;
//                buf[14] = 0;
//                buf[15] = 0;
//                buf[16] = 0;
//                buf[17] = 1;
//            }
//        }

        if (null != videoQueue) {
            boolean b = videoQueue.offer(buf);
//            PILog.e(" -----> 将视频流添加到解码队列里： " + b);
        }
    }

    public synchronized void start(Surface surface) {
        if (initDecoder(this.videoWidth, this.videoHeight, surface)) {
            initCodec = true;
            new Thread(this).start();
        }
    }

    public synchronized void stop() {
        videoQueue.clear();
        initCodec = false;
        decodeSuccess = false;
        releaseDecoder();
    }

    protected boolean tryCodecSuccess(int value) {
        if (decodeSuccess == false) {
            if (value == 7 || value == 8) {
                decodeSuccess = true;
                if (null != decoderListener) {
                    decoderListener.decoderSuccess();
                }
            }
        }
        return decodeSuccess;
    }
}
