package com.androidavancado.posiot.meump3player.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.androidavancado.posiot.meump3player.MyMp3Activity;
import com.androidavancado.posiot.meump3player.R;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by erika on 27/01/18.
 */

public class MyMp3ServiceImpl extends Service implements MyMp3Service {

    public static final String EXTRA_ARQUIVO = "arquivo";
    public static final String EXTRA_ACAO = "acao";
    public static final String ACAO_PLAY  = "play";
    public static final String ACAO_PAUSE = "pause";
    public static final String ACAO_STOP  = "stop";
    private MediaPlayer mPlayer;
    private String mArquivo;
    private boolean mPausado;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return new MyMp3Binder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACAO_PLAY.equals(intent.getStringExtra(EXTRA_ACAO))) {
                play(intent.getStringExtra(EXTRA_ARQUIVO));
            } else if (ACAO_PAUSE.equals(intent.getStringExtra(EXTRA_ACAO))) {
                pause();
            } else if (ACAO_STOP.equals(intent.getStringExtra(EXTRA_ACAO))) {
                stop();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // Implementação da interface Mp3Service
    @Override
    public void play(String musica) {
        Log.i("MyMp3ServiceImpl", "play");
        if (musica != null && !mPlayer.isPlaying() && !mPausado) {
            try {
                mPlayer.reset();
                FileInputStream fis = new FileInputStream(musica);
                mPlayer.setDataSource(fis.getFD());
                mPlayer.prepare();
                mArquivo = musica;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        mPausado = false;
        mPlayer.start();
        criarNotificacao();
    }

    @Override
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPausado = true;
            mPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (mPlayer.isPlaying() || mPausado) {
            mPausado = false;
            mPlayer.stop();
            mPlayer.reset();
        }
        removerNotificacao();
    }

    @Override
    public void setVolume(float newVolume) {


        Log.i("MyMp3ServiceiImpl", "newVolume = " + newVolume);

        if(mPlayer.isPlaying() || mPausado) {
            mPlayer.setVolume(newVolume, newVolume);
        }

    }


    @Override
    public String getMusicaAtual() {
        return mArquivo;
    }

    @Override
    public int getTempoTotal() {
        if (mPlayer.isPlaying() || mPausado) {
            return mPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getTempoDecorrido() {
        if (mPlayer.isPlaying() || mPausado) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    private void criarNotificacao() {
        Intent itPlay = new Intent(this, MyMp3ServiceImpl.class);
        itPlay.putExtra(EXTRA_ACAO, ACAO_PLAY);
        Intent itPause = new Intent(this, MyMp3ServiceImpl.class);
        itPause.putExtra(EXTRA_ACAO, ACAO_PAUSE);
        Intent itStop = new Intent(this, MyMp3ServiceImpl.class);
        itStop.putExtra(EXTRA_ACAO, ACAO_STOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MyMp3Activity.class);
        stackBuilder.addNextIntent(new Intent(this, MyMp3Activity.class));
        PendingIntent pitPlay = PendingIntent.getService(this, 1, itPlay, 0);
        PendingIntent pitPause = PendingIntent.getService(this, 2, itPause, 0);
        PendingIntent pitStop = PendingIntent.getService(this, 3, itStop, 0);
        PendingIntent pitActivity = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.my_notification_layout);
        views.setOnClickPendingIntent(R.id.imgBtnPlay, pitPlay);
        views.setOnClickPendingIntent(R.id.imgBtnPause, pitPause);
        views.setOnClickPendingIntent(R.id.imgBtnClose, pitStop);
        views.setOnClickPendingIntent(R.id.txtMusica, pitActivity);
        views.setTextViewText(R.id.txtMusica,
                mArquivo.substring(mArquivo.lastIndexOf(File.separator)+1));
        Notification n = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(views)
                .setOngoing(true)
                .build();
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(1, n);
    }

    private void removerNotificacao() {
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.cancel(1);
    }

}
