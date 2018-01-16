package com.example.jarim.myapplication.USBConnector;

/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */


import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class USBSerialProber {

    private final ProbeTable mProbeTable;

    public USBSerialProber(ProbeTable probeTable) {
        mProbeTable = probeTable;
    }

    public static USBSerialProber getDefaultProber() {
        return new USBSerialProber(getDefaultProbeTable());
    }

    public static ProbeTable getDefaultProbeTable() {
        final ProbeTable probeTable = new ProbeTable();
        probeTable.addDriver(CdcAcmSerialDriver.class);
        probeTable.addDriver(Cp21xxSerialDriver.class);
        probeTable.addDriver(FtdiSerialDriver.class);
        probeTable.addDriver(ProlificSerialDriver.class);
        probeTable.addDriver(CH34xSerialDriver.class);
        return probeTable;
    }

    /**
     * Finds and builds all possible {@link USBSerialDriver UsbSerialDrivers}
     * from the currently-attached {@link UsbDevice} hierarchy. This method does
     * not require permission from the Android USB system, since it does not
     * open any of the devices.
     *
     * @param usbManager
     * @return a list, possibly empty, of all compatible drivers
     */
    public List<USBSerialDriver> findAllDrivers(final UsbManager usbManager) {
        final List<USBSerialDriver> result = new ArrayList<USBSerialDriver>();

        for (final UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            final USBSerialDriver driver = probeDevice(usbDevice);
            if (driver != null) {
                result.add(driver);
            }
        }
        return result;
    }

    /**
     * Probes a single device for a compatible driver.
     *
     * @param usbDevice the usb device to probe
     * @return a new {@link USBSerialDriver} compatible with this device, or
     *         {@code null} if none available.
     */
    public USBSerialDriver probeDevice(final UsbDevice usbDevice) {
        final int vendorId = usbDevice.getVendorId();
        final int productId = usbDevice.getProductId();

        final Class<? extends USBSerialDriver> driverClass =
                mProbeTable.findDriver(vendorId, productId);
        if (driverClass != null) {
            final USBSerialDriver driver;
            try {
                final Constructor<? extends USBSerialDriver> ctor =
                        driverClass.getConstructor(UsbDevice.class);
                driver = ctor.newInstance(usbDevice);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return driver;
        }
        return null;
    }

}