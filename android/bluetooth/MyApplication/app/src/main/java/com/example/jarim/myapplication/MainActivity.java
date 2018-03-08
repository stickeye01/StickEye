package com.example.jarim.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jarim.myapplication.AndroidSide.MultiDimensionMenu;
import com.example.jarim.myapplication.Bluetooth.BluetoothDBHandler;
import com.example.jarim.myapplication.Bluetooth.BluetoothService;
import com.example.jarim.myapplication.USBConnector.RegisterDialog;
import com.example.jarim.myapplication.USBConnector.SerialConnector;
import com.example.jarim.myapplication.BrailleKeyboard.BrailleKeyboard;

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
    boolean MY_PERMISSION_SERIAL = false;

    // Layout
    private Button btn_connect;
    private Button btn_register;
    private TextView txt_Result;
    private TextView txt_mac_id;
    private TextView txt_conn_stats;
    private TextView txt_serv_stats;
    private TextView txt_usb_stats;

    // TEST Layout
    private MultiDimensionMenu mDimMenu;

    // Bluetooth
    private BluetoothService btService = null;

    //tts
    TtsService tts = null;

    // Database
    private BluetoothDBHandler mDBOpenHandler;

    // Register dialog
    private RegisterDialog mRegDialog;
    private SerialConnector mSerialConn;
    private usbHandler usbHandler;
    private final Handler mRegDialogHandler = new Handler();
    private final Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRegDialog != null && mRegDialog.isShowing()) {
                mRegDialog.dismiss();
                mRegDialog = null;
            }
            txt_usb_stats.setText("USB connection failed, TIMEOUT");
        }
    };

    // Activity context
    private Context aContext;

    // Braille keyboard
    private char brailleInput = 0;
    private BrailleKeyboard braille;
    private TextView bMode;
    private TextView bInput;
    private EditText testInput;
    private int bModeNo = 0;

    /*
     * Check whether Bluetooth network is working or not.
     *
     * Description: The handler that gets information back from the BluetoothService.
     */
    @SuppressLint("HandlerLeak")
    private final Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_READ:
                    String command = (String) msg.obj;
                    txt_Result.setText(command);
                    processData(command);
                    break;
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    String status = (String) msg.obj;
                    txt_conn_stats.setText(status);
                    Log.e("LHC", "State is changed");
                    break;
                case BluetoothService.MESSAGE_MAC_ID_CHANGE:
                    txt_mac_id.setText((String) msg.obj);
                    break;
                case BluetoothService.MESSAGE_SERVER_STATE:
                    txt_serv_stats.setText((String) msg.obj);
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
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (device != null) {
                    if (mSerialConn != null) {
                        //mSerialConn.finalize();
                        MY_PERMISSION_SERIAL = false;
                        txt_usb_stats.setText("A drive is NULL");
                    }
                    Toast.makeText(getApplicationContext(),
                            "USB is disconnected", Toast.LENGTH_LONG).show();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Toast.makeText(getApplicationContext(),
                        "USB is connected", Toast.LENGTH_LONG).show();
            } else if (Constants.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //allowed permission
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
                            false)) {
                        if (device != null) {
                            MY_PERMISSION_SERIAL = true;
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

    private final BroadcastReceiver bluetoothTurnedOnOff = new BroadcastReceiver() {
        @SuppressLint("NewApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state;
            BluetoothDevice bluetoothDevice;

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_OFF) {
                        Toast.makeText(getApplicationContext(),
                                "Bluetooth is off", Toast.LENGTH_SHORT).show();
                    } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                        Toast.makeText(getApplicationContext(),
                                "Bluetooth is turning off", Toast.LENGTH_SHORT).show();
                    } else if (state == BluetoothAdapter.STATE_ON) {
                        Toast.makeText(getApplicationContext(),
                                "Bluetooth is on", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    tts.ispeak("블루투스 연결이 성공하였습니다.");
                    txt_conn_stats.setText("Connected!");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    btService.initialize();
                    tts.ispeak("블루투스 연결이 실패하였습니다.");
                    txt_conn_stats.setText("Disconnected!");
                    break;
            }
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ==
                    BluetoothAdapter.STATE_OFF) {
                txt_conn_stats.setText("Unconnected!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LHC", "Handler thread ID:" + Thread.currentThread().getId());
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        // TTS
        tts = new TtsService();
        tts.init(this);

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

        // Braille
        braille = new BrailleKeyboard(tts, this);
        bInput = findViewById(R.id.b_input);
        bMode = findViewById(R.id.b_mode);
        testInput = findViewById(R.id.test_input);

        mDimMenu = new MultiDimensionMenu(tts, this, braille);

        if (btService == null) {
            btService = new BluetoothService(this, btHandler);
        }

        mDBOpenHandler = new BluetoothDBHandler(this);
        //
        //PackageManager pkgMan = getPackageManager();
        //if (pkgMan.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        //    Log.e("LHC", "it supports BLE");
        //} else {
        //    Log.e("LHC", "it does not support BLE");
        //}

        usbHandler = new usbHandler();
        mSerialConn = new SerialConnector(getApplicationContext(), usbHandler);
        mRegDialog = new RegisterDialog(this);
        aContext = this;

        //BroadReceiver for serial connection
        IntentFilter filter = new IntentFilter("ACTION_USB_PERMISSION");
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(Constants.ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        // Get registered device information.
        updateMACID();

        // Attempts to connect Bluetooth.
        if (btService.getDeviceState()) {
            if (btService.getState() != BluetoothService.STATE_CONNECTED) {
                btService.enableBluetooth();
            }
        }

        // Monitor whether Bluetooth is turned off or not.d
        filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(bluetoothTurnedOnOff, filter);
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
                //TTS TEST
                tts.sspeak("블루투스 접속 요청되었습니다.");
                if (btService.getDeviceState()) {
                    if (btService.getState() != BluetoothService.STATE_CONNECTED) {
                        btService.enableBluetooth();
                    }
                } else {

                }
                break;
            case R.id.btn_register:
                tts.ispeak("장치 등록이 요청되었습니다. " +
                        "USB를 통해 지팡이를 연결해주세요.");
                if (mSerialConn != null) mSerialConn.finalize();
                Toast.makeText(getApplicationContext(), "USB is registering", Toast.LENGTH_LONG).show();
                mRegDialog = new RegisterDialog(this);
                mRegDialog.show();
                mRegDialogHandler.postDelayed(mDismissRunnable, Constants.TIMEOUT);

                if (mSerialConn != null && !MY_PERMISSION_SERIAL) {
                    txt_usb_stats.setText("Try to get device permission..");
                    mSerialConn.obtainPermission();
                }
                txt_usb_stats.setText("Registering..");
                mSerialConn.initialize();
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "MainActivity: onActivityResult(" + resultCode + ")");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                break;
            case REQUEST_ENABLE_BT:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSerialConn.finalize();
        mRegDialogHandler.removeCallbacks(mDismissRunnable);
        mRegDialog.dismiss();
        mRegDialog = null;

        MY_PERMISSION_SERIAL = false;
        unregisterReceiver(bluetoothTurnedOnOff);
        unregisterReceiver(mUsbReceiver);
    }

    /**
     * USB connection processing..
     */
    public class usbHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_DEVICD_INFO:  // Serial connected device information.
                    txt_usb_stats.setText((String) msg.obj);
                    break;
                case Constants.MSG_DEVICE_COUNT: // The number of connected device.
                    txt_usb_stats.setText(Integer.toString(msg.arg1) + " device(s) found \n");
                    break;
                case Constants.MSG_READ_DATA_COUNT: // The number of gotten bytes.
                    txt_usb_stats.append("Read data from a serial port (bytes): " +
                            Integer.toString(msg.arg1) + "\n");
                    break;
                case Constants.MSG_READ_DATA: // Get data and register a device.
                    if (msg.obj != null) {
                        String data = (String) msg.obj;
                        // Complete input format:
                        //   "\r20:03:04:....:02\n"
                        if (data.length() == 19) {
                            mRegDialogHandler.removeCallbacks(mDismissRunnable);
                            txt_usb_stats.setText(data);
                            String device_address = data.replaceAll("\n", "").
                                    replace("\r", "");
                            mDBOpenHandler.open();
                            mDBOpenHandler.deleteAll();
                            mDBOpenHandler.insert("target", device_address);
                            Toast.makeText(getApplicationContext(), device_address + ":" +
                                            Integer.toString(device_address.length()),
                                    Toast.LENGTH_SHORT).show();
                            txt_mac_id.setText(data);
                            if (mRegDialog != null && mRegDialog.isShowing()) {
                                mRegDialog.dismiss();
                                mRegDialogHandler.removeCallbacks(mDismissRunnable);
                                mRegDialog = null;
                            }
                            mDBOpenHandler.close();
                            mSerialConn.finalize();
                            updateMACID();
                            tts.ispeak("USB를 통해 장치 등록을 완료하였습니다.");
                        }
                    }
                    break;
                case Constants.MSG_SERIAL_ERROR: // Error statement.
                    txt_usb_stats.setText((String) msg.obj);
                    break;
                case Constants.MSG_DIALOG_HIDE: // Hide dialogue.
                    txt_usb_stats.setText("Failed to connect USB Serial Port: Timeout");
                    if (mRegDialog != null && mRegDialog.isShowing()) {
                        mRegDialog.dismiss();
                        mRegDialogHandler.removeCallbacks(mDismissRunnable);
                        mRegDialog = null;
                    }
                    break;
                case Constants.MSG_USB_CONN_SUCCESS: // Notify that serial networking is achieved.
                    // Send "MAC_ADDR\0" to the Arduino
                    try {
                        Thread.sleep(50);
                        mSerialConn.sendCommand("MAC_ADDR\0");
                        tts.ispeak("USB로 장치가 접속되었습니다.");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.MSG_CONN_FAIL:
                    break;
                case Constants.MSG_USB_NOTIFY:  // Show normal messages.
                    txt_usb_stats.append((String) msg.obj);
                    break;
            }
        }
    }

    /**
     * process command from Bluetooth.
     */
    void processData(String command) {
        // @{ Joystick --
        if (command.equals("dt")) { // top
            mDimMenu.top();
        } else if (command.equals("db")) { // down
            mDimMenu.down();
        } else if (command.equals("dl")) { // left
            mDimMenu.left();
        } else if (command.equals("dr")) { // right
            mDimMenu.right();
        } else if (command.equals("ds")) { // selected
            mDimMenu.click();
        }
        //  @} Joystick --
        //  @{ Braille keyboard
        if (Constants.KEYBOARD_MODE == Constants.BRAILLE_KEYBOARD_ON) {
            if (command.equals("b0")) {
                Log.e("LHC", "Braille key value: 0");
                brailleInput |= 0b00000001;
            } else if (command.equals("b1")) {
                Log.e("LHC", "Braille key value: 1");
                brailleInput |= 0b00000010;
            } else if (command.equals("b2")) {
                Log.e("LHC", "Braille key value: 2");
                brailleInput |= 0b00000100;
            } else if (command.equals("b3")) {
                Log.e("LHC", "Braille key value: 3");
                brailleInput |= 0b00001000;
            } else if (command.equals("b4")) {
                Log.e("LHC", "Braille key value: 4");
                brailleInput |= 0b00010000;
            } else if (command.equals("b5")) {
                Log.e("LHC", "Braille key value: 5");
                brailleInput |= 0b00100000;
            } else if (command.equals("bc")) { // complete
                braille.translateB2C(brailleInput);
                bInput.setText(BrailleKeyboard.resultString);
                testInput.setText(BrailleKeyboard.resultString);
                Log.e("LHC", "Braille key value: complete" +
                                            Integer.toBinaryString(brailleInput));
                brailleInput = 0;
            } else if (command.equals("br")) { // remove
                Log.e("LHC", "Braille key value: remove");
                braille.removeOneChar();
                bInput.setText(BrailleKeyboard.resultString);
                brailleInput = 0;
            } else if (command.equals("bra")) {
                Log.e("LHC", "Braille key value: remove all");
                braille.removeAll();
                bInput.setText(BrailleKeyboard.resultString);
                brailleInput = 0;
            } else if (command.equals("bm")) { // mode
                Log.e("LHC", "Braille key value: mode");
                braille.changeMode();
            } else if (command.equals("bd")) { // double character
                Log.e("LHC", "Braille key value: double");
                brailleInput |= 0b01000000;
            }
        }
        // @} Braille keyboard
    }
}

