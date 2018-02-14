package com.example.jarim.myapplication.AndroidSide;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.jarim.myapplication.Constants;
import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

/**
 * Created by hochan on 2018-02-07.
 */

public class PhoneCallBean extends AppBean{
    private EditText input_etext;
    private int no_degree = Constants.PHONE_NUM_WRITE_STAGE;
    private String phoneNum;
    private Intent intent;

    public PhoneCallBean(String _name, String _intentName, TtsService _tts, Context _ctx) {
        super(_name, _intentName, _tts, _ctx);
        input_etext = (EditText) mActivity.findViewById(R.id.test_input);
    }


    @Override
    public boolean start(Object o) {
        tts.sspeak("전화 걸기입니다. 전화번호를 입력하세요.");
        input_etext.setText("");
        input_etext.requestFocus();
        return true;
    }


    @Override
    public void clicked(Object _v) {
        View v = (View) _v;
        // We go through two steps while calling,
        // First, get a phone number,
        // Second, call this phone number,
        if (v.getId() == R.id.click2) {
            if (no_degree == Constants.PHONE_NUM_WRITE_STAGE) {
                phoneNum = input_etext.getText().toString();
                input_etext.setText("");
                tts.sspeak("전화번호가 입력되었습니다. 전화번호는 " + phoneNum + " 입니다.");
                tts.sspeak("전화를 걸고 싶으면 Click 버튼을 누르세요");
                no_degree = Constants.CALLING_STAGE;
            } else if (no_degree == Constants.CALLING_STAGE) {
                tts.sspeak("전화를 겁니다.");
                intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel:" + phoneNum));

                // Check permissions
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    mContext.startActivity(intent);
                }
                no_degree = Constants.PHONE_NUM_WRITE_STAGE;
            }
        }
    }


}
