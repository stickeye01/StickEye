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
import android.util.Log;

import static java.sql.Types.NULL;

public class BluetoothService {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Debugging
    private static final String TAG = "BluetoothService";

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

    // 상태를 나타내는 상태 변수
    private static final int STATE_NONE = 0; // we're doing nothing
    private static final int STATE_LISTEN = 1; // now listening for incoming
    // connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    private static final int STATE_CONNECTED = 3; // now connected to a remote
    // device

    // Constructors
    public BluetoothService(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;

        mState = STATE_NONE;

        // BluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean getDeviceState() {
        Log.i(TAG, "Check the Bluetooth support");

        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");

            return false;
        } else {
            Log.d(TAG, "Bluetooth is available");

            return true;
        }
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterface(String received_test) {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        Message m = mHandler.obtainMessage(MESSAGE_READ);
        m.obj = (Object) received_test;
        mHandler.sendMessage(m);
    }

    /**
     * Check the enabled Bluetooth
     */
    public void enableBluetooth(int dev_type) {
        Log.i(TAG, "Check the enabled Bluetooth");

        if (btAdapter.isEnabled() && dev_type ==  CLIENT_SIDE) {
            // 기기의 블루투스 상태가 On인 경우
            Log.d(TAG, "Bluetooth Enable Now: Client Side");

            // Next Step
            scanDevice();
        } else if (btAdapter.isEnabled() && dev_type == SERVER_SIDE) {
            Log.d(TAG, "Bluetooth Enable Now: ServerSide");

            startServer();
        } else {
            // 기기의 블루투스 상태가 Off인 경우
            Log.d(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    //디바이스스캔
    public void scanDevice() {
        Log.d(TAG, "Scan Device");

        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void getDeviceInfo(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(
                DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        connect(device);
    }

    // Bluetooth 상태 set
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    // Bluetooth 상태 get
    public synchronized int getState() {
        return mState;
    }

    public synchronized  void startServer() {
        mServerThread = new BtServerThread();
        mServerThread.start();
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    // ConnectThread 초기화 device의 모든 연결 제거
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);

        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // ConnectedThread 초기화
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Reset the threads @{
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
        // @}

        // Start to next phase by generating mConnectedThread
        // It will perform actual Bluetooth networking
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    // 모든 thread stop
    public synchronized void stop() {
        Log.d(TAG, "stop");

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

    // 값을 쓰는 부분(보내는 부분)
    public void write(byte[] out) { // Create temporary object
        ConnectedThread r; // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        } // Perform the write unsynchronized
        r.write(out);
    }

    // Fail to connect
    private void connectionFailed() {
        setState(STATE_NONE);
    }

    // Lost to connect
    private void connectionLost() {
        setState(STATE_NONE);

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // 디바이스 정보를 얻어서 BluetoothSocket 생성
            try {
                /*
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    Log.i("LHC", "SDK VERSION IS LOWER THAN JELLY BEAN, ret code"+tmp.toString());
                } else {
                    tmp = (BluetoothSocket) device.getClass()
                            .getMethod("createRfcommSocket", new Class[]{int.class})
                            .invoke(device, 1);
                    Log.i("LHC", "SDK VERSION IS EQUAL AND HIGHER THAN JELLY BEAN, ret code"+tmp.toString());
                }
                */
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
            setState(STATE_CONNECTING);
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Before trying connection, it always stops device searching
            // This is because device searching takes a lot of energy and makes the application slow.
            btAdapter.cancelDiscovery();

            // Try to connect BluetoothSocket
            try {
                mmSocket.connect();
                Log.d(TAG, "Connection successes");
            } catch (IOException e) {
                connectionFailed(); // 연결 실패시 불러오는 메소드
                Log.d(TAG, "Connection fails");
                e.printStackTrace();
                // socket을 닫는다.
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "Unable to close socket when connection failures",
                            e2);
                }

                // Restart Bluetooth connection phase
                // In the start() function, this additional and failed thread will be removed.
                BluetoothService.this.start();
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

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get in/out streams for Bluetooth networking
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
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            String sensor_val; // Sensor string stream that comes from Arduino

            // Keep listening to the InputStream while connection
            while (mState == STATE_CONNECTED) {
                Log.i(TAG, "Loop is started");
                // ** get byte stream values from Arduino
                try {
                    if ((bytes = mmInStream.read(buffer)) != NULL) {
                        // convert byte stream to String object
                        sensor_val = new String(buffer, "UTF-8");
                        Log.e(TAG, "GET MSG: " + sensor_val);
                        updateUserInterface(sensor_val);
                    } else {
                        Log.e(TAG, "NO MESSAGE");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                // 값을 쓰는 부분(값을 보낸다)
                mmOutStream.write(buffer);
                Log.e("LHC", "Success to send msg:"+buffer.toString());
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
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

    /// Bluetooth Server Portion
    /// @{

    private class BtServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public BtServerThread () {
            // Use a temporary object that is later assigned to mmServerSoccket
            // Because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // UUID is the app's UUID string, also used by tghe client code
                // What are the insecure and secure?
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
            // Keep listening until exception occurs or a socket is returned
            Log.d("LHC", "BEGIN mAcceptThread" + this + ", state:"+BluetoothService.this.getState());

            while (mState != STATE_CONNECTED) {
                Log.e("LHC", "starts to listen");
                try {
                    Log.e("LHC", "wait for a socket connection");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("LHC", "server connection failed");
                    break;
                }
                Log.e("LHC", "accepted");

                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    Log.e("LHC", "server connection succeed");
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation is normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e("LHC", "Could not close the socket", e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        /**
         *  Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("LHC", "close() of server failed", e);
            }
        }

                        /*
                        try {
                            mmInStream = socket.getInputStream();
                            mmOutStream = socket.getOutputStream();

                            int test_idx = 0;
                            byte[] wr_buffer = new byte[1024];
                            while (true) {
                                test_idx++;
                                String test_str = Integer.toString(test_idx);
                                wr_buffer = test_str.getBytes("UTF-8");
                                write(wr_buffer);
                                Log.e("LHC", "send message:" + test_str);
                                if (test_idx == 10000) test_idx = 0;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    */
    }

    /// @}
    /// End of Bluetooth server
}