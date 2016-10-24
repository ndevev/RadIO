package com.ndevev.radio;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * Created by nevi on 2016. 10. 17..
 */

public class AudioChannel {

    public boolean isNeedProcessing() {
        return needProcessing;
    }

    public void setNeedProcessing(boolean needProcessing) {
        this.needProcessing = needProcessing;
    }

    private boolean needProcessing;

    public Semaphore getDataProcessedSemaphore() {
        return dataProcessedSemaphore;
    }

    private Semaphore dataProcessedSemaphore=new Semaphore(0);

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    private ByteBuffer buffer;

    public int getProcessedByteCount() {
        return processedByteCount;
    }

    public void setProcessedByteCount(int processedByteCount) {
        this.processedByteCount = processedByteCount;
    }

    private int processedByteCount;

    public AudioDecoder getAudioDecoder() {
        return audioDecoder;
    }

    private AudioDecoder audioDecoder;

    public AudioDecoderEvents getEvents() {
        return events;
    }

    public void setEvents(AudioDecoderEvents events) {
        this.events = events;
    }

    private AudioDecoderEvents events;

    public AudioChannel(AudioDecoder audioDecoder) {
        this.audioDecoder = audioDecoder;
    }
}
