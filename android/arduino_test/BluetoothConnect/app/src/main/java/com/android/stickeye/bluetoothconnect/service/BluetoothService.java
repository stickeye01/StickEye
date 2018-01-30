package com.android.stickeye.bluetoothconnect.service;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.stickeye.bluetoothconnect.vo.Device;

import static com.android.stickeye.bluetoothconnect.MainActivity.INFO;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BluetoothService extends Service {
    private String TAG = "BluetoothService";
    private final IBinder LocalBinder = new LocalBinder();
    private boolean isConnectDevice;
    private int version ;
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        //startDiscovery();
        return LocalBinder;
    }

    private static Handler mainHandler;
    public class LocalBinder extends Binder {
        public BluetoothService getService(Handler handler){
            BluetoothService.mainHandler = handler;
            return BluetoothService.this;
        }
    }
/*
    void startDiscovery(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.v(TAG,"start to discovery");
        }else{
            Log.v(TAG,"because build version, scanner cannot be used");
        }
    }*/
    //페어링된 기기 쿼리
    /*void showPariedDeviceList(){
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        String str="";
        if(pairedDevice.size()>0){
            for(BluetoothDevice device : pairedDevice){
                str+=" "+device.getName();
            }
        }
    }*/

/*    public void discoveryDevice(){
        bluetoothLeScanner.startScan(mScanCallback);

    }*/

    String[] adresses = new String[20];
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.v(TAG,"onScanResult");
            BluetoothDevice device = result.getDevice();
            boolean val = true;
            int index = 0;
            for(int i = 0 ; i < index ;i++){
                if(adresses[i].equals(device.getAddress())){
                    val = false;
                }
            }

            if(val) {
                adresses[index++]=device.getAddress();
                Device myDevice = new Device();
                myDevice.setName(device.getName());
                myDevice.setAddress(device.getAddress());
                Log.v(TAG, myDevice.toString());
                INFO.add(myDevice.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onCreate()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        //bluetoothLeScanner.stopScan(mScanCallback);
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onLowMemory()");
        //bluetoothLeScanner.stopScan(mScanCallback);
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onLowMemory()");
        super.onRebind(intent);
    }
}
