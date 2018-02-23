package com.android.stickeye.bluetoothconnect.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.stickeye.bluetoothconnect.vo.Device;


import static com.android.stickeye.bluetoothconnect.Constances.FIND_BLUETOOTH_DEVICE;
import static com.android.stickeye.bluetoothconnect.Constances.REQUEST_RESTART_DISCOVERY;

public class BluetoothReceiver extends BroadcastReceiver {
    private String TAG = "BluetoothReceiver";
    private Handler mainHandler = null;
    private int counter = 10;
    public BluetoothReceiver(){

    }
    public BluetoothReceiver(Context context, Handler handler) {
        super();
        this.mainHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(BluetoothDevice.ACTION_FOUND.equals(action)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            /*Device myDevice = new Device();
            myDevice.setName(device.getName());
            myDevice.setAddress(device.getAddress());*/
            Message msg = mainHandler.obtainMessage();
            msg.what = FIND_BLUETOOTH_DEVICE;
            msg.obj = device;
            mainHandler.sendMessage(msg);
            Log.v(TAG,"블루투스 기기 검색 성공");
        }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
            Log.v(TAG,"블루투스 기기 검색 시작");
        }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.v(TAG, "블루투스 기기 검색 종료");
            if(counter < 5) {
                Message msg = mainHandler.obtainMessage();
                msg.what = REQUEST_RESTART_DISCOVERY;
                mainHandler.sendMessage(msg);
                counter++;
            }
        }
    }
}
