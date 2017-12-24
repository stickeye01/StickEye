package com.example.jarim.myapplication;

import android.Manifest;
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

public class MainActivity extends Activity implements OnClickListener {
    // Debugging
    private static final String TAG = "Main";

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // permission
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1;
    int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 1;
    // Layout
    private Button btn_Connect;
    private TextView txt_Result;

    private BluetoothService btService = null;

    // Database
    private DBHandler mDBOpenHandler;

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        //
        // Android Permissions
        // Since android versions that are higher than 6.0 needs to specify and check permissions in
        // code section.
        // ****
        // ACCESS_COARSE_LOCATION: wifi permission for coarse-location
        // ACCESS_FINE_LOCATION: wifi permission for fine-location
        // BLUETOOTH: bluetooth permission
        // BLUETOOTH_ADMIN: bluetooth admin permission
        //
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
        txt_Result = (TextView) findViewById(R.id.txt_result);

        btn_Connect.setOnClickListener(this);

        // create BluetoothService
        if(btService == null) {
            btService = new BluetoothService(this, mHandler);
        }

        mDBOpenHandler = new DBHandler(this);
        mDBOpenHandler.open();
    }

    @Override
    public void onClick(View v) {
        if(btService.getDeviceState()) {
            // If the device supports bluetooth function
            btService.enableBluetooth();
        } else {
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    btService.getDeviceInfo(data);
                }


            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {

                } else {

                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }
    }

}

