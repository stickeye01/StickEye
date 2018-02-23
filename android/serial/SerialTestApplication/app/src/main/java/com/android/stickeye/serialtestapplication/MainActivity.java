package com.android.stickeye.serialtestapplication;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private Context mContext = null;
    private ActivityHandler mHandler = null;

    private SerialListener mListener = null;
    private SerialConnector mSerialConn = null;

    private TextView mTextLog = null;
    private TextView mTextInfo = null;
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;
    private Button mButton4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // System
        mContext = getApplicationContext();

        // Layouts
        setContentView(R.layout.activity_main);
        /*
        mTextLog : 아두이노->안드로이드 serial 전송 데이터 출력
        mTextInfo : error message
        mButton1 : "AT" 전송 (아두이노 블루투스 AT 명령모드 ) ->
                    성공시 아두이노 -> 안드로이드 "OK" 전송
        mButton2 : "AT+NAMED01"전송 (아두이노 블루투스 이름 변경) ->
                    성공시 아두이노 -> 안드로이드 "OKsetname" 전송
        */
        mTextLog = (TextView) findViewById(R.id.text_serial);
        mTextLog.setMovementMethod(new ScrollingMovementMethod());
        mTextInfo = (TextView) findViewById(R.id.text_info);
        mTextInfo.setMovementMethod(new ScrollingMovementMethod());

        mButton1 = (Button) findViewById(R.id.button_send1);
        mButton1.setOnClickListener(this);
        mButton2 = (Button) findViewById(R.id.button_send2);
        mButton2.setOnClickListener(this);
        mButton3 = (Button) findViewById(R.id.button_send3);
        mButton3.setOnClickListener(this);
        mButton3 = (Button) findViewById(R.id.button_send4);
        mButton3.setOnClickListener(this);

        // Initialize
        mListener = new SerialListener();
        mHandler = new ActivityHandler();

        // Initialize Serial connector and starts Serial monitoring thread.
        mSerialConn = new SerialConnector(mContext, mListener, mHandler);
        mSerialConn.initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSerialConn.finalize();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_send1:
                mSerialConn.sendCommand("AT");
                break;
            case R.id.button_send2:
                mSerialConn.sendCommand("AT+NAMED01");
                break;
            case R.id.button_send3:
                mSerialConn.sendCommand("bluetooth");
                break;
            case R.id.button_send4:
                mSerialConn.sendCommand("maxid");
                break;
            default:
                break;
        }
    }

    public class SerialListener {
        public void onReceive(int msg, int arg0, int arg1, String arg2, Object arg3) {
            switch(msg) {
                case Constants.MSG_DEVICD_INFO:
                    mTextLog.append(arg2);
                    break;
                case Constants.MSG_DEVICE_COUNT:
                    mTextLog.append(Integer.toString(arg0) + " device(s) found \n");
                    break;
                case Constants.MSG_READ_DATA_COUNT:
                    mTextLog.append(Integer.toString(arg0) + " buffer received \n");
                    break;
                case Constants.MSG_READ_DATA:
                    if(arg3 != null) {
                        mTextInfo.setText((String)arg3);
                        mTextLog.append((String)arg3);
                        mTextLog.append("\n");
                    }
                    break;
                case Constants.MSG_SERIAL_ERROR:
                    mTextLog.append(arg2);
                    break;
                case Constants.MSG_FATAL_ERROR_FINISH_APP:
                    finish();
                    break;
            }
        }
    }

    public class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case Constants.MSG_DEVICD_INFO:
                    mTextLog.append((String)msg.obj);
                    break;
                case Constants.MSG_DEVICE_COUNT:
                    mTextLog.append(Integer.toString(msg.arg1) + " device(s) found \n");
                    break;
                case Constants.MSG_READ_DATA_COUNT:
                    mTextLog.append(((String)msg.obj) + "\n");
                    break;
                case Constants.MSG_READ_DATA:
                    if(msg.obj != null) {
                        mTextInfo.setText((String)msg.obj);
                        mTextLog.append((String)msg.obj);
                        mTextLog.append("\n");
                    }
                    break;
                case Constants.MSG_SERIAL_ERROR:
                    mTextLog.append((String)msg.obj);
                    break;
            }
        }
    }
}
