package com.example.icross;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RankActivity extends AppCompatActivity {
    private static final String TAG = "RankActivity";
    private List<RankListItem> rankListItems;
    private View selectedView;
    private ProgressDialog progressDialog;
    Typeface typeface;
    RecyclerView recyclerView;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // 添加测试数据
        rankListItems = new ArrayList<>();

        RankListItem item = new RankListItem();
//        item.setName("玩家");
//        item.setLocation("洛阳");
//        item.setScore(2000);
//        item.setNum(1);
//        rankListItems.add(item);

        typeface = Typeface.createFromAsset(getAssets(), "font.ttf");
        TextView nameHeader = (TextView) findViewById(R.id.rank_name);
        TextView locationHeader = (TextView) findViewById(R.id.rank_location);
        TextView timeHeader = (TextView) findViewById(R.id.rank_time);
        TextView scoreHeader = (TextView) findViewById(R.id.rank_score);
        TextView numHeader = (TextView) findViewById(R.id.rank_num);
        TextView header = (TextView) findViewById(R.id.rank_header);
        nameHeader.setTypeface(typeface);
        locationHeader.setTypeface(typeface);
        timeHeader.setTypeface(typeface);
        scoreHeader.setTypeface(typeface);
        numHeader.setTypeface(typeface);
        header.setTypeface(typeface);

        progressDialog = new ProgressDialog(RankActivity.this);
        progressDialog.setTitle("请稍候");
        progressDialog.setMessage("数据加载中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        getData();
    }

    class RankAdapter extends RecyclerView.Adapter<RankActivity.RankAdapter.ViewHolder> {
        private List<RankListItem> rankListItems;
        private Typeface typeface = Typeface.createFromAsset(getAssets(), "font.ttf");

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_list_item, parent, false);
            RankActivity.RankAdapter.ViewHolder holder = new RankActivity.RankAdapter.ViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RankListItem rankListItem = rankListItems.get(position);
            holder.nameView.setText(rankListItem.getName());
            holder.locationView.setText(rankListItem.getLocation());
            holder.timeView.setText(rankListItem.getTime());
            holder.scoreView.setText(String.valueOf(rankListItem.getScore()));
            holder.numView.setText(String.valueOf(rankListItem.getNum()));
        }

        @Override
        public int getItemCount() {
            return rankListItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;
            TextView locationView;
            TextView timeView;
            TextView scoreView;
            TextView numView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.rank_item_name);
                locationView = itemView.findViewById(R.id.rank_item_location);
                timeView = itemView.findViewById(R.id.rank_item_time);
                scoreView = itemView.findViewById(R.id.rank_item_score);
                numView = itemView.findViewById(R.id.rank_item_num);
            }
        }

        public RankAdapter(List<RankListItem> items) {
            rankListItems = items;
        }
    }

    private void getData() {
//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url = Common.baseUrl + "/getRankList";
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        // 异步请求
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.cancel();
                        Toast.makeText(RankActivity.this, "数据加载失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resp = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject res = new JSONObject(resp);
                            int count = res.getInt("count");
                            JSONArray arr = new JSONArray(res.getJSONArray("data").toString());
                            for (int i = 0; i < count; ++i) {
                                JSONObject jsonObject = arr.getJSONObject(i);
                                RankListItem item = new RankListItem();
                                item.setNum(i + 1);
                                item.setName(jsonObject.getString("name"));
                                item.setLocation(jsonObject.getString("location"));
                                item.setScore(jsonObject.getInt("score"));
                                item.setTime(jsonObject.getString("time"));
                                rankListItems.add(item);
                                // Log.i(TAG, "run: " + item.toString());

                                recyclerView = (RecyclerView) findViewById(R.id.rank_list);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RankActivity.this);
                                recyclerView.setLayoutManager(linearLayoutManager);
                                RankActivity.RankAdapter adapter = new RankAdapter(rankListItems);
                                recyclerView.setAdapter(adapter);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        progressDialog.cancel();
                    }
                });
            }
        });
    }
}