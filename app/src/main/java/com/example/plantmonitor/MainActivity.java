package com.example.plantmonitor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView homerecyclerView;
    private MyAdapter myAdapter;
    static String[] feature = {"個人資料", "數值調整", "掃描藍芽", "感測器", "植物紀錄"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        homerecyclerView = findViewById(R.id.Homerecyclerview);


        homerecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        myAdapter = new MyAdapter(Arrays.asList(feature));
        homerecyclerView.setAdapter(myAdapter);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("登出").
                setMessage("確定登出?").
                setNegativeButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).

                setPositiveButton("取消", (dialog, which) -> {

                })

                .show();

    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private List<String> mlist;

        MyAdapter(List<String> data) {
            mlist = data;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView txtItem;

            ViewHolder(View itemView) {
                super(itemView);
                txtItem = (TextView) itemView.findViewById(R.id.homecardViewrd);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.homecardview, parent, false);

            ViewHolder vh = new ViewHolder(view);

            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = vh.getAdapterPosition();

                    switch (feature[position]) {
                        case ("個人資料"): {
                            startActivity(new Intent(MainActivity.this, Person.class));
                            break;
                        }
                        case ("數值調整"): {
                            startActivity(new Intent(MainActivity.this, ValueChange.class));
                            break;
                        }
                        case ("感測器"): {
                            startActivity(new Intent(MainActivity.this, Sensor.class));
                            break;
                        }
                        case ("植物紀錄"): {
                            startActivity(new Intent(MainActivity.this, PlantRecord.class));
                            break;
                        }

                        case ("掃描藍芽"): {
                            startActivity(new Intent(MainActivity.this, ScanBLE.class));
                            break;
                        }
                    }
                }
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.txtItem.setText(mlist.get(position));
        }

        @Override
        public int getItemCount() {
            return mlist.size();
        }
    }

}