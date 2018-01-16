package com.example.jarim.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.CountDownTimer;
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
    boolean MY_PERMISSION_SERIAL = false;

    // Layout
    private Button btn_connect;
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
    private usbHandler usbHandler;
    private final Handler mRegDialogHandler = new Handler();
    private final Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            if(mRegDialog != null && mRegDialog.isShowing())
                mRegDialog.hide();
        }
    };

    // Activity context
    private Context aContext;

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
    /*
    * Broadcast Receiver for usb serial connection
    *  ACTION_USB_DEVICE_DETACHED : usb disconnected. close connection and clear the connected devices.(finalize())
    *  ACTION_USB_DEVICE_ATTACHED : usb connected. Get device information and Try to get permission.
    *  ACTION_USB_PERMISSION : Try to get permission. if user allowed permission, open connection with arduino.(initialize())
     */
    BroadcastReceiver mUsbReceiver =new BroadcastReceiver(){
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device =(UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(device != null){
                    if(mSerialConn != null) {
                        mSerialConn.finalize();
                        MY_PERMISSION_SERIAL = false;
                        txt_usb_stats.setText("A drive is NULL");
                    }
                }
            } else if (Constants.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //allowed permission
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            MY_PERMISSION_SERIAL = true;
                            mSerialConn.initialize();
                        }
                    }
                    //denied permission
                    else {
                        MY_PERMISSION_SERIAL = false;
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LHC", "Handler thread ID:"+Thread.currentThread().getId());
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

        // Layout
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_register = (Button) findViewById(R.id.btn_register);
        txt_Result = (TextView) findViewById(R.id.txt_result);
        txt_conn_stats = (TextView) findViewById(R.id.conn_stats);
        txt_mac_id = (TextView) findViewById(R.id.mac_id);
        txt_serv_stats = (TextView) findViewById(R.id.server_stats);
        txt_usb_stats = (TextView) findViewById(R.id.usb_stat);
        btn_connect.setOnClickListener(this);
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

        // Initialize and start server thread.
        if(btService.getDeviceState()) {
            btService.enableBluetooth(SERVER_SIDE);
        } else {
            finish();
        }

        usbHandler = new usbHandler();
        mSerialConn = new SerialConnector(getApplicationContext(), usbHandler);
        mRegDialog = new RegisterDialog(this);
        aContext = this;

        //BroadReceiver for serial connection
        IntentFilter filler = new IntentFilter("ACTION_USB_PERMISSION");
        filler.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filler.addAction(Constants.ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filler);

        // Get registered device information.
        updateMACID();

        // Attempts to connect Bluetooth.
        if(btService.getDeviceState()) {
            if (btService.getState() != BluetoothService.STATE_CONNECTED &&
                    btService.getSState() != BluetoothService.STATE_CONNECTED) {
                btService.enableBluetooth(CLIENT_SIDE);
            }
        }
    }

    /**
     *  select and update mac id.
     */
    public void updateMACID() {
        if (mDBOpenHandler != null) {
            mDBOpenHandler.open();
            Constants.macAddr = mDBOpenHandler.select().
                                    replace("\n", "").
                                    replace("\n", "");
            txt_usb_stats.setText(Constants.macAddr);
            txt_mac_id.setText(Constants.macAddr);
            mDBOpenHandler.close();
        } else {
            Toast.makeText(this, "USB handler is NULL", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                if(btService.getDeviceState()) {
                    if (btService.getState() != BluetoothService.STATE_CONNECTED &&
                            btService.getSState() != BluetoothService.STATE_CONNECTED) {
                        btService.enableBluetooth(CLIENT_SIDE);
                    }
                }
                break;
            case R.id.btn_register:
                mRegDialog.show();
                mRegDialogHandler.postDelayed(mDismissRunnable, Constants.TIMEOUT);
                if(mSerialConn != null && !MY_PERMISSION_SERIAL){
                    txt_usb_stats.setText("Try to get device permission..");
                    mSerialConn.obtainPermission();
                } else {
                    txt_usb_stats.setText("Registering..");
                    mSerialConn.initialize();
                }
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "MainActivity: onActivityResult(" + resultCode + ")");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    btService.getDeviceInfo(data);
                }
                break;
            case REQUEST_ENABLE_BT:
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

        MY_PERMISSION_SERIAL = false;
        unregisterReceiver(mUsbReceiver);
    }

    /**
     *
     */
    public class usbHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case Constants.MSG_DEVICD_INFO:  // Serial connected device information.
                    txt_usb_stats.setText((String)msg.obj);
                    break;
                case Constants.MSG_DEVICE_COUNT: // The number of connected device.
                    txt_usb_stats.setText(Integer.toString(msg.arg1) + " device(s) found \n");
                    break;
                case Constants.MSG_READ_DATA_COUNT: // The number of gotten bytes.
                    txt_usb_stats.append("Read data from a serial port (bytes): " +
                                                Integer.toString(msg.arg1)+"\n");
                    break;
                case Constants.MSG_READ_DATA: // Get data and register a device.
                    if(msg.obj != null) {
                        String data = (String) msg.obj;
                        // Complete input format:
                        //   "\r20:03:04:....:02\n"
                        if (data.length() == 19) {
                            txt_usb_stats.setText(data);
                            Log.e("LHC", "get data:"+data);
                            Log.e("LHC", "data start:"+Integer.toString(data.length()));
                            String device_address = data.replaceAll("\n", "").
                                                        replace("\r", "");
                            mDBOpenHandler.open();
                            mDBOpenHandler.deleteAll();
                            mDBOpenHandler.insert("target", device_address);
                            Toast.makeText(getApplicationContext(), device_address+":"+
                                    Integer.toString(device_address.length()),
                                    Toast.LENGTH_SHORT).show();
                            txt_mac_id.setText(data);
                            if (mRegDialog.isShowing()) mRegDialog.hide();
                            mDBOpenHandler.close();
                            mSerialConn.finalize();
                            //Log.e("LHC", "Handler thread ID:"+Thread.currentThread().getId());
                            Toast.makeText(getApplicationContext(), "threads are finished", Toast.LENGTH_SHORT).show();
                            updateMACID();
                        }
                    }
                    break;
                case Constants.MSG_SERIAL_ERROR: // Error statement.
                    txt_usb_stats.setText((String)msg.obj);
                    break;
                case Constants.MSG_DIALOG_HIDE: // Hide dialogue.
                    if (mRegDialog.isShowing()) mRegDialog.hide();
                    break;
                case Constants.MSG_USB_CONN_SUCCESS: // Notify that serial networking is achieved.
                    Toast.makeText(aContext, "USB connection succeeds!!",
                                            Toast.LENGTH_SHORT).show();
                    // Send "MAC_ADDR\0" to the Arduino
                    mSerialConn.sendCommand("MAC_ADDR\0");
                    break;
                case Constants.MSG_CONN_FAIL:
                    break;
                case Constants.MSG_USB_NOTIFY:  // Show normal messages.
                    txt_usb_stats.append((String)msg.obj);
                    break;
            }
        }
    }
}

