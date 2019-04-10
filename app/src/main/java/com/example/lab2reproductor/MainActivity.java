package com.example.lab2reproductor;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    Button btnPlayPause;
    boolean isActivePlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, R.raw.flyingpancake);

        btnPlayPause = findViewById(R.id.btnPlayPause);

        // Manejo de volumen
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                Log.d("Volume:", Integer.toString(progress));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Manejo de avance
        final SeekBar advanceSeekbar = findViewById(R.id.advanceSeekBar);
        int duration = mediaPlayer.getDuration();
        int progress = mediaPlayer.getCurrentPosition();
        advanceSeekbar.setMax(duration);
        advanceSeekbar.setProgress(progress);

        advanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mediaPlayer.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        new Timer().scheduleAtFixedRate(new TimerTask() {
                                            @Override
                                            public void run() {
                                                advanceSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                                            }
                                        }, 0, 1000
        );

        isActivePlaying = false;

        //Llenar list view
        ListView listaCanciones= findViewById(R.id.listCanciones);

        MediaPlayer.TrackInfo[] trackInfo=   mediaPlayer.getTrackInfo();

        ArrayList canciones=new ArrayList();

        for (MediaPlayer.TrackInfo info:trackInfo) {
            canciones.add(info.toString());
        }

        ArrayAdapter adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,canciones);
        listaCanciones.setAdapter(adapter);
    }

    public void btnPlayPause(View view) {
        if(isActivePlaying){
            mediaPlayer.pause();
            isActivePlaying=false;
            btnPlayPause.setBackgroundResource(android.R.drawable.ic_media_play);
        }
        else{
            mediaPlayer.start();
            isActivePlaying=true;
            btnPlayPause.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
    }
}
