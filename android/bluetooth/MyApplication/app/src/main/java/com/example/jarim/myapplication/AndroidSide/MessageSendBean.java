package com.example.jarim.myapplication.AndroidSide;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.EditText;

import com.example.jarim.myapplication.R;
import com.example.jarim.myapplication.TtsService;

/**
 * Created by hochan on 2018-02-07.
 */

public class MessageSendBean extends AppBean {
    EditText input_etext;

    public MessageSendBean(String _name, String _intentName, TtsService _tts, Context _ctx) {
        super(_name, _intentName, _tts, _ctx);
        input_etext = (EditText) mActivity.findViewById(R.id.test_input);
    }


    @Override
    public boolean start(Object o) {
        checkPermission();
        tts.sspeak("문자 메시지 보내기입니다. 전화번호를 입력하세요.");
        input_etext.setText("");
        input_etext.requestFocus();
        return true;
    }

    @Override
    public void clicked(Object o) {
        tts.sspeak("문자 메시지 전송합니다.");
        sendSMS("01068788192", "Test");
    }

    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(mContext,
                                            Manifest.permission.RECEIVE_SMS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.e("LHC", "Message 수신 권한이 있습니다.");
        } else {
            Log.e("LHC", "Message 수신 권한이 없습니다.");

            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                                                            Manifest.permission.RECEIVE_SMS))
            {
                Log.e("LHC", "SMS 권한이 필요합니다.");
            }

            ActivityCompat.requestPermissions(mActivity, new String[]
                    {Manifest.permission.RECEIVE_SMS}, 1);
        }
    }

    public void sendSMS(String smsNumber, String smsText){
        PendingIntent sentIntent = PendingIntent.getBroadcast(mActivity,
                    0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(mActivity,
                    0, new Intent("SMS_DELIVERED_ACTION"), 0);

        mActivity.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Log.e("LHC", "SMS 전송 완료");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Log.e("LHC", "SMS 전송 실패");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Log.e("LHC", "SMS 서비스 지역 아님");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Log.e("LHC", "SMS 무선 라디오 꺼져있습니다.");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Log.e("LHC", "SMS PDU 실패");
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        mActivity.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Log.e("LHC", "SMS 도착 완료");
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Log.e("LHC", "SMS 도착 실패");
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
    }
}
