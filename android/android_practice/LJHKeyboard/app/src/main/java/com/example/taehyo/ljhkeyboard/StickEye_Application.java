package com.example.taehyo.ljhkeyboard;

import android.app.Application;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by taehyo on 2018-02-11.
 */

public class StickEye_Application extends Application implements TextToSpeech.OnInitListener {
    private TextToSpeech _tts;



    public TextToSpeech get_tts() {
        return _tts;
    }



    public void set_tts(TextToSpeech _tts) {
        this._tts = _tts;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        _tts = new TextToSpeech(this.getApplicationContext(), this);
    }



    @Override
    public void onInit(int arg0) {
        StickEye_playEffectSound eff = new StickEye_playEffectSound(this);
        eff.setResID(R.raw.es067);
        _tts.setLanguage(Locale.KOREA);
        _tts.setSpeechRate(1.0f);

    }


}
