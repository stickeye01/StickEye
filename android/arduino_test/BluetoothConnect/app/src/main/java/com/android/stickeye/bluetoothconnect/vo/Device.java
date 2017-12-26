package com.android.stickeye.bluetoothconnect.vo;

/**
 * Created by user on 2017-12-01.
 */

public class Device {
    String name;
    String uuid;
    boolean isBoned;
    String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isBoned() {
        return isBoned;
    }

    public void setBoned(boolean boned) {
        isBoned = boned;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Device{" +
                "name='" + name + '\'' +
                //", uuid='" + uuid + '\'' +
               // ", isBoned=" + isBoned +
                ", address='" + address + '\'' +
                '}';
    }
}
