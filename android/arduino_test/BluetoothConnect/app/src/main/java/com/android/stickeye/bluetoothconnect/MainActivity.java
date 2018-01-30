package com.android.stickeye.bluetoothconnect;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.stickeye.bluetoothconnect.receiver.BluetoothReceiver;
import com.android.stickeye.bluetoothconnect.service.BluetoothService;
import com.android.stickeye.bluetoothconnect.thread.ConnectThread;
import com.android.stickeye.bluetoothconnect.thread.ConnectedThread;
import com.android.stickeye.bluetoothconnect.vo.Device;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Set;

import static com.android.stickeye.bluetoothconnect.Constances.FIND_BLUETOOTH_DEVICE;
import static com.android.stickeye.bluetoothconnect.Constances.MESSAGE_READ;
import static com.android.stickeye.bluetoothconnect.Constances.REQUEST_RESTART_DISCOVERY;

public class MainActivity extends AppCompatActivity {
    public static int  REQUEST_ENABLE_BT = 1;
    public static int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    public static int MY_PERMISSIONS_REQUEST_BLUETOOTH = 2;
    public static int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 2;
    private String TAG = "MainActivity";
    public static BluetoothAdapter bluetoothAdapter = null;
    BluetoothReceiver mReceiver = null;
    boolean isStartedBluetoothService = false;
    private ListView listView;
    ArrayAdapter adapter = null;
    public static ArrayList<String> INFO = new ArrayList<String>();
    TextView textView = null;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REQUEST_RESTART_DISCOVERY:
                    startDiscovery();
                    break;
                case FIND_BLUETOOTH_DEVICE:
                    BluetoothDevice myDevice = (BluetoothDevice)msg.obj;
                    addItems(myDevice.toString());
                    if(myDevice.getName().equals("??")){
                        //paring시도
                        doConnect(myDevice);
                    }
                    break;
                case MESSAGE_READ:
                    char c = (char) msg.obj;
                    Log.v(TAG,"arrive message from arduino............."+c);
                    textView.append(Character.toString(c));
                    break;
            }
        }
    };
    void doConnect(BluetoothDevice device){
        Log.v(TAG,"doConnect");
        new ConnectThread(device, this, handler).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //listview
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,INFO);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        //textView : 데이터 송수신 시 화면에 보기 위한 텍스트뷰 초기화
        textView = (TextView) findViewById(R.id.textView);

        //Reciever
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mReceiver = new BluetoothReceiver(this,handler);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        checkPermission();
        //showDeviceList();
        //bluetooth

    }
    private void bindBluetoothService(){
        if (isAvailBluetooth()&&!isBindBluetoothService) {
            Log.v(TAG, "able use");
            if(!isStartedBluetoothService){
                Intent i =  new Intent(this,BluetoothService.class);
                startService(i);
                isStartedBluetoothService = true;
                boolean val = bindService(i, connection, Context.BIND_AUTO_CREATE);
                if(val){
                    isBindBluetoothService = true;
                    Log.v(TAG, "바인드 성공");
                }

            }
        }
    }
    public void addItems(String str){
        INFO.add(str);

        listView.setAdapter(adapter);
        Log.v(TAG, "item : "+str);
    }
    public void startDiscovery(){
        bluetoothAdapter.startDiscovery();
    }
    public void cancleDiscovery(){
        bluetoothAdapter.cancelDiscovery();
    }

    private void showPariedDeviceList(){
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        String str="";
        Log.v(TAG, "showPariedDeviceList");
        if(pairedDevice.size()>0){
            Log.v(TAG, "pairedDevice size "+ pairedDevice.size());
            for(BluetoothDevice device : pairedDevice){
                Device myDevice = new Device();
                if(device.getName().equals("??")) {
                    doConnect(device);
                }
                myDevice.setName(device.getName());
                myDevice.setAddress(device.getAddress());
                myDevice.setUuid(device.getUuids().toString());
                INFO.add("paried device : "+myDevice.toString());
            }
        }else{
            Log.v(TAG, "연결 기기 없음.");
            startDiscovery();
        }
        listView.setAdapter(adapter);
    }

    private boolean isBindBluetoothService = false;
    public static BluetoothService bluetoothService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService(handler);
            isBindBluetoothService = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBindBluetoothService = false;
        }
    };


    private void isServiceRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if (BluetoothService.class.getName().equals(runningService.service.getClassName())) {
                isStartedBluetoothService = true;
                Log.v(TAG, "서비스 이미 실행 되었음");
            }
        }
    }
    //permission 관련 코드 추가
    void checkPermission() {
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
        showPariedDeviceList();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG,"onRequestPermissionsResult ");
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION");
            }
        }else if(requestCode == REQUEST_ENABLE_BT){
            Log.v(TAG,"REQUEST_ENABLE_BT ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(connection!=null) {
            unbindService(connection);
            connection=null;
            isBindBluetoothService = false;
            isStartedBluetoothService = false;
        }
        if(bluetoothAdapter.startDiscovery()){
            bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(mReceiver);
        finish();
    }

    boolean isAvailBluetooth(){
        if(bluetoothAdapter==null){//bluetooth 지원하지 않는 폰의 경우
            return false;
        }else{//블루투스 지원하는 경우
            if(!bluetoothAdapter.isEnabled()){//블루투스가 안켜져 있는 경우
                return false;
            }else{
                return true;
            }
        }
    }
}


