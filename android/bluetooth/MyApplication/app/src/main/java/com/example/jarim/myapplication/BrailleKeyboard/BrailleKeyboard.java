package com.example.jarim.myapplication.BrailleKeyboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jarim.myapplication.TtsService;

public class BrailleKeyboard {

    public int modeFlag = 0;
    private String[] modeStr = {"한글", "영어 소문자", "영어 대문자", "숫자"};

    private Scanner scan = new Scanner();
    private KorTranslation kt = new KorTranslation();

    public static String resultString = "";
    private String one, two, three;
    private Token token;
    private int oneinput[] = new int[6];
    private int totalinput[] = new int[12];
    private boolean doublemode = false;
    private char value;
    private TtsService tts;

    public BrailleKeyboard(TtsService _tts) {
        tts = _tts;
    }

    public void init(int num[]) {
        for (int i = 0; i < 6; i++) {
            num[i] = 0;
        }
    }

    public void removeOneChar() {
        if (resultString.length() > 0)
           resultString = resultString.substring(0, resultString.length()-1);
    }

    public void removeAll() {
        resultString = "";
    }

    /**
     * translate braille to character (e.g. English or Korean)
     */
    public void translateB2C(char _input) {
        int[] input = parsing(_input);
        // if the mode is changed.
        if (input[7] == 1) {
            modeFlag ++;
            modeFlag %= 4;
        }
        isDoubleCharacter(input);
        String test = start(input, modeFlag);
        Log.e("LHC", ">>>>>>>>>>"+test);
        tts.ispeak(test);
    }

    /**
     * parse an input from Arduino.
     * @param _input
     * @return
     */
    private int[] parsing(char _input) {
        int i=6,a;
        int input[] = new int[8];

        for(i = 0; i < 8; i++) {
            a = _input & 1;
            _input >>= 1;
            input[i] = a;
        }
        Log.e("LHC", "Braille:"+input.toString());
        return input;
    }


    public void isDoubleCharacter(int input[]) {
        if (input[6] == 1) {
            init(oneinput);
            for (int i = 0; i < 6; i++) {
                oneinput[i] = input[i];
            }
            doublemode = true;
            init(input);
        }
    }

    public String start(int input[], int modeFlag) {
        int flag = 0;
        for (int i = 0; i < 6; i++) {
            if (input[i] == 1) {
                flag = 1;
            }
        }

        if (flag == 1) {
            if (!doublemode) {
                token = scan.scanner(input, modeFlag);
                doublemode = false;
            } else {
                for (int i = 0; i < 6; i++) {
                    totalinput[i] = oneinput[i];
                }
                for (int i = 6; i < 12; i++) {
                    totalinput[i] = input[i - 6];
                }
                token = scan.scanner(totalinput, modeFlag);
                doublemode = false;
            }

            if (token != null) {
                if (modeFlag == 0) {
                    value = kt.korSplitData(token);

                    if (value != ' ') {
                        resultString += String.valueOf(value);
                    }
                } else if (modeFlag == 2) {
                    resultString += (token.getUnicodeValue()).toUpperCase();
                } else {
                    resultString += (token.getUnicodeValue());
                }
            } else {
                tts.ispeak("점자가 잘못입력되었습니다.");
            }
        }
        init(input);

        return resultString;
    }
}


