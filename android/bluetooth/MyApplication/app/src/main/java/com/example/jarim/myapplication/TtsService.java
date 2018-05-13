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
                int rate = mTts.setSpeechRate((float) 0.8);
                isLoaded = true;
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
        if (Constants.TTS_MODE == Constants.TTS_READ_NORMAL)
            mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
        else
            sspeakNumber(text);
    }

    // speak after speak
    public void ispeak(String text) {
        if (Constants.TTS_MODE == Constants.TTS_READ_NORMAL)
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        else
            ispeakNumber(text);
    }

    // speak number
    public void sspeakNumber(String num) {
        mTts.setSpeechRate((float)2);
        for (int i = 0; i < num.length(); i++) {
            mTts.speak(""+num.charAt(i), TextToSpeech.QUEUE_ADD, null);
        }
        mTts.setSpeechRate((float)0.8);
    }

    // speak number
    public void ispeakNumber(String num) {
        mTts.setSpeechRate((float)2);
        for (int i = 0; i < num.length(); i++) {
            mTts.speak(""+num.charAt(i), TextToSpeech.QUEUE_FLUSH, null);
        }
        mTts.setSpeechRate((float)0.8);
    }

}