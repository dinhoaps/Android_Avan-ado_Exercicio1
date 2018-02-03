package com.androidavancado.posiot.meump3player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidavancado.posiot.meump3player.service.MyMp3Binder;
import com.androidavancado.posiot.meump3player.service.MyMp3Service;
import com.androidavancado.posiot.meump3player.service.MyMp3ServiceImpl;

public class MyMp3Activity extends AppCompatActivity
        implements ServiceConnection, SensorEventListener {

    private MyMp3Service mMP3Service;
    private ProgressBar mPrgDuracao;
    private TextView mTxtMusica;
    private TextView mTxtDuracao;
    private String mMusica;

    private SensorManager mSensorManager;
    private Sensor mSensorGravity;
    private float mOldSensorValue;

    private AudioManager mAudioManager;

    private Handler mHandler = new Handler();

    private final int SENSOR_MIN = -9;
    private final int MAX_VOL = 1;
    private final int MIN_VOL = 0;
    private final int SENSOR_MAX = 9;


    private Thread mThreadProgresso = new Thread() {
        public void run() {
            atualizarTela();
            if (mMP3Service.getTempoTotal() > mMP3Service.getTempoDecorrido()) {
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    //EU
    float standardGravity;
    float thresholdGraqvity;

    //

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_mp3);


        //EU
        standardGravity = SensorManager.STANDARD_GRAVITY;
        thresholdGraqvity = standardGravity/5;
        //

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mOldSensorValue = 0;

        String nomeMusica = getIntent().getStringExtra("nomeMusica");

        Log.i("MyMp3Activity", "onCreate - Musica = " + nomeMusica);

        mMusica = nomeMusica;
        mPrgDuracao = (ProgressBar) findViewById(R.id.progressBar);
        mTxtMusica = (TextView) findViewById(R.id.txtMusica);
        mTxtDuracao = (TextView) findViewById(R.id.txtTempo);

        //mSensorManager.registerListener(this, mSensorGravity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent it = new Intent(this, MyMp3ServiceImpl.class);
        startService(it);
        bindService(it, this, 0);

        mSensorManager.registerListener(this, mSensorGravity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
        mHandler.removeCallbacks(mThreadProgresso);
        mSensorManager.unregisterListener(this);
    }

    public void btnPlayClick(View v) {
        Log.i("MyMp3Activity", "botãoPlay");
        mHandler.removeCallbacks(mThreadProgresso);
        if (mMusica != null) {
            Log.i("MyMp3Activity", "musica != null");
            mMP3Service.play(mMusica);
            mHandler.post(mThreadProgresso);
        }
    }

    public void btnPauseClick(View v) {
        mMP3Service.pause();
        mHandler.removeCallbacks(mThreadProgresso);
    }

    public void btnStopClick(View v) {
        mMP3Service.stop();
        mHandler.removeCallbacks(mThreadProgresso);
        mPrgDuracao.setProgress(0);
        mTxtDuracao.setText(DateUtils.formatElapsedTime(0));
    }

    public void btnListaMusicas(View v) {
        Log.i("MyMp3Activity", "ListaMusicas");

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mMP3Service = ((MyMp3Binder) service).getServico();

        Log.i("MyMp3Activity", "onServiceConnected");

        if(mMusica == null)
            mMusica = mMP3Service.getMusicaAtual();

        if (mMusica != null) {
            mMP3Service.play(mMusica);
            mHandler.post(mThreadProgresso);
        }

    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
        mMP3Service = null;
    }

    private void atualizarTela() {
        mMusica = mMP3Service.getMusicaAtual();
        mTxtMusica.setText(mMusica);
        mPrgDuracao.setMax(mMP3Service.getTempoTotal());
        mPrgDuracao.setProgress(mMP3Service.getTempoDecorrido());
        mTxtDuracao.setText(
                DateUtils.formatElapsedTime(mMP3Service.getTempoDecorrido() / 1000));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor source = sensorEvent.sensor;
        float normalizedVolume = 0f;
        float sensorValue;


        sensorValue = sensorEvent.values[2];


        if(source.getType() == Sensor.TYPE_GRAVITY){

                mOldSensorValue = sensorValue;
                normalizedVolume = normalizaVolume(sensorValue);

            }

        mMP3Service.setVolume(normalizedVolume);
        }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Não precisa fazer nada por eqt
    }

    /**
     * O volume do sistema varia entre 0 e valor Máximo
     * @param sensorValue
     * @return
     */
    private float normalizaVolume (float sensorValue) {
        return ((sensorValue - (SENSOR_MIN)) * (MAX_VOL - MIN_VOL)) / (SENSOR_MAX - (SENSOR_MIN)) + MIN_VOL;
    }

}
