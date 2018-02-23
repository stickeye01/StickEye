package com.android.stickeye.bluetoothconnect.thread;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Timer;

import static com.android.stickeye.bluetoothconnect.Constances.MESSAGE_READ;

/**
 * Created by user on 2017-12-22.
 */
public class ConnectedThread extends Thread {
    private final String TAG = "ConnectedThread";
    final int MAX = 64;
    private char[] buffer = new char[MAX];
    private final BluetoothSocket mmSocket;
    private InputStreamReader isr;
    private final InputStream mmInStream;
    protected final OutputStream mmOutStream;
    boolean isStart = false;
    final char mEndDelimiter = '\n';
    final char mStartDelimiter = '\r';
    Handler mainHandler = null;


    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        Log.v(TAG," in ConnectedThread.........");
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        InputStreamReader isr = null;
        this.mainHandler = handler;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG,"IOException");
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        int index = 0;
        long currentTime = 0;
        long startTime = 0;
        String recvMessage = null;
        Log.v(TAG," Run...................");
        try {
            isr  = new InputStreamReader(mmInStream,"ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                int bytesAvailable = mmInStream.available();
                //읽을 값이 있다면
                if(bytesAvailable>0){
                    Log.v(TAG, " get...................");
                    int c = isr.read();
                    Log.v(TAG, "bytesAvailable " + bytesAvailable + " " + (char) c);
                    switch ((char)c) {
                        case mStartDelimiter:
                           // Log.v(TAG, "bytesAvailable " + bytesAvailable + " " + (char) c);
                            buffer = new char[MAX];
                            isStart = true;
                            index = 0;
                            startTime = System.currentTimeMillis();
                            currentTime = startTime;
                            break;
                        case mEndDelimiter:
                            //Log.v(TAG, "bytesAvailable " + bytesAvailable + " " + (char) c);
                            char[] recvChar = new char[index];
                            System.arraycopy(buffer, 0, recvChar, 0, recvChar.length);
                            recvMessage = new String(recvChar);
                            Log.v(TAG, "message : " + recvMessage);
                            isStart = false;
                            break;
                        default:
                            if (isStart) {
                                buffer[index++] = (char) c;
                               // Log.v(TAG, "bytesAvailable " + bytesAvailable + " " + (char) c);
                            }
                            break;
                         }
                    }

                       //Message msg = mainHandler.obtainMessage();
                       //msg.what = MESSAGE_READ;
                       //msg.obj = recvMessage;
                       // mainHandler.sendMessage(msg);
                } catch(IOException e){
                    Log.v(TAG, "error: " + e.toString());
                }
        }
    }
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}