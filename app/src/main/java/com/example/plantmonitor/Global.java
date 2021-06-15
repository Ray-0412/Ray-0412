package com.example.plantmonitor;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.HashMap;

public class Global extends Application {

    public String username,name;
    public HashMap<BluetoothDevice, Boolean> devicemap = new HashMap();
    public HashMap<BluetoothGatt, BluetoothGattCharacteristic> CharacteristicHashMap = new HashMap<>();


}
