package com.example.jarim.myapplication;

import android.speech.tts.TextToSpeech;
import android.content.Context;
import android.util.Log;
import java.util.Locale;

/**
 * Created by Jarim on 2018-01-05.
 */

public class TtsService {

    private TextToSpeech mTts = null;
    private boolean isLoaded = false;

    //initializing TTS
    public void init(Context context) {
        try {
            mTts = new TextToSpeech(context, onInitListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //complete initializing
    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            //setting the language
            if (status == TextToSpeech.SUCCESS) {
                int result = mTts.setLanguage(Locale.KOREAN);
                isLoaded = true;
                Log.e("error", "language");
                Log.e("error", "isloaded111: " + Boolean.toString(isLoaded));


                //error is detected
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("error", "This Language is not supported");
                }

                // When the TTS initialization succeeds, speak it out.
                sspeak("앱이 실행되었습니다.");
            } else {
                sspeak("음성 출력이 실패하였습니다.");
                Log.e("error", "Initialization Failed!");
            }
        }
    };

    //shutting down TTS
    public void shutDown() {
        mTts.shutdown();
    }

    // ignore and speak
    public void sspeak(String text) {
            mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    // speak after speak
    public void ispeak(String text) {
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}