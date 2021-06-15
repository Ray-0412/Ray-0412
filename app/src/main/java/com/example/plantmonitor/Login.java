package com.example.plantmonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Login extends AppCompatActivity {
    private EditText account;
    private EditText password;
    private Button login;
    final OkHttpClient client = new OkHttpClient();
    public ExecutorService service = Executors.newSingleThreadExecutor();
    Gson gson = new Gson();
    Global gv;
    public MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        account = findViewById(R.id.accountEditText);
        password = findViewById(R.id.passwordEdittext);
        login = findViewById(R.id.Login_Button);
        gv = (Global) getApplicationContext();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginname = account.getText().toString();
                String loginpassword = password.getText().toString();
                service.submit(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("username", loginname);
                            jsonObject.put("password", loginpassword);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                        Request request = new Request.Builder().url("http://192.168.10.114/Login/login").post(body).build();
                        try {
                            final Response response = client.newCall(request).execute();
                            final String resStr = response.body().string();
                            JSONObject responseobject = new JSONObject(resStr);
                            int status = responseobject.getInt("status");
                            Log.e("登入", resStr);
                            if (status == 1) {
                                gv.name = responseobject.getString("username");
                                gv.username=loginname;
                                startActivity(new Intent(Login.this, MainActivity.class));
                            } else {
                                Toast.makeText(getApplicationContext(), "帳號密碼錯誤", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

            }
        });

    }
}