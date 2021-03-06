package com.example.jarim.myapplication.USBConnector;

/**
 * Created by hochan on 2018-01-06.
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.example.jarim.myapplication.Constants;

public class SerialConnector {
    public static final String tag = "SerialConnector";

    private Context mContext;
    private Handler mHandler;

    private SerialConnectingThread mConnectingThread;
    private SerialMonitorThread mSerialThread;

    private USBSerialDriver mDriver;
    private USBSerialPort mPort;

    public static final int TARGET_VENDOR_ID = 9025;	// Arduino
    public static final int TARGET_VENDOR_ID2 = 1659;	// PL2303
    public static final int TARGET_VENDOR_ID3 = 1027;	// FT232R
    public static final int TARGET_VENDOR_ID4 = 6790;	// CH340G
    public static final int TARGET_VENDOR_ID5 = 4292;	// CP210x
    public static final int BAUD_RATE = 115200;
    /*****************************************************
     *	Constructor, Initialize
     ******************************************************/
    public SerialConnector(Context c, Handler h) {
        mContext = c;
        mHandler = h;
    }


    public void initialize() {
        // Everything is fine. Start serial monitoring thread.
        finalize();
        startConnectingThread();
    }	// End of initialize()

    public void finalize() {
        try {
            mDriver = null;
            stopThread();
            if(mPort != null) {
                mPort.close();
                mPort = null;
            }
        } catch(Exception ex) {
            Message msg1 = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Error: Cannot finalize serial connector \n" + ex.toString() + "\n");
            mHandler.sendMessage(msg1);
        }
    }

    public void obtainPermission(){
        Message msg = null;
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        if (manager == null)
            manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        List<USBSerialDriver> availableDrivers = USBSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0,
                    "Error # A drive is NULL \n");
            mHandler.sendMessage(msg);
            return;
        }
        mDriver = availableDrivers.get(0);
        if (mDriver == null) {
            msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0,
                    "Error # B drive is NULL \n");
            mHandler.sendMessage(msg);
            return;
        }
        UsbDevice device = mDriver.getDevice();
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(Constants.ACTION_USB_PERMISSION), 0);
        manager.requestPermission(device, mPermissionIntent);
        msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0,
                "Request USB connection\n");
        mHandler.sendMessage(msg);
    }



    /*****************************************************
     *	public methods
     ******************************************************/
    // send string to remote
    public void sendCommand(String cmd) {
        if(mPort != null && cmd != null) {
            try {
                Log.e("LHC", "send command: "+cmd);
                Message msg1 = mHandler.obtainMessage(Constants.MSG_USB_NOTIFY, 0, 0, "device"+mPort.getDriver()+"\nSend command: "+cmd+"\n");
                mHandler.sendMessage(msg1);
                mPort.write(cmd.getBytes(), cmd.length());		// Send to remote device
            }
            catch(IOException e) {
                Message msg1 = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Failed in sending command. : IO Exception:"+e.toString()+"\n");
                mHandler.sendMessage(msg1);
            }
        }
    }


    /*****************************************************
     *	private methods
     ******************************************************/
    // attempt to connect
    private void startConnectingThread() {
        Log.d(tag, "Start a thread that attempts to connect serial networking");
        if (mConnectingThread != null) {
            mConnectingThread.interrupt();
            mConnectingThread = null;
        }
        if (mSerialThread != null) {
            mSerialThread.interrupt();
            mSerialThread = null;
        }

        mConnectingThread = new SerialConnectingThread();
        mConnectingThread.start();
    }

    // start thread
    private void startConnectedThread() {
        Log.d(tag, "Start serial monitoring thread");
        Message msg1 = mHandler.obtainMessage(Constants.MSG_USB_NOTIFY, 0, 0, "Start serial monitoring thread \n");
        mHandler.sendMessage(msg1);
        if(mSerialThread == null) {
            mSerialThread = new SerialMonitorThread();
            mSerialThread.start();
        }
        if(mConnectingThread != null && mConnectingThread.isAlive())
            mConnectingThread.interrupt();
        if(mConnectingThread != null) {
            mConnectingThread = null;
        }
    }

    // stop thread
    private void stopThread() {
        if(mSerialThread != null && mSerialThread.isAlive())
            mSerialThread.interrupt();
        if(mSerialThread != null) {
            mSerialThread.setKillSign(true);
            mSerialThread = null;
        }
        if(mConnectingThread != null && mConnectingThread.isAlive())
            mConnectingThread.interrupt();
        if(mConnectingThread != null) {
            mConnectingThread = null;
        }
    }


    public class SerialConnectingThread extends Thread {
        /**
         *	Main loop
         **/
        @Override
        public void run()
        {
            int startTime = 0;
            boolean is_success = false;
            while (!Thread.interrupted()) {
                if (startTime >= Constants.TIMEOUT) break;
                if (this.initialize()) {
                    is_success = true;
                    break;
                }
                startTime += 10;
                SystemClock.sleep(10);
                Log.e("LHC", "timer1: "+Integer.toString(startTime));
            }

            if (is_success) startConnectedThread();
            else {
                Message msg = mHandler.obtainMessage(Constants.MSG_USB_NOTIFY, 0, 0, "ConnectingThread: failed to initialize");
                mHandler.sendMessage(msg);
            }
        }	// End of run()

        private boolean initialize()
        {
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            Message msg;

            List<USBSerialDriver> availableDrivers = USBSerialProber.getDefaultProber().findAllDrivers(manager);
            if (availableDrivers.isEmpty()) {
                msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Error # A drive is NULL \n");
                mHandler.sendMessage(msg);
                return false;
            }

            mDriver = availableDrivers.get(0);
            if(mDriver == null) {
                msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Error # A drive is NULL \n");
                mHandler.sendMessage(msg);
                return false;
            }

            // Report to UI
            StringBuilder sb = new StringBuilder();
            UsbDevice device = mDriver.getDevice();
            sb.append(" DName : ").append(device.getDeviceName()).append("\n")
                    .append(" DID : ").append(device.getDeviceId()).append("\n")
                    .append(" VID : ").append(device.getVendorId()).append("\n")
                    .append(" PID : ").append(device.getProductId()).append("\n")
                    .append(" IF Count : ").append(device.getInterfaceCount()).append("\n");
            msg = mHandler.obtainMessage(Constants.MSG_DEVICD_INFO, sb.toString());
            mHandler.sendMessage(msg);

            UsbDeviceConnection connection = manager.openDevice(device);
            if (connection == null) {
                msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Error # mPort is NULL \n");
                mHandler.sendMessage(msg);
                return false;
            }

            // Read some data! Most have just one port (port 0).
            mPort = mDriver.getPorts().get(0);
            if(mPort == null) {
                msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Error # mPort is NULL \n");
                mHandler.sendMessage(msg);
                return false;
            }

            try {
                mPort.open(connection);
                mPort.setParameters(9600, 8, 1, 0);		// baudrate:9600, dataBits:8, stopBits:1, parity:N
//			byte buffer[] = new byte[16];
//			int numBytesRead = mPort.read(buffer, 1000);
//			Log.d(TAG, "Read " + numBytesRead + " bytes.");
            } catch (IOException e) {
                // Deal with error.
                Log.e("LHC", "initialize error");
                msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Error # run: " + e.toString() + "\n");
                mHandler.sendMessage(msg);
                return false;
            } finally {
            }
            msg = mHandler.obtainMessage(Constants.MSG_USB_NOTIFY, 0, 0, "USB port is activated\n");
            mHandler.sendMessage(msg);


            msg = mHandler.obtainMessage(Constants.MSG_USB_CONN_SUCCESS);
            mHandler.sendMessage(msg);

            return true;
        }

    }	// End of SerialMonitorThread




    /*****************************************************
     *	Sub classes, Handler, Listener
     ******************************************************/

    public class SerialMonitorThread extends Thread {
        // Thread status
        private boolean mKillSign = false;
        private SerialCommand mCmd = new SerialCommand();


        private void finalizeThread() {
            stopThread();
        }

        // stop this thread
        public void setKillSign(boolean isTrue) {
            mKillSign = isTrue;
        }

        /**
         *	Main loop
         **/
        @Override
        public void run()
        {
            byte buffer[] = new byte[1000];
            Message msg;
            final char mEndDelimiter = '\n';
            final char mStartDelimiter = '\r';
            boolean isStart = false;
            int startTime = 0;
            Log.e("LHC", "monitor Handler ID:"+Thread.currentThread().getId());

            while(!Thread.interrupted())
            {
                if(startTime >= Constants.TIMEOUT)  {
                    mKillSign = true;
                    msg = mHandler.obtainMessage(Constants.MSG_DIALOG_HIDE);
                    mHandler.sendMessage(msg);
                }

                if (mKillSign) {
                    break;
                }

                if(mPort != null) {
                    Log.e("LHC", "mPort is not null in SerialMonitorThread");
                    Arrays.fill(buffer, (byte)0x00);
                    try {
                        // Read received buffer
                        int numBytesRead = mPort.read(buffer, 1000);
                        msg = mHandler.obtainMessage(Constants.MSG_USB_NOTIFY, 0, 0, "\nWait for data...\n");
                        mHandler.sendMessage(msg);
                        if(numBytesRead > 0) {
                            Log.e(tag, "run : read bytes = " + numBytesRead);
                            Log.e("LHC", "run : read data = " + new String(buffer));
                            // Print message length
                            msg = mHandler.obtainMessage(Constants.MSG_READ_DATA_COUNT, numBytesRead, 0,
                                    new String(buffer));
                            mHandler.sendMessage(msg);

                            // Extract data from buffer
                            for(int i=0; i<numBytesRead; i++) {
                                char c = (char)buffer[i];
                                switch (c) {
                                    case mStartDelimiter:
                                        mCmd.initialize();
                                        mCmd.addChar(c);
                                        isStart = true;
                                        break;
                                    case mEndDelimiter:
                                        mCmd.addChar(c);
                                        msg = mHandler.obtainMessage(Constants.MSG_READ_DATA, 0, 0, mCmd.toString());
                                        mHandler.sendMessage(msg);
                                        isStart = false;
                                        setKillSign(true);
                                        break;
                                    default:
                                        if (isStart) mCmd.addChar(c);
                                }
                            }
                        } // End of if(numBytesRead > 0)
                    }
                    catch (IOException e) {
                        Log.e(tag, "IOException - mDriver.read: "+e.toString());
                        msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Error # run: " + e.toString() + "\n");
                        mHandler.sendMessage(msg);
                        mKillSign = true;
                    }
                } else {
                    msg = mHandler.obtainMessage(Constants.MSG_USB_NOTIFY, 0, 0, "Port is null..\n");
                    mHandler.sendMessage(msg);
                }

                try {
                    startTime += 10;
                    Thread.sleep(10);
                    Log.e("LHC", "timer:"+Integer.toString(startTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }	// End of while() loop

            // Finalize
            finalizeThread();

        }	// End of run()


    }	// End of SerialMonitorThread

}
