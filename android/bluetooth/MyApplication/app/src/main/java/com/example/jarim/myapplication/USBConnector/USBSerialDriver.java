package com.example.jarim.myapplication.USBConnector;

import android.hardware.usb.UsbDevice;

import java.util.List;

/**
 * Created by hochan on 2018-01-05.
 */

public interface USBSerialDriver {

    /**
     * Returns the raw {@link UsbDevice} backing this port.
     *
     * @return the device
     */
    public UsbDevice getDevice();

    /**
     * Returns all available ports for this device. This list must have at least
     * one entry.
     *
     * @return the ports
     */
    public List<USBSerialPort> getPorts();
}
