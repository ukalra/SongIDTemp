package goose.mictest;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.media.audiofx.Visualizer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    Button RECORD, STOP, PLAY ;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    Random random ;
    String RandomAudioFileName = "qwertyuiopasdfghjklzxcvbnm";//lel querty keyboard style
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;
    VisualizerView mVisualizerView;


    private Visualizer mVisualizer;
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RECORD = (Button) findViewById(R.id.record);
        STOP = (Button) findViewById(R.id.stop);
        PLAY = (Button) findViewById(R.id.play);

        STOP.setEnabled(false);
        PLAY.setEnabled(false);
        RECORD.setEnabled(true);

        random = new Random();

        RECORD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {//you do have the perms to use mic
                    RECORD.setEnabled(false);//turns off the recorder button
                    recording = true;//you are indeed recording
                    STOP.setEnabled(true);//turns on the stop button
                    PLAY.setEnabled(false);//turns off the play button
                    AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/" + CreateRandomAudioFileName(4) + "TEST.3gp";//saves to some random place somewhere...-_-
                    MediaRecorderReady();//prepares the microphone thing

                    try {//exception things, cuz they're safe
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    }
                    catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(MainActivity.this, "Recording started", Toast.LENGTH_SHORT).show();//little blurb at bottom
                } else {//makes sure permissions are set
                    requestPermission();//request the perms to use mic
                }

            }
        });

        STOP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recording) {
                    mediaRecorder.stop();
                    STOP.setEnabled(false);
                    PLAY.setEnabled(true);
                    RECORD.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Recording Completed",Toast.LENGTH_SHORT).show();//little blurb at bottom
                }
                else {
                    STOP.setEnabled(false);
                    RECORD.setEnabled(true);
                    PLAY.setEnabled(true);

                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        MediaRecorderReady();
                    }
                }
            }
        });

        PLAY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException, SecurityException, IllegalStateException {
                recording = false;
                STOP.setEnabled(true);
                RECORD.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                    mVisualizerView = (VisualizerView) findViewById(R.id.myvisualizerview);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                initAudio();
                Toast.makeText(MainActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setupVisualizerFxAndUI();
        // Make sure the visualizer is enabled only when you actually want to
        // receive data, and
        // when it makes sense to receive data.
        mVisualizer.setEnabled(true);
        // When the stream ends, we don't need to collect any more data. We
        // don't do this in
        // setupVisualizerFxAndUI because we likely want to have more,
        // non-Visualizer related code
        // in this callback.
        mediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mVisualizer.setEnabled(false);
                    }
                });
        mediaPlayer.start();

    }

    private void setupVisualizerFxAndUI() {

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }


    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//makes sure we are using the mic
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//file format is in 3gp
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//uses proper speakers for output
        mediaRecorder.setOutputFile(AudioSavePathInDevice);//sets the file output to this thing...

    }

    public String CreateRandomAudioFileName(int n){//literally just used to make consstant random names of size n
        StringBuilder stringBuilder = new StringBuilder( n );
        int i = 0 ;
        while(i < n ) {
            stringBuilder.append(RandomAudioFileName.charAt(random.nextInt(RandomAudioFileName.length())));
            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
}