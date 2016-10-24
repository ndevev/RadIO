package com.ndevev.radio;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * Created by nevi on 2016. 10. 16..
 */

public interface AudioDecoderEvents {
    void onStart();
    void onDataReceived(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo, MediaFormat mediaFormat);
    void onStop();
    void onError();
}
