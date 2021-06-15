package com.example.plantmonitor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ScanBLE extends AppCompatActivity {

    private SwipeRefreshLayout rescan;
    private BluetoothLeScanner BleScanner;
    private BluetoothAdapter BleAdapter;
    BLEService mService;
    Global gv;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_b_l_e);

        Toast.makeText(getApplicationContext(),"往下滑來掃描裝置",Toast.LENGTH_LONG).show();


        if (!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getBaseContext(), "No_sup_ble", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        gv = (Global) getApplicationContext();


        rescan = findViewById(R.id.refresh);

        BleScanner = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().getBluetoothLeScanner();
        BleAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView1);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(gv.devicemap);
        mRecyclerView.setAdapter(mAdapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction("connectionstate");


        registerReceiver(gattUpdateReceiver, filter);

        rescan.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (BleAdapter.isEnabled()) {
                    List<BluetoothDevice> tmp = gv.devicemap.keySet().stream().filter(x -> !gv.devicemap.get(x)).collect(Collectors.toList());
                    tmp.stream().forEach(x -> gv.devicemap.remove(x));
                    mAdapter.notifyDataSetChanged();
                    BleAdapter.startLeScan(scanCallback);
                    rescan.setRefreshing(true);


                    new Handler().postDelayed(() -> {
                        BleAdapter.stopLeScan(scanCallback);
                        rescan.setRefreshing(false);
                    }, 3000);

                } else {
                    Toast.makeText(getApplicationContext(), "請先啟動藍芽", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals("connectionstate")) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getApplicationContext(), intent.getStringExtra("devicename") + " " + intent.getStringExtra("state"), Toast.LENGTH_SHORT).show();
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }


        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(ScanBLE.this, BLEService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        unregisterReceiver(gattUpdateReceiver);

    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
            mService = binder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };






    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            setResult(2);
        }
        return super.onKeyDown(keyCode, event);
    }


    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {


            if (!gv.devicemap.keySet().contains(bluetoothDevice) && bluetoothDevice.getName() != null) {
                gv.devicemap.put(bluetoothDevice, false);
                runOnUiThread(() -> mAdapter.notifyDataSetChanged());

            }
        }
    };

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private HashMap<BluetoothDevice, Boolean> mdevicemap;


        public MyAdapter(HashMap<BluetoothDevice, Boolean> devicemap) {
            mdevicemap = devicemap;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView device_name;
            public TextView device_address;
            public ImageView imageView;

            public ViewHolder(View v) {
                super(v);
                device_name = v.findViewById(R.id.device_name);
                device_address = v.findViewById(R.id.device_address);
                imageView = v.findViewById(R.id.imageView);
            }
        }


        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scandevicecardview, parent, false);
            final ViewHolder vh = new ViewHolder(v);

            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE)).vibrate(50);

                    final int position = vh.getAdapterPosition();
                    BluetoothDevice device = new ArrayList<>(mdevicemap.keySet()).get(position);
                    if(!gv.devicemap.get(device)){
                        mService.connectgatt(device);
                    }

                }
            });
            vh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


                    final int position = vh.getAdapterPosition();
                    BluetoothDevice device = new ArrayList<>(mdevicemap.keySet()).get(position);
                    if(!gv.devicemap.get(device)){return true;}
                    new AlertDialog.Builder(ScanBLE.this).setCancelable(false).
                            setTitle("中斷連線").
                            setMessage("要中斷 "+device.getName()+"連線?").
                            setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
                                public void onClick(DialogInterface dialog, int i) {
                                    BluetoothGatt gatt = gv.CharacteristicHashMap.keySet().stream().filter(x->x.getDevice().getAddress().equals(device.getAddress())).findFirst().get();
                                    mService.disconnectgatt(gatt);
                                }
                            }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int i) {

                        }
                    }).show();
                    return true;
                }
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            BluetoothDevice device = new ArrayList<>(mdevicemap.keySet()).get(position);


            holder.device_name.setText(device.getName());
            holder.device_address.setText(device.getAddress());

            holder.imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), mdevicemap.get(device) ? R.drawable.green : R.drawable.red));


        }

        @Override
        public int getItemCount() {
            return mdevicemap.size();
        }
    }



}