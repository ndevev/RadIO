package com.ndevev.radio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.pocketmagic.android.openmxplayer.OpenMXPlayer;
import net.pocketmagic.android.openmxplayer.PlayerEvents;

public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private Button addButton;
    private OpenMXPlayer mPlayer;
    private MixerPlayer mixerPlayer;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.test_button);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AudioDecoder audioDecoder=new AudioDecoder();
                audioDecoder.setDataSource("/sdcard/Download/test.mp3");

                AudioChannel audioChannel=new AudioChannel(audioDecoder);

                mixerPlayer=new MixerPlayer();
                mixerPlayer.addChannel(audioChannel);
                mixerPlayer.play();

                audioDecoder.play();
            }
        });

        addButton = (Button) findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AudioDecoder audioDecoder=new AudioDecoder();
                audioDecoder.setDataSource("/sdcard/Download/test.mp3");

                AudioChannel audioChannel=new AudioChannel(audioDecoder);

                mixerPlayer.addChannel(audioChannel);

                audioDecoder.play();
            }
        });

        verifyStoragePermissions(this);

        PlayerEvents events = new PlayerEvents() {
            @Override public void onStop() {

            }
            @Override public void onStart(String mime, int sampleRate, int channels, long duration) {

            }
            @Override public void onPlayUpdate(int percent, long currentms, long totalms) {

            }
            @Override public void onPlay() {
            }
            @Override public void onError() {

            }
        };

        mPlayer = new OpenMXPlayer(events);

    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(AppCompatActivity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
