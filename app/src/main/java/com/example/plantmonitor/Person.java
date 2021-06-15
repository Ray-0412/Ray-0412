package com.example.plantmonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Person extends AppCompatActivity {


    private TextView person_name,person_account;
    final OkHttpClient client = new OkHttpClient();
    public ExecutorService service = Executors.newSingleThreadExecutor();
    public MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    Gson gson = new Gson();
    Global gv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        gv = (Global) getApplicationContext();
        person_name=findViewById(R.id.person_name);
        person_account=findViewById(R.id.person_account);
        person_name.setText(gv.name);
        person_account.setText(gv.username);



    }

}