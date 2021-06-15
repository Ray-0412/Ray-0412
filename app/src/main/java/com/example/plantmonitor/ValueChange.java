package com.example.plantmonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ValueChange extends AppCompatActivity {
    private EditText sw_max_value, sw_min_value, at_max_value, at_min_value, aw_max_value, aw_min_value;
    private Button savebutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value_change);

        savebutton = findViewById(R.id.save_button);
        sw_max_value = findViewById(R.id.sw_max_value);
        sw_min_value = findViewById(R.id.sw_min_value);
        at_max_value = findViewById(R.id.at_max_value);
        at_min_value = findViewById(R.id.at_min_value);
        aw_max_value = findViewById(R.id.aw_max_value);
        aw_min_value = findViewById(R.id.aw_min_value);


        SharedPreferences pref = getSharedPreferences("example", MODE_PRIVATE);
        sw_max_value.setText(pref.getString("swmax", "100"));
        sw_min_value.setText(pref.getString("swmin", "0"));
        at_max_value.setText(pref.getString("atmax", "50"));
        at_min_value.setText(pref.getString("atmin", "10"));
        aw_max_value.setText(pref.getString("awmax", "100"));
        aw_min_value.setText(pref.getString("awmin", "0"));


        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Float.parseFloat(sw_min_value.getText().toString())<Float.parseFloat(sw_max_value.getText().toString())
                        &&Float.parseFloat(at_min_value.getText().toString())<Float.parseFloat(at_max_value.getText().toString())
                        &&Float.parseFloat(aw_min_value.getText().toString())<Float.parseFloat(aw_max_value.getText().toString())
                ){
                    SharedPreferences pref = getSharedPreferences("example", MODE_PRIVATE);
                    pref.edit()
                            .putString("swmax", sw_max_value.getText().toString())
                            .putString("swmin", sw_min_value.getText().toString())
                            .putString("atmax", at_max_value.getText().toString())
                            .putString("atmin", at_min_value.getText().toString())
                            .putString("awmax", aw_max_value.getText().toString())
                            .putString("awmin", aw_min_value.getText().toString())

                            .commit();
                    Toast.makeText(getApplicationContext(),"資料儲存成功",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"資料儲存失敗",Toast.LENGTH_SHORT).show();
                }



            }
        });

    }
}