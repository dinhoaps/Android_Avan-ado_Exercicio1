package com.androidavancado.posiot.meump3player.service;

/**
 * Created by erika on 27/01/18.
 */

public interface MyMp3Service {
    void play(String arquivo);
    void pause();
    void stop();
    void setVolume(float newVolume);
    String getMusicaAtual();
    int getTempoTotal();
    int getTempoDecorrido();
}

