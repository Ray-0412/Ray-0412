package com.example.plantmonitor;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;

import java.util.UUID;


import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;


public class BLEService extends Service {

    Global gv;
    private final IBinder binder = new LocalBinder();


    public class LocalBinder extends Binder {
        BLEService getService() {

            return BLEService.this;
        }
    }

    public void connectgatt(BluetoothDevice device) {
        device.connectGatt(getApplicationContext(), false, gattCallback);

    }

    public void disconnectgatt(BluetoothGatt gatt) {
        gatt.disconnect();


    }

    public void senddata(byte[] txdata) {
        try {
            gv.CharacteristicHashMap.keySet().stream().forEach(x ->
                    {
                        BluetoothGattCharacteristic characteristic = gv.CharacteristicHashMap.get(x);
                        characteristic.setValue(txdata);
                        x.writeCharacteristic(characteristic);
                    }

            );
        } catch (Exception ex) {

        }


    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {

                case (BluetoothProfile.STATE_CONNECTED): {
                    gatt.discoverServices();
                    break;
                }
                case (BluetoothProfile.STATE_DISCONNECTED): {
                    gv.devicemap.put(gatt.getDevice(), false);
                    gv.CharacteristicHashMap.remove(gatt);
                    sendBroadcast(new Intent("connectionstate").putExtra("devicename", gatt.getDevice().getName()).putExtra("state", "連線中斷"));
                    break;
                }
            }


        }


        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                BluetoothGattCharacteristic chara = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                if (chara != null) {
                    gatt.setCharacteristicNotification(chara, true);

                    gv.CharacteristicHashMap.put(gatt, chara);
                    gv.devicemap.put(gatt.getDevice(), true);

                    sendBroadcast(new Intent("connectionstate").putExtra("devicename", gatt.getDevice().getName()).putExtra("state", "連線成功"));
                }
            } else {

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String AT="",AW="",SW="";

            byte[] rec = characteristic.getValue();

            for (int i = 0; i < 4; i++) {
                AT = AT+new Character((char) rec[i]).toString();
            }


            for (int i = 4; i < 8; i++) {
                AW = AW+new Character((char) rec[i]).toString();
            }
            for (int i = 8; i < 10; i++) {
                SW= SW+new Character((char) rec[i]).toString();
            }

            sendBroadcast(new Intent("dataaaaaaaaa").putExtra("AT",AT).putExtra("AW",AW).putExtra("SW",SW));

        }


    };


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        gv = (Global) getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}