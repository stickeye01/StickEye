package com.example.jarim.myapplication;

/**
 * Created by Jarim on 2017-12-05.
 */

import android.bluetooth.BluetoothAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public static final int MESSAGE_SERVER_STATE = 7;

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
           // .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
            .fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter btAdapter;

    private Activity mActivity;
    private Handler mHandler;

    private ConnectThread mConnectThread; // a thread is being used for connection
    private ConnectedThread mConnectedThread; // a thread is used for communicate
    private BtServerThread mServerThread;
    private ConnectedThread mServerConnectedThread;

    private int mState;
    private int mSState;

    // State types
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing
    public static final int STATE_CONNECTED = 3; // now connected to a remote

    private String address;

    public BluetoothService(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;
        mState = STATE_NONE;
        mSState = STATE_NONE;
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
        String resultStr = "Not activated";
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
        Log.e(TAG, "Check the enabled Bluetooth");

        if (btAdapter.isEnabled() && dev_type ==  CLIENT_SIDE) {
            Log.e(TAG, "Bluetooth Enable Now: Client Side");

            scanDevice();
        } else if (btAdapter.isEnabled() && dev_type == SERVER_SIDE) {

            startServer();
        } else {
            Log.e(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    /*
     * Scan devices.
     */
    public void scanDevice() {
        Log.e(TAG, "Scan Device....");

        /*
        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        // mActivity --> DeviceListActivity --> mActivity
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
*/

        setMACID("08:D4:2B:2C:31:F5");
        setMACID("00:21:13:01:51:5D");
        setMACID("20:16:05:19:90:62");
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

        Log.e(TAG, "Get Device Info \n" + "address : " + addr);
        Log.e("LHC", "Device Info:"+device.toString());

        connect(device);
    }

    /*
     * Remove current executing threads and initialize all threads.
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.e(TAG, "connect to: " + device);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            Log.e("LHC", "connect(): mConnectedThread closed");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mServerConnectedThread != null) {
            mServerConnectedThread.cancel();
            mServerConnectedThread = null;
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

        Log.e("LHC", getStatusString(Integer.toString(mState))+"-->"+getStatusString(Integer.toString(state)));
        mState = state;
        updateUserInterface(Integer.toString(state), MESSAGE_STATE_CHANGE);
    }

    /*
     * Get the device state.
     */
    public synchronized int getState() {
        return mState;
    }


    /*
     * Set server states of the device.
     */
    private synchronized void setSState(int state) {
        mSState = state;
    }

    /*
     * Get the server state.
     */
    public synchronized int getSState() {
        return mSState;
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

        if (mServerConnectedThread != null) {
            mServerConnectedThread.cancel();
            mServerConnectedThread = null;
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
                                       BluetoothDevice device,
                                       int side_type) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        Log.e("LHC", "connected()");
        // Start to next phase by generating mConnectedThread.
        // It is going to perform actual Bluetooth networking.
        if (side_type == CLIENT_SIDE) {
            mConnectedThread = new ConnectedThread(socket, CLIENT_SIDE);
            mConnectedThread.start();
        }
        else if (side_type == SERVER_SIDE && mServerConnectedThread == null) {
            mServerConnectedThread = new ConnectedThread(socket, SERVER_SIDE);
            mServerConnectedThread.start();
        }
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

        if (mServerConnectedThread != null) {
            mServerConnectedThread.cancel();
            mServerConnectedThread = null;
        }

        if (mServerThread != null) {
            mServerThread.cancel();
            mServerThread = null;
        }

        Log.e("LHC", "stop()");
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

    private void connectionFailed(int threadType) {
        Log.e("LHC", "connectionFailed()");
        if (threadType == SERVER_SIDE)  setSState(STATE_NONE);
        else setState(STATE_NONE);

        BluetoothService.this.initialize();
        BluetoothService.this.enableBluetooth(SERVER_SIDE);
    }

    // THREAD THAT WILL BE USED WHILE CONNECTING .. @{
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            Log.e(TAG, "create ConnectThread");
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
                Log.e("LHC", "ConnectThread: try to open socket:"+tmp.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateUserInterface(device.getAddress(), MESSAGE_MAC_ID_CHANGE);
            mmSocket = tmp;
            Log.e("LHC", "connectThread socket info: "+mmSocket.toString());
            setState(STATE_CONNECTING);
        }

        public void run() {
            Log.e(TAG, "Begin mConnectThread...");
            setName("ConnectThread");

            // Before trying connection, it always needs to stop device searching process.
            // This is because device searching takes a lot of energy and makes the application slow.
            btAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.e(TAG, "Bluetooth Socket Connection succeeds!");
            } catch (IOException e) {
                Log.e("LHC", "ConnectThread exception..");
                Log.e(TAG, "ConnectThread: Bluetooth Socket Connection fails!::"+e.toString());
                Log.e(TAG, "ConnectThread: Socket Info:"+mmSocket.toString());
                e.printStackTrace();
                connectionFailed(CLIENT_SIDE);
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
            connected(mmSocket, mmDevice, CLIENT_SIDE);
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
        private final InputStreamReader mmInRStream;
        private final int threadType;

        public ConnectedThread(BluetoothSocket socket, int _threadType) {
            Log.e(TAG, "create ConnectedThread");

            mmSocket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                mmInStream = tmpIn;
                mmOutStream = tmpOut;
                mmInRStream = new InputStreamReader(mmInStream, "ASCII");
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }


            threadType = _threadType;

            if (threadType == SERVER_SIDE) {
                setSState(STATE_CONNECTED);
            }
            else if (threadType == CLIENT_SIDE) {
                setState(STATE_CONNECTED);
            }
        }

        public void run() {
            int state;
            Log.e(TAG, "Begin mConnectedThread...");
            byte[] buffer = new byte[1024];
            int bytes;
            int data_available = 0;
            String sensor_val; // Sensor string stream that comes from Arduino.

            if (threadType == SERVER_SIDE) {
                state = getSState();
            }
            else {
                state = BluetoothService.this.getState();
            }

            // Keep listening to the InputStream while connection
            while (state == STATE_CONNECTED) {
                Log.e(TAG, "mConnectedThread: waiting is started");
                try {
                    if ((data_available = mmInStream.available()) > 0) {
                        //bytes = mmInStream.read(buffer);
                        //sensor_val = new String(buffer, "UTF-8");
                        //Log.e(TAG, "GET MSG: " + sensor_val);
                        int c = mmInRStream.read();
                        switch ((char)c) {
                            case mStartDelimiter: //'\r' 전송 char을 buffer에 저장하기 시작
                                buffer = new char[MAX];
                                isStart = true;
                                index = 0;
                                break;
                            case mEndDelimiter: //'\n' buffer를 가지고 recvMessage 문자열 만듬
                +                                char[] recvChar = new char[index];
                +                                System.arraycopy(buffer, 0, recvChar, 0, recvChar.length);
                +                                recvMessage = new String(recvChar);
                +                                Log.v(TAG, "message : " + recvMessage);
                +                                isStart = false;
                +                                break;
            +                            default: //mStartDelimiter가 전송 되면 buffer에 전송된 character 저장
                +                                if (isStart) {
                    +                                    buffer[index++] = (char) c;
                    +                                }
                +                                break;
            +                        }
                        //updateUserInterface(sensor_val, MESSAGE_READ);
                    }
                    Thread.sleep(500);
                } catch (IOException e) {
                    Log.e(TAG, "["+Long.toString(this.getId())+"]ConnectedThread: Bluetooth Socket Connection fails!"+e.toString());
                    Log.e("LHC", "connectedThread socket info: "+mmSocket.toString());
                    e.printStackTrace();

                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        Log.e(TAG,
                                "Unable to close socket when connection failures",
                                e2);
                    }

                    connectionFailed(threadType);
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                Log.e("LHC", "["+Long.toString(this.getId())+"]ConnectedThread: cancel()");
                if (threadType == CLIENT_SIDE)  setState(STATE_NONE);
                else updateUserInterface("Not activated", MESSAGE_SERVER_STATE);
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
            setSState(STATE_LISTEN);
        }

        public void run() {
            BluetoothSocket socket = null;
            InputStream mmInStream = null;
            OutputStream mmOutStream = null;

            Log.e("LHC", "BtServerThread: Begin mAcceptThread" + this + ", state:"+BluetoothService.this.getState());

            while (mSState != STATE_CONNECTED) {
                Log.e("LHC", "BtServerThread: Start to listen");
                try {
                    Log.e("LHC", "BtServerThread: Wait for a socket connection");
                    updateUserInterface("Wait for connection...", MESSAGE_SERVER_STATE);
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("LHC", "BtServerThread: Server connection failed");
                    break;
                }


                if (socket != null) {
                    Log.e("LHC", "BtServerThread: Socket connection succeeds");
                    synchronized (BluetoothService.this) {
                        switch (mSState) {
                            case STATE_LISTEN:
                                connected(socket, socket.getRemoteDevice(), SERVER_SIDE);
                                updateUserInterface("Connected!", MESSAGE_SERVER_STATE);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                    //updateUserInterface("Wait for connection...", MESSAGE_SERVER_STATE);
                                    Log.e("LHC", "BtServerThread: socket closed");
                                } catch (IOException e) {
                                    Log.e("LHC",
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
                updateUserInterface("Not activated", MESSAGE_SERVER_STATE);
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("LHC", "close() of server failed", e);
            }
        }
    }
    // @} SERVER SIDE THREAD ..
}