package com.androidavancado.posiot.meump3player.service;

import android.os.Binder;

/**
 * Created by erika on 27/01/18.
 */

public class MyMp3Binder extends Binder {
    private MyMp3Service mServico;
    public MyMp3Binder(MyMp3Service s) {
        mServico = s;
    }
    public MyMp3Service getServico() {
        return mServico;
    }
}
