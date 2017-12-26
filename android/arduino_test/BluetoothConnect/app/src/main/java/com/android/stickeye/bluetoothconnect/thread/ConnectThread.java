package com.android.stickeye.bluetoothconnect.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static com.android.stickeye.bluetoothconnect.MainActivity.bluetoothAdapter;

/**
 * Created by user on 2017-12-22.
 */

public class ConnectThread extends Thread {
    private final String TAG = "ConnectThread";
    Context context = null;
    Handler mainHandler = null;
    private final BluetoothSocket mmSocket;
    UUID paramString = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final BluetoothDevice mmDevice;

    private UUID getDevicesUUID(){
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid;
    }

    public ConnectThread(BluetoothDevice device, Context context, Handler handler) {
        // because mmSocket is final
        //tmp는 임시 소켓
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.context = context;
        this.mainHandler = handler;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(paramString);
            Log.v(TAG,"create temp socket and connect success");
        } catch (IOException e) {

        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        if(mmSocket.isConnected()) {
            // Do work to manage the connection (in a separate thread)

            doService();
        }
    }
    void doService(){
        Log.v(TAG,"start communicate with arduino.....");
        new ConnectedThread(mmSocket, mainHandler).start();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}