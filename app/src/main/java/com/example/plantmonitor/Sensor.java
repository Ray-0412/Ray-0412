package com.example.plantmonitor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Sensor extends AppCompatActivity {
    private TextView sw_now_value, at_now_value, aw_now_value;
    Float sw_max_value, at_max_value, aw_max_value, sw_min_value, at_min_value, aw_min_value;
    final OkHttpClient client = new OkHttpClient();
    public ExecutorService service = Executors.newSingleThreadExecutor();
    Gson gson = new Gson();
    public MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    IntentFilter filter = new IntentFilter();
    private boolean isFirstRun = false;
    public int x = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        sw_now_value = findViewById(R.id.sw_now_value);
        at_now_value = findViewById(R.id.at_now_value);
        aw_now_value = findViewById(R.id.aw_now_value);

        filter.addAction("dataaaaaaaaa");

        SharedPreferences pref = getSharedPreferences("example", MODE_PRIVATE);
        sw_max_value = Float.parseFloat(pref.getString("swmax", ""));
        sw_min_value = Float.parseFloat(pref.getString("swmin", ""));
        at_max_value = Float.parseFloat(pref.getString("atmax", ""));
        at_min_value = Float.parseFloat(pref.getString("atmin", ""));
        aw_max_value = Float.parseFloat(pref.getString("awmax", ""));
        aw_min_value = Float.parseFloat(pref.getString("awmin", ""));
    }


    @Override
    public void onResume() {
        super.onResume();
        // 註冊mConnReceiver，並用IntentFilter設置接收的事件類型為網路開關
        registerReceiver(mConnReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 解除註冊
        unregisterReceiver(mConnReceiver);
    }


    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            sw_now_value.setText(intent.getStringExtra("SW"));
            at_now_value.setText(intent.getStringExtra("AT"));
            aw_now_value.setText(intent.getStringExtra("AW"));


            if (Float.parseFloat(sw_now_value.getText().toString()) > sw_max_value || Float.parseFloat(sw_now_value.getText().toString()) < sw_min_value) {
                sw_now_value.setTextColor(Color.RED);
                x = x + 1;

            }
            if (Float.parseFloat(at_now_value.getText().toString()) > at_max_value || Float.parseFloat(at_now_value.getText().toString()) < at_min_value) {
                at_now_value.setTextColor(Color.RED);
                x = x + 1;

            }
            if (Float.parseFloat(aw_now_value.getText().toString()) > aw_max_value || Float.parseFloat(aw_now_value.getText().toString()) < aw_min_value) {
                aw_now_value.setTextColor(Color.RED);
                x = x + 1;

            }
            if (0 < x && x < 4) {
                isFirstRun = true;
                x=4;
            } else {
                if (x > 10) {
                    isFirstRun = true;
                } else {
                    isFirstRun = false;
                }
            }
            if (isFirstRun) {

                Log.e("登入", x+"");
                x=4;
                new AlertDialog.Builder(Sensor.this)
                        .setTitle("警告").
                        setMessage("數值異常!!!").
                        setNegativeButton("確定", (dialog, which) -> {
                        })
                        .show();


            }

            Double sendat = Double.valueOf(at_now_value.getText().toString());
            Double sendaw = Double.valueOf(aw_now_value.getText().toString());
            Double sendsw = Double.valueOf(sw_now_value.getText().toString());
            service.submit(new Runnable() {
                @Override
                public void run() {

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("at", sendat);
                        jsonObject.put("aw", sendaw);
                        jsonObject.put("sw", sendsw);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                    Request request = new Request.Builder().url("http://192.168.10.114/Login/test").post(body).build();
                    try {
                        final Response response = client.newCall(request).execute();
                        final String send = response.body().string();
//                        Log.e("數值", send);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });


        }
    };
}