package com.example.jarim.myapplication.BrailleKeyboard;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jarim.myapplication.Constants;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

public class BrailleKeyboard {

    public int modeFlag = 0;    // 0: Kor, 1: Eng(l), 2: Eng(U), 3: Num
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
    private int isModeLock = 0;

    private TextView bMode;

    public BrailleKeyboard(TtsService _tts, Context _ctx) {
        tts = _tts;
        Activity activity = (Activity) _ctx;
        bMode = activity.findViewById(R.id.b_mode);
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
        String test = start(input);
        tts.ispeak(test);
    }

    public void changeMode() {
        if (isModeLock != 0) return ;
        modeFlag++;
        modeFlag %= 4;
        if (modeFlag == 0) bMode.setText("Kor");
        else if (modeFlag == 1) bMode.setText("Eng(s)");
        else if (modeFlag == 2) bMode.setText("Eng(u)");
        else if (modeFlag == 3) bMode.setText("num");
    }

    public void changeMode(int mode) {
        if (0 <= mode && mode <= 3) {
            modeFlag = mode;
            if (modeFlag == 0) bMode.setText("Kor");
            else if (modeFlag == 1) bMode.setText("Eng(s)");
            else if (modeFlag == 2) bMode.setText("Eng(u)");
            else if (modeFlag == 3) bMode.setText("num");
        }
    }

    /**
     * parse an input from Arduino.
     * @param _input
     * @return
     */
    private int[] parsing(char _input) {
        int i=6,a;
        int input[] = new int[6];

        for(i = 0; i < 6; i++) {
            a = _input & 1;
            _input >>= 1;
            input[i] = a;
        }

        int isDouble = _input & 1;
        isDoubleCharacter(isDouble, input);

        return input;
    }


    public void isDoubleCharacter(int isDouble, int input[]) {
        if (isDouble == 1) {
            init(oneinput);
            for (int i = 0; i < 6; i++) {
                oneinput[i] = input[i];
            }
            doublemode = true;
            init(input);
        }
    }

    public String start(int input[]) {
        int flag = 0;
        for (int i = 0; i < 6; i++) {
            if (input[i] == 1)      flag = 1;
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

    /**
     *  Turn off braille keyboard.
     */
    public void turnOffBrailleKB() {
        Constants.KEYBOARD_MODE = Constants.BRAILLE_KEYBOARD_OFF;
    }

    /**
     *  Turn on braille keyboard.
     */
    public void turnOnBrailleKB() {
        Constants.KEYBOARD_MODE = Constants.BRAILLE_KEYBOARD_ON;
        TOffModeLock();
    }

    /**
     * Choose lock mode. If the lock mode is enabled, then user cannot change input mode
     * (such as Kor, Eng or Num)
     */
    public void TOnModeLock() {
        isModeLock = 1;
    }

    public void TOffModeLock() {
        isModeLock = 0;
    }

    public void clearString() {
        resultString = "";
    }

}


