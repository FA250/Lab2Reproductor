package com.example.lab2reproductor;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    Button btnPlayPause;
    boolean isActivePlaying;
    int idCancionActual;
    String [][] listaCanciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[][] listaidCanciones=obtenerCanciones();

        String[][] listaCanciones =poblarListViewCanciones(listaidCanciones);

        cargarCancionRamdom(listaCanciones);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        isActivePlaying = false;
    }

    public void cargarCancionRamdom(String[][] listaCanciones){
        Random r=new Random();
        int numeroCancion=r.nextInt(listaCanciones.length);
        mediaPlayer = MediaPlayer.create(this, getResources().getIdentifier("com.example.lab2reproductor:raw/"+listaCanciones[numeroCancion][1], null,null));
        idCancionActual= numeroCancion;
        cambiarCancionArtista(listaCanciones[numeroCancion][2],listaCanciones[numeroCancion][3]);

        inicializarBarraReproduccionVolumen();
    }

    public void siguienteCancion(){
        if(listaCanciones.length<=idCancionActual+1)
            reproducirCancion(0);
        else
            reproducirCancion(idCancionActual+1);
    }

    public void anteriorCancion(){
        if(idCancionActual-1>=0)
            reproducirCancion(idCancionActual-1);
        else
            reproducirCancion(listaCanciones.length-1);
    }

    public void reproducirCancion(int id){
        if(listaCanciones!=null) {
            mediaPlayer.stop();
            mediaPlayer = MediaPlayer.create(this, getResources().getIdentifier("com.example.lab2reproductor:raw/" + listaCanciones[id][1], null, null));
            idCancionActual = id;
            cambiarCancionArtista(listaCanciones[id][2], listaCanciones[id][3]);

            inicializarBarraReproduccionVolumen();
            mediaPlayer.start();
            isActivePlaying = true;
            btnPlayPause.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
        else
            Toast.makeText(this,"ERROR: no se encontraron canciones disponibles",Toast.LENGTH_LONG).show();
    }

    public void cambiarCancionArtista(String nombreCancion, String nombreArtista){
        //TODO mostrar el artista
        TextView lbNombreCancion=findViewById(R.id.lbNombreCancion);

        lbNombreCancion.setText(nombreCancion);
    }

    public void inicializarBarraReproduccionVolumen(){
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
        final int duration = mediaPlayer.getDuration();
        int progress = mediaPlayer.getCurrentPosition();
        advanceSeekbar.setMax(duration);
        advanceSeekbar.setProgress(progress);

        advanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b)
                    mediaPlayer.seekTo(i);


                //TODO agregar duracion total de la cancion
                //TODO mostrar el tiempo actual de la cancion
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                siguienteCancion();
            }
        });

        new Timer().scheduleAtFixedRate(new TimerTask() {
                                            @Override
                                            public void run() {

                                                advanceSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                                            }
                                        }, 0, 1000
        );
    }

    public String[][] poblarListViewCanciones(String[][] idCanciones){
        String[][] ResCanciones=new String[idCanciones.length][5];
        //Llenar list view
        if(idCanciones!=null) {
            ListView listaCanciones = findViewById(R.id.listCanciones);

            final ArrayList canciones = new ArrayList();
            final ArrayList artistas = new ArrayList();

            int i=0;
            for (String[] id : idCanciones) {
                Uri path = Uri.parse("android.resource://" + getPackageName() + "/" + id[0]);
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(this, path);
                ResCanciones[i][0]=id[0];
                ResCanciones[i][1]=id[1];

                if(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)!=null) {
                    canciones.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                    ResCanciones[i][2]=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                }
                else {
                    canciones.add(id[1]);
                    ResCanciones[i][2]=id[1];
                }

                if(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null){
                    artistas.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                    ResCanciones[i][3]=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                }
                else {
                    artistas.add("");
                    ResCanciones[i][3] = "?";
                }

                if(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!=null){
                    ResCanciones[i][4]=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                }
                else {
                    ResCanciones[i][4] = "?";
                }
                i++;
            }

            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, canciones) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                    text1.setText(canciones.get(position).toString());
                    text2.setText(artistas.get(position).toString());
                    return view;
                }
            };
            listaCanciones.setAdapter(adapter);

            listaCanciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    reproducirCancion(position);
                }
            });
        }
        else
            Toast.makeText(this,"No se encontraron canciones",Toast.LENGTH_LONG).show();
        listaCanciones=ResCanciones;
        return ResCanciones;
    }


    public String[][] obtenerCanciones(){
        Field[] ID_Fields = R.raw.class.getFields();
        String[][] resArray = new String[ID_Fields.length][2];
        for(int i = 0; i < ID_Fields.length; i++) {
            try {
                resArray[i][0]= String.valueOf(ID_Fields[i].getInt(0));
                resArray[i][1]= ID_Fields[i].getName();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return resArray;
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

    public void btnAnterior(View view) {
        anteriorCancion();
    }

    public void btnSiguiente(View view) {
        siguienteCancion();
    }
}
