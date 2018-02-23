package com.android.stickeye.bluetoothconnect.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BluetoothClientService extends Service {
    public BluetoothClientService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
