package com.example.plantmonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.IntStream;

public class PlantRecord extends AppCompatActivity {

    List<RecordModel> record = new ArrayList<>();
    private RecyclerView plantrecyclerView;

    final OkHttpClient client = new OkHttpClient();
    public ExecutorService service = Executors.newSingleThreadExecutor();
    //    Gson gson = new Gson();
    Gson gson = new GsonBuilder().setDateFormat("EEEE, dd MM yyyy HH:mm:ss").create();
    public MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private SectionedRecyclerViewAdapter sectionedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_record);

        plantrecyclerView = findViewById(R.id.plantrecyclerView);

        plantrecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        sectionedAdapter = new SectionedRecyclerViewAdapter();

        service.submit(new Runnable() {
            @Override
            public void run() {

                Request request = new Request.Builder().url("http://192.168.10.114/Login/getrecord").get().build();
                try {
                    final Response response = client.newCall(request).execute();
                    final String resStr = response.body().string();
//                    Log.e("resStr", resStr);
                    JSONObject jsonObject = new JSONObject(resStr);
                    String array = jsonObject.getString("jsonResult");
                    record.clear();
                    record.addAll(gson.fromJson(array, new TypeToken<List<RecordModel>>() {
                    }.getType()));


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd EEEE");
                            Map<Date, List<RecordModel>> map = record.stream().collect(Collectors.groupingBy(x -> {
                                try {
                                    return formatter.parse(formatter.format(x.date));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return new Date();
                                }
                            }));//以日期切割



                            map.keySet().forEach(date -> {
                                SimpleDateFormat formatter1 = new SimpleDateFormat("HH");
                                Map<Date, List<RecordModel>> everyhourmap = map.get(date).stream().collect(Collectors.groupingBy(x -> {
                                    try {
                                        return formatter1.parse(formatter1.format(x.date));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        return new Date();
                                    }
                                }));

                                List<RecordModel> avglist = new ArrayList<>();
                                everyhourmap.keySet().forEach(hour -> {

                                    List<RecordModel> onehour = everyhourmap.get(hour);

                                    double[] sum = onehour.stream().map(x -> x.valuearray()).reduce(new double[]{0, 0, 0}, (a, b) -> new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]});
                                    double[] avg = IntStream.range(0, sum.length).mapToDouble(index -> sum[index] / onehour.size()).toArray();
                                    RecordModel Avgmodel = new RecordModel(hour, avg);
                                    avglist.add(Avgmodel);
                                });
                                map.put(date, avglist);
                            });


                            map.keySet().stream().sorted((a,b)->a.compareTo(b)).forEach(x -> sectionedAdapter.addSection(new ExpandableContactsSection(formatter.format(x), map.get(x))));


                            plantrecyclerView.setAdapter(sectionedAdapter);


                        }
                    });


                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }

    class ExpandableContactsSection extends Section {

        private final String title;
        private final List<RecordModel> SectionList;


        private boolean expanded = false;

        ExpandableContactsSection(@NonNull String title, @NonNull List<RecordModel> list
        ) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.plant_record_cardview)
                    .headerResourceId(R.layout.plant_record_header)
                    .build());

            this.title = title;
            this.SectionList = list;

        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            final View rootView;
            final TextView item_time1;
            final TextView item_time2;
            final TextView item_sw;
            final TextView item_at;
            final TextView item_aw;


            ItemViewHolder(@NonNull View view) {
                super(view);

                rootView = view;
                item_time1 = view.findViewById(R.id.item_time1);
                item_time2 = view.findViewById(R.id.item_time2);
                item_sw = view.findViewById(R.id.item_sw);
                item_at = view.findViewById(R.id.item_at);
                item_aw = view.findViewById(R.id.item_aw);
            }
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {

            final View rootView;
            final TextView tvTitle;
            final ImageView imgArrow;

            HeaderViewHolder(@NonNull View view) {
                super(view);

                rootView = view;
                tvTitle = view.findViewById(R.id.recodeTitle);
                imgArrow = view.findViewById(R.id.imgArrow);
            }
        }


        @Override
        public int getContentItemsTotal() {
            return expanded ? SectionList.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(final View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;
            final RecordModel fireItem = SectionList.get(position);
            DecimalFormat frmt = new DecimalFormat();
            frmt.setMaximumFractionDigits(3);
            SimpleDateFormat formattertime = new SimpleDateFormat("HH");
            int t=Integer.valueOf(formattertime.format(fireItem.date))+1;
            itemHolder.item_time1.setText(formattertime.format(fireItem.date));
            itemHolder.item_time2.setText(t+"");
            itemHolder.item_sw.setText(frmt.format(fireItem.sw) + "");
            itemHolder.item_at.setText(frmt.format(fireItem.at) + "");
            itemHolder.item_aw.setText(frmt.format(fireItem.aw) + "");


            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });


            itemHolder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {


                    return true;
                }
            });
        }


        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
            final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;


            int index = sectionedAdapter.getSectionIndex(this) + 1;

            headerHolder.tvTitle.setText(title + " 有" + SectionList.size() + "筆");
            headerHolder.imgArrow.setImageResource(
                    expanded ? R.drawable.ic_keyboard_arrow_up_black_18dp : R.drawable.ic_keyboard_arrow_down_black_18dp
            );


////////////////////////////////////////////////////////////////////////////////////
            headerHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final SectionAdapter sectionAdapter = sectionedAdapter.getAdapterForSection(ExpandableContactsSection.this);


                    // store info of current section state before changing its state
                    final boolean wasExpanded = ExpandableContactsSection.this.isExpanded();
                    final int previousItemsTotal = ExpandableContactsSection.this.getContentItemsTotal();

                    ExpandableContactsSection.this.setExpanded(!wasExpanded);
                    sectionAdapter.notifyHeaderChanged();

                    if (wasExpanded) {
                        sectionAdapter.notifyItemRangeRemoved(0, previousItemsTotal);
                    } else {

                        sectionAdapter.notifyAllItemsInserted();
                    }


                }
            });


        }

        boolean isExpanded() {
            return expanded;
        }

        void setExpanded(final boolean expanded) {
            this.expanded = expanded;
        }

////////////////////////////////////////////////////////////////////////////////////
    }


    public class RecordModel {

        public Date date;

        public double sw;
        public double at;
        public double aw;

        public RecordModel(Date HourDate, double[] avgarray) {
            date = HourDate;
            sw = avgarray[0];
            at = avgarray[1];
            aw = avgarray[2];

        }


        public double[] valuearray() {

            return new double[]{sw, at, aw};
        }
    }


}