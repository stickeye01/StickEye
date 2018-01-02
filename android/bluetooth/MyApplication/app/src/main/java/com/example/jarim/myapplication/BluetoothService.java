package com.example.jarim.myapplication;

/**
 * Created by Jarim on 2017-12-05.
 */

import android.bluetooth.BluetoothAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import static java.sql.Types.NULL;

public class BluetoothService {
    // Message types that are sent by the BluetoothChatService handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_MAC_ID_CHANGE = 6;

    // Key names received from the BluetoothChatService handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final String TAG = "BluetoothService";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int SERVER_SIDE = 1;
    private static final int CLIENT_SIDE = 2;

    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private BluetoothAdapter btAdapter;

    private Activity mActivity;
    private Handler mHandler;

    private ConnectThread mConnectThread; // a thread is being used for connection
    private ConnectedThread mConnectedThread; // a thread is used for communicate
    private BtServerThread mServerThread;

    private int mState;
    private int mNewState;

    // State types
    private static final int STATE_NONE = 0; // we're doing nothing
    private static final int STATE_LISTEN = 1; // now listening for incoming
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing
    private static final int STATE_CONNECTED = 3; // now connected to a remote

    private String address;

    public BluetoothService(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;
        mState = STATE_NONE;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /*
     * Check whether Bluetooth is available or not.
     */
    public boolean getDeviceState() {
        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");
            return false;
        } else {
            Log.d(TAG, "Bluetooth is available");
            return true;
        }
    }

    /*
     * Change UI state.
     */
    private String getStatusString(String data) {
        int type = Integer.parseInt(data);
        String resultStr = "NONE";
        switch (type) {
            case STATE_NONE:
                return resultStr;
            case STATE_CONNECTING:
                resultStr = "Connecting...";
                return resultStr;
            case STATE_CONNECTED:
                resultStr = "Connected!";
                return resultStr;
            case STATE_LISTEN:
                resultStr = "Connected! and wait for data..";
                return resultStr;
        }

        return resultStr;
    }

    /*
     * Update UI title according to the current state of the chat connection.
     */
    private synchronized void updateUserInterface(String data, int msg_type) {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle(): " + mNewState + " -> " + mState);
        mNewState = mState;

        if (msg_type == MESSAGE_STATE_CHANGE) {
            data = getStatusString(data);
        }

        // Give the new state to the Handler so the UI Activity can be updated.
        Message m = mHandler.obtainMessage(msg_type);
        m.obj = (Object) data;
        mHandler.sendMessage(m);
    }

    /*
     * Check the enabled Bluetooth.
     */
    public void enableBluetooth(int dev_type) {
        Log.i(TAG, "Check the enabled Bluetooth");

        if (btAdapter.isEnabled() && dev_type ==  CLIENT_SIDE) {
            Log.d(TAG, "Bluetooth Enable Now: Client Side");

            scanDevice();
        } else if (btAdapter.isEnabled() && dev_type == SERVER_SIDE) {

            startServer();
        } else {
            Log.d(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    /*
     * Scan devices.
     */
    public void scanDevice() {
        Log.d(TAG, "Scan Device....");

        /*
        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        // mActivity --> DeviceListActivity --> mActivity
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        */
        setMACID("08:D4:2B:2C:31:F5");
        getDeviceInfo(address);
    }

    /*
     * Get the device's MAC address and try to connect it.
     */
    public void getDeviceInfo(Intent data) {
        String address = data.getExtras().getString(
                DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        connect(device);
    }

    /*
     * Get the device's MAC address and try to connect it.
     */
    public void getDeviceInfo(String addr) {
        BluetoothDevice device = btAdapter.getRemoteDevice(addr);

        Log.d(TAG, "Get Device Info \n" + "address : " + addr);

        connect(device);
    }

    /*
     * Remove current executing threads and initialize all threads.
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mServerThread != null) {
            mServerThread.cancel();
            mServerThread = null;
        }

        // Start a thread that connects with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

        setState(STATE_CONNECTING);
    }

    private void setMACID(String macId) {
        address = macId;
    }

    private String getMACID() {
        return address;
    }

    /*
     * Set states of the device.
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        updateUserInterface(Integer.toString(mState), MESSAGE_STATE_CHANGE);
    }

    /*
     * Get the device state.
     */
    public synchronized int getState() {
        return mState;
    }

    public synchronized  void startServer() {
        mServerThread = new BtServerThread();
        mServerThread.start();
    }

    /*
     * Nullify all the threads.
     */
    public synchronized void initialize() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mServerThread != null) {
            mServerThread.cancel();
            mServerThread = null;
        }
    }

    /*
     * Nullify all the threads and start to connect with the device.
     */
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mServerThread != null) {
            mServerThread.cancel();
            mServerThread = null;
        }

        // Start to next phase by generating mConnectedThread.
        // It is going to perform actual Bluetooth networking.
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /*
     * Stop and nullify all the threads
     */
    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mServerThread != null) {
            mServerThread.cancel();
            mServerThread = null;
        }

        setState(STATE_NONE);
    }

    /*
     * Write out data through socket output stream.
     * (Send data to other devices if the connection is set)
     */
    public void write(byte[] out) {
        ConnectedThread r; // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private void connectionFailed() {
        setState(STATE_NONE);
    }
    private void connectionLost() { setState(STATE_NONE); }

    // THREAD THAT WILL BE USED WHILE CONNECTING .. @{
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Create Bluetooth socket.
            try {
                /*
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    Log.e("LHC", "device information:\n"+device.toString());
                    Log.i("LHC", "SDK VERSION IS LOWER THAN JELLY BEAN, ret code"+tmp.toString());
                } else {
                    tmp = (BluetoothSocket) device.getClass()
                            .getMethod("createRfcommSocket", new Class[]{int.class})
                            .invoke(device, 1);
                    Log.e("LHC", "device information:\n"+device.toString());
                    Log.i("LHC", "SDK VERSION IS EQUAL AND HIGHER THAN JELLY BEAN, ret code"+tmp.toString());
                }
                */
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateUserInterface(device.getAddress(), MESSAGE_MAC_ID_CHANGE);
            mmSocket = tmp;
            setState(STATE_CONNECTING);
        }

        public void run() {
            Log.i(TAG, "Begin mConnectThread...");
            setName("ConnectThread");

            // Before trying connection, it always needs to stop device searching process.
            // This is because device searching takes a lot of energy and makes the application slow.
            btAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.d(TAG, "Bluetooth Socket Connection succeeds!");
            } catch (IOException e) {
                connectionFailed();
                Log.d(TAG, "Bluetooth Socket Connection fails!");
                e.printStackTrace();

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "Unable to close socket when connection failures",
                            e2);
                }
                BluetoothService.this.initialize();
                return;
            }

            // Reset ConnectTread class
            // However, I think it is tricky point.
            // Is it good idea to nullify the thread in itself?
            // [WARRING]
            // 왜 쓰는 지 알겠음,
            // connected()에서는 thread 정리를 수행함.
            // 이때, mConnectThread가 NULL이 아닐 경우에는,
            // socket을 clear하는 데,
            // 그것을 방지하기 위해 본인 쓰레드에서 수정하는 중.
            // 별로 좋은 방법은 아닌듯.
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            //
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    // @} THREAD THAT WILL BE USED WHILE CONNECTING ..

    // THREAD THAT WILL BE USED AFTER CONNECTING .. @{
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");

            mmSocket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            setState(STATE_CONNECTED);
        }

        public void run() {
            Log.i(TAG, "Begin mConnectedThread...");
            byte[] buffer = new byte[1024];
            int bytes;
            String sensor_val; // Sensor string stream that comes from Arduino.

            // Keep listening to the InputStream while connection
            while (mState == STATE_CONNECTED) {
                Log.i(TAG, "mConnectedThread: waiting is started");
                try {
                    if ((bytes = mmInStream.read(buffer)) != NULL) {
                        sensor_val = new String(buffer, "UTF-8");
                        Log.e(TAG, "GET MSG: " + sensor_val);
                        updateUserInterface(sensor_val, MESSAGE_READ);
                    } else {
                        Log.e(TAG, "NO MESSAGE");
                    }
                } catch (IOException e) {
                    connectionFailed();
                    Log.d(TAG, "Bluetooth Socket Connection fails!");
                    e.printStackTrace();

                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        Log.e(TAG,
                                "Unable to close socket when connection failures",
                                e2);
                    }
                    BluetoothService.this.initialize();
                    return;
                }
                SystemClock.sleep(100);
            }
        }

        /*
         * Write data onto write output stream.
         * (it means that we can send data to other devices)
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                Log.e("LHC", "Succeed to send msg:"+buffer.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    // @} THREAD THAT WILL BE USED AFTER CONNECTING ..

    // SERVER SIDE THREAD .. @{
    private class BtServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public BtServerThread () {
            BluetoothServerSocket tmp = null;
            try {
                // UUID is the app's UUID string, also used by tghe client code
                // What are the insecure and secure? [NEED TO SOLVE]
                tmp = btAdapter.listenUsingRfcommWithServiceRecord("StickEyeServer", MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            BluetoothSocket socket = null;
            InputStream mmInStream = null;
            OutputStream mmOutStream = null;

            Log.v("LHC", "BtServerThread: Begin mAcceptThread" + this + ", state:"+BluetoothService.this.getState());

            while (mState != STATE_CONNECTED) {
                Log.v("LHC", "BtServerThread: Start to listen");
                try {
                    Log.v("LHC", "BtServerThread: Wait for a socket connection");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v("LHC", "BtServerThread: Server connection failed");
                    break;
                }


                if (socket != null) {
                    Log.v("LHC", "BtServerThread: Socket connection succeeds");
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.v("LHC",
                                            "BtServerThread: Could not close the socket",
                                            e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("LHC", "close() of server failed", e);
            }
        }
    }
    // @} SERVER SIDE THREAD ..
}