package com.example.jarim.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jarim.myapplication.USBConnector.Constants;
import com.example.jarim.myapplication.USBConnector.SerialConnector;

import java.io.UnsupportedEncodingException;

public class MainActivity extends Activity implements OnClickListener {
    // Debugging
    private static final String TAG = "Main";

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int SERVER_SIDE = 1;
    private static final int CLIENT_SIDE = 2;

    // permission
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1;
    int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 1;

    // Layout
    private Button btn_connect;
    private Button btn_send;
    private Button btn_register;
    private TextView txt_Result;
    private TextView txt_mac_id;
    private TextView txt_conn_stats;
    private TextView txt_serv_stats;
    private TextView txt_usb_stats;

    // Bluetooth
    private BluetoothService btService = null;

    // Database
    private DBHandler mDBOpenHandler;

    // Register dialog
    private RegisterDialog mRegDialog;
    private SerialConnector mSerialConn;
    private SerialListener usbListener;
    private usbHandler usbHandler;

    // Activity context
    private Context aContext;

    int test_int = 0;

    /*
     * Check whether Bluetooth network is working or not.
     *
     * Description: The handler that gets information back from the BluetoothService.
     */
    @SuppressLint("HandlerLeak")
    private final Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case BluetoothService.MESSAGE_READ:
                    txt_Result.setText((String)msg.obj);
                    break;
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    txt_conn_stats.setText((String)msg.obj);
                    break;
                case BluetoothService.MESSAGE_MAC_ID_CHANGE:
                    txt_mac_id.setText((String)msg.obj);
                    break;
                case BluetoothService.MESSAGE_SERVER_STATE:
                    txt_serv_stats.setText((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        // Permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH},
                MY_PERMISSIONS_REQUEST_BLUETOOTH);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);

        // Main Layout
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_register = (Button) findViewById(R.id.btn_register);
        txt_Result = (TextView) findViewById(R.id.txt_result);
        txt_conn_stats = (TextView) findViewById(R.id.conn_stats);
        txt_mac_id = (TextView) findViewById(R.id.mac_id);
        txt_serv_stats = (TextView) findViewById(R.id.server_stats);
        txt_usb_stats = (TextView) findViewById(R.id.usb_stat);
        btn_connect.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        btn_register.setOnClickListener(this);

        if(btService == null) {
            btService = new BluetoothService(this, btHandler);
        }

        mDBOpenHandler = new DBHandler(this);
        //
        //PackageManager pkgMan = getPackageManager();
        //if (pkgMan.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        //    Log.e("LHC", "it supports BLE");
        //} else {
        //    Log.e("LHC", "it does not support BLE");
        //}

        if(btService.getDeviceState()) {
            btService.enableBluetooth(SERVER_SIDE);
        } else {
            finish();
        }

        usbHandler = new usbHandler();
        usbListener = new SerialListener();
        mSerialConn = new SerialConnector(getApplicationContext(), usbListener, usbHandler);
        mRegDialog = new RegisterDialog(this);
        aContext = this;

        // initialize all the things for checking.
        mSerialConn.initialize();
        if(btService.getDeviceState()) {
            if (btService.getState() != BluetoothService.STATE_CONNECTED &&
                    btService.getSState() != BluetoothService.STATE_CONNECTED) {
                btService.enableBluetooth(CLIENT_SIDE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Two options: client and server side.
            case R.id.btn_connect:
                if(btService.getDeviceState()) {
                    if (btService.getState() != BluetoothService.STATE_CONNECTED &&
                            btService.getSState() != BluetoothService.STATE_CONNECTED) {
                        btService.enableBluetooth(CLIENT_SIDE);
                    }
                }
                break;
            // Send messages to other devices
            case R.id.btn_send:
                try {
                    btService.write((byte[])("test"+Integer.toString(test_int++)+"\n").getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_register:
                mRegDialog.show();
                txt_usb_stats.setText("");
                mSerialConn.initialize();
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "MainActivity: onActivityResult(" + resultCode + ")");

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // Scan devices and
                if(resultCode == Activity.RESULT_OK){
                    btService.getDeviceInfo(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns.
                if (resultCode != Activity.RESULT_OK) {
                    Log.d(TAG, "Bluetooth is not enabled");
                } else {
                    // repetitively activate server.
                    btService.enableBluetooth(SERVER_SIDE);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSerialConn.finalize();
        mRegDialog.dismiss();
    }


    public class usbHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case Constants.MSG_DEVICD_INFO:
                    txt_usb_stats.setText((String)msg.obj);
                    break;
                case Constants.MSG_DEVICE_COUNT:
                    txt_usb_stats.setText(Integer.toString(msg.arg1) + " device(s) found \n");
                    break;
                case Constants.MSG_READ_DATA_COUNT:
                    //txt_usb_stats.setText(((String)msg.obj) + "\n");
                    break;
                case Constants.MSG_READ_DATA:
                    if(msg.obj != null) {
                        //mTextInfo.setText((String)msg.obj);
                        txt_usb_stats.setText("");
                        txt_usb_stats.append((String)msg.obj);
                        txt_usb_stats.append("\n");
                    }
                    break;
                case Constants.MSG_SERIAL_ERROR:
                    txt_usb_stats.setText((String)msg.obj);
                    break;
                case Constants.MSG_DIALOG_HIDE:
                    mRegDialog.hide();
                    break;
                case Constants.MSG_USB_CONN_SUCCESS:
                    Toast.makeText(aContext, "USB connection succeeds!!", Toast.LENGTH_SHORT).show();
                    mSerialConn.sendCommand("MAC_ADDR");
                    mDBOpenHandler.open();
                    mDBOpenHandler.delete("*", "*");
                    String device_address = mDBOpenHandler.select();
                    txt_mac_id.setText(device_address);
                    mDBOpenHandler.close();
                    break;
                case Constants.MSG_CONN_FAIL:
                    //Toast.makeText(aContext, "USB connection fails!!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    public class SerialListener {
        public void onReceive(int msg, int arg0, int arg1, String arg2, Object arg3) {
            switch(msg) {
                case Constants.MSG_DEVICD_INFO:
                    txt_usb_stats.append(arg2);
                    break;
                case Constants.MSG_DEVICE_COUNT:
                    txt_usb_stats.append(Integer.toString(arg0) + " device(s) found \n");
                    break;
                case Constants.MSG_READ_DATA_COUNT:
                    txt_usb_stats.append(Integer.toString(arg0) + " buffer received \n");
                    break;
                case Constants.MSG_READ_DATA:
                    if(arg3 != null) {
                        txt_usb_stats.append((String)arg3);
                        txt_usb_stats.append("\n");
                    }
                    break;
                case Constants.MSG_SERIAL_ERROR:
                    txt_usb_stats.append(arg2);
                    break;
                case Constants.MSG_FATAL_ERROR_FINISH_APP:
                    //finish();
                    break;
            }
        }
    }
}

