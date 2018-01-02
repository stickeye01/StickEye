package com.example.jarim.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

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
    private Button btn_Connect;
    private Button btn_Server;
    private Button btn_Send;
    private TextView txt_Result;
    private TextView txt_mac_id;
    private TextView txt_conn_stats;

    // Bluetooth
    private BluetoothService btService = null;

    // Database
    private DBHandler mDBOpenHandler;

    int test_int = 0;

    /*
     * Check whether Bluetooth network is working or not.
     *
     * Description: The handler that gets information back from the BluetoothService.
     */
    private final Handler mHandler = new Handler() {
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
        btn_Connect = (Button) findViewById(R.id.btn_connect);
        btn_Server = (Button) findViewById(R.id.btn_server);
        btn_Send = (Button) findViewById(R.id.btn_send);
        txt_Result = (TextView) findViewById(R.id.txt_result);
        txt_conn_stats = (TextView) findViewById(R.id.conn_stats);
        txt_mac_id = (TextView) findViewById(R.id.mac_id);
        btn_Connect.setOnClickListener(this);
        btn_Server.setOnClickListener(this);
        btn_Send.setOnClickListener(this);

        if(btService == null) {
            btService = new BluetoothService(this, mHandler);
        }

        mDBOpenHandler = new DBHandler(this);
        mDBOpenHandler.open();

        //
        //PackageManager pkgMan = getPackageManager();
        //if (pkgMan.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        //    Log.e("LHC", "it supports BLE");
        //} else {
        //    Log.e("LHC", "it does not support BLE");
        //}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Two options: client and server side.
            case R.id.btn_connect:
                if(btService.getDeviceState()) {
                    btService.enableBluetooth(CLIENT_SIDE);
                } else {
                    finish();
                }
                break;
            case R.id.btn_server:
                if(btService.getDeviceState()) {
                    btService.enableBluetooth(SERVER_SIDE);
                } else {
                    finish();
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
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }
    }

}

