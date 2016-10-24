package com.ndevev.radio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static android.media.AudioTrack.WRITE_BLOCKING;

/**
 * Created by nevi on 2016. 10. 17..
 */

public class MixerPlayer implements Runnable {
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = SAMPLE_RATE * 2 * 3;

    private ArrayList<AudioChannel> channels=new ArrayList<>();
    private AudioTrack audioTrack;
    private boolean stop=true;
    private byte[] masterBuffer=new byte[BUFFER_SIZE*2];
    private Semaphore dataReceivedSemaphore=new Semaphore(0);


    public boolean addChannel(AudioChannel audioChannel){
        setupAudioChannel(audioChannel);

        return channels.add(audioChannel);
    }

    private void setupAudioChannel(final AudioChannel audioChannel) {
        audioChannel.setEvents(new AudioDecoderEvents() {
            @Override
            public void onStart() {

            }

            @Override
            public void onDataReceived(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo, MediaFormat mediaFormat) {
                int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                if(sampleRate!=SAMPLE_RATE || channels!=2){
                    //TODO
                }

                try{
                    //synchronized(dataReceivedSemaphore) {
                        byteBuffer.rewind();
                        audioChannel.setBuffer(byteBuffer);
                        //dataReceivedSemaphore.notifyAll();

                        dataReceivedSemaphore.release();
                    //}
                    /*// wait for processing to complete
                    synchronized(audioChannel.getDataProcessedSemaphore()) {
                        audioChannel.getDataProcessedSemaphore().wait();
                    }*/
                    audioChannel.getDataProcessedSemaphore().acquire();

                    byteBuffer.clear();
                } catch (Exception e) {
                    //TODO
                }
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onError() {

            }
        });

        audioChannel.getAudioDecoder().setEventsListener(audioChannel.getEvents());
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
        int i;
        short sample;
        boolean atLeastOneBufferEmpty;
        ByteBuffer currentBuffer, audioTrackBuffer;
        int length;
        byte[] audioTrackBytes;
        int minSize = AudioTrack.getMinBufferSize( SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE*2, AudioTrack.MODE_STREAM);

        audioTrack.play();

        try {
            while (!stop) {
                length=0;
                // wait for data reveived
                //synchronized (dataReceivedSemaphore) {
                    //dataReceivedSemaphore.wait();
                    dataReceivedSemaphore.acquire();

                    for (AudioChannel channel : channels) {
                        channel.setNeedProcessing(channel.getBuffer()!=null && channel.getBuffer().remaining()>=2);
                        if(channel.getBuffer()!=null && channel.getBuffer().remaining()>length){
                            length=channel.getBuffer().remaining();
                        }
                    }
                //}

                audioTrackBytes=new byte[length];
                audioTrackBuffer=ByteBuffer.wrap(audioTrackBytes);
                audioTrackBuffer.order(ByteOrder.LITTLE_ENDIAN);

                do {
                    sample=0;
                    atLeastOneBufferEmpty=false;
                    for (AudioChannel channel : channels) {
                        if (!channel.isNeedProcessing()) continue;

                        currentBuffer=channel.getBuffer();
                        sample+=currentBuffer.getShort();

                        if(currentBuffer.remaining()<2){
                            atLeastOneBufferEmpty=true;

                            /*synchronized(channel.getDataProcessedSemaphore()) {
                                channel.getDataProcessedSemaphore().notifyAll();
                            }*/
                            channel.getDataProcessedSemaphore().release();
                        }
                    }

                    audioTrackBuffer.putShort(sample);


                }while(!atLeastOneBufferEmpty);

                length=audioTrackBuffer.position();
                audioTrackBuffer.rewind();

                audioTrack.write(audioTrackBuffer, length, WRITE_BLOCKING);
                audioTrackBuffer.rewind();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if(audioTrack != null) {
            audioTrack.flush();
            audioTrack.release();
            audioTrack = null;
        }
    }
}
