package com.example.icross;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import org.litepal.LitePal;

public class HistoryActivity extends AppCompatActivity {
//    LinearLayout linearLayout;
    private View selectedView;
    private List<HistoryItem> historyItems;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    Typeface typeface;

    private static final String TAG = "HistoryActivity";

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Objects.requireNonNull(getSupportActionBar()).hide();

        historyItems = LitePal.order("time desc").limit(100).find(HistoryItem.class);
        recyclerView = (RecyclerView) findViewById(R.id.history_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new HistoryAdapter(historyItems);
        recyclerView.setAdapter(adapter);

        typeface = Typeface.createFromAsset(getAssets(), "font.ttf");
        TextView headerTime = (TextView) findViewById(R.id.his_header_time);
        TextView headerScore = (TextView) findViewById(R.id.his_header_score);
        TextView header = (TextView) findViewById(R.id.history_header);
        TextView historyName = (TextView) findViewById(R.id.history_name);
        headerTime.setTypeface(typeface);
        headerScore.setTypeface(typeface);
        header.setTypeface(typeface);
        historyName.setText(Common.userName);
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<HistoryItem> historyItems;
        private final Typeface typeface = Typeface.createFromAsset(getAssets(), "font.ttf");

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView timeView;
            TextView scoreView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                timeView = itemView.findViewById(R.id.his_item_time);
                scoreView = itemView.findViewById(R.id.his_item_score);
            }
        }

        public HistoryAdapter(List<HistoryItem> items) {
            historyItems = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                    builder.setTitle("删除");
                    builder.setMessage("确认删除该项记录吗？");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int position = holder.getAdapterPosition();
                            HistoryItem historyItem = historyItems.remove(position);
                            historyItem.delete();                   // 数据库删除
                            adapter.historyItems.remove(historyItem);
                            adapter.notifyItemRemoved(position);    // 更新
//                            recyclerView.invalidate();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                    return false;
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            HistoryItem historyItem = historyItems.get(position);
            holder.timeView.setText(historyItem.getTime());
            holder.scoreView.setText(String.valueOf(historyItem.getScore()));

            Typeface typeface = Typeface.createFromAsset(getAssets(), "font.ttf");
//            holder.timeView.setTypeface(typeface);
            holder.scoreView.setTypeface(typeface);
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}


