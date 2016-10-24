package com.ndevev.radio;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * Created by nevi on 2016. 10. 16..
 */

public class AudioDecoder implements Runnable{
    private MediaExtractor extractor;
    long presentationTimeUs = 0, duration = 0;
    private boolean stop = true;

    AudioDecoderEvents events;
    private String dataSource;

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setEventsListener(AudioDecoderEvents events) {
        this.events = events;
    }

    public void play() {
        if (stop) {
            stop = false;
            new Thread(this).start();
        }
    }

    public void stop() {
        stop = true;
    }

    @Override
    public void run() {
        MediaCodec codec = null;
        String mime = null;
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // extractor gets information about the stream
            extractor = new MediaExtractor();
            extractor.setDataSource(dataSource);
            extractor.selectTrack(0);

            // Read track header
            MediaFormat format = extractor.getTrackFormat(0);
            mime = format.getString(MediaFormat.KEY_MIME);
            if (format == null || !mime.startsWith("audio/")) {
                if (events != null) events.onError();
                return;
            }
            codec = MediaCodec.createDecoderByType(mime);
            final MediaCodec finalCodec = codec;

            final Object endSemaphore=new Object();

            codec.setCallback(new MediaCodec.Callback() {
                private MediaFormat bufferFormat;
                private ByteBuffer inputBuffer;
                private ByteBuffer outputBuffer;

                @Override
                public void onInputBufferAvailable(MediaCodec mc, int inputBufferId) {

                    inputBuffer = finalCodec.getInputBuffer(inputBufferId);

                    boolean end=false;
                    int sampleSize;
                    if(!stop) {
                        sampleSize = extractor.readSampleData(inputBuffer, 0);
                        if (sampleSize < 0) {
                            end = true;
                            sampleSize = 0;
                        } else {
                            presentationTimeUs = extractor.getSampleTime();
                            extractor.advance();
                        }
                    }else{
                        end = true;
                        sampleSize = 0;
                    }

                    finalCodec.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, end ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                }

                @Override
                public void onOutputBufferAvailable(MediaCodec mediaCodec, int outputBufferId, MediaCodec.BufferInfo bufferInfo) {
                    if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                        synchronized(endSemaphore) {
                            endSemaphore.notify();
                        }
                        return;
                    }

                    outputBuffer = finalCodec.getOutputBuffer(outputBufferId);
                    bufferFormat = finalCodec.getOutputFormat(outputBufferId); // option A

                    /*final byte[] chunk = new byte[bufferInfo.size];
                    outputBuffer.get(chunk);
                    outputBuffer.clear();*/

                    if(events != null) events.onDataReceived(outputBuffer, bufferInfo, bufferFormat);

                    finalCodec.releaseOutputBuffer(outputBufferId, false);
                }

                @Override
                public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
                    endSemaphore.notify();
                }

                @Override
                public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) {

                }


            });
            codec.configure(format, null, null, 0);
            if (events != null) events.onStart();
            codec.start();

            // wait for processing to complete
            synchronized(endSemaphore) {
                endSemaphore.wait();
            }

            codec.stop();
            codec.release();
        } catch (Exception e) {
            if (events != null) events.onError();
        }

        if (events != null) events.onStop();
    }
}
