package com.android.stickeye.bluetoothconnect.thread;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static com.android.stickeye.bluetoothconnect.Constances.MESSAGE_READ;

/**
 * Created by user on 2017-12-22.
 */
public class ConnectedThread extends Thread {
    private final String TAG = "ConnectedThread";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    protected final OutputStream mmOutStream;
    Handler mainHandler = null;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        Log.v(TAG," in ConnectedThread.........");
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.mainHandler = handler;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        BufferedInputStream bis = new BufferedInputStream(mmInStream);
        //byte[] buffer; // buffer store for the stream
        char[] buffer;
        int bytes; // bytes returned from read()
        String fromArduinoMSG = null;
        int readBufferPosition = 0;
        Log.v(TAG," Run...................");
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                int bytesAvailable = mmInStream.available();
                //읽을 값이 있다면
                if (bytesAvailable > 0) {
                    int c;
                    //buffer = new byte[1024]; //버퍼 생성
                    buffer = new char[1024];
                    Log.v(TAG, " get...................");
                    //bytes = mmInStream.read(buffer);
                    InputStreamReader isr = new InputStreamReader(mmInStream,"US-ASCII");
                    bytes = isr.read(buffer);
                    int i = (int) buffer[0]&0xff;
                    Log.v(TAG,"bytesAvailable " + bytesAvailable + " " + buffer[0]+" "+ i);
                    if(buffer[0]=='r'){
                        Log.v(TAG,"good");
                    }
                    //byte[] encodedBytes = new byte[bytesAvailable];
                    char[] encodedChars = new char[bytesAvailable];
                    //System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);
                    String recvMessage = new String(encodedChars);
                    Log.v(TAG,"message : " + recvMessage);
                   // Message msg = mainHandler.obtainMessage();
                   // msg.what = MESSAGE_READ;
                   // msg.obj = recvMessage;
                   // mainHandler.sendMessage(msg);
                    /*for (int i = 0; i <bytesAvailable ; i++) {
                        byte b = buffer[i];
                        Log.v(TAG, "byte bytes: " + b);
                        if (b == '\n') {
                            Log.v(TAG, " end...................");
                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);
                            String recvMessage = new String(encodedBytes, "ASCII");
                            readBufferPosition = 0;
                            Log.v(TAG, "msg : " + recvMessage);
                            Message msg = mainHandler.obtainMessage();
                            msg.what = MESSAGE_READ;
                            msg.obj = recvMessage;
                            mainHandler.sendMessage(msg);
                        } else {
                            buffer[readBufferPosition] = b;
                            int temp = (int) b;
                            Log.v(TAG, " " + Integer.toString(temp));
                        }
                    }*/
                }
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