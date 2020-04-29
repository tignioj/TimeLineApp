package com.tignioj.timelineapp.floating_timeline;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.TimeLinePoJo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FloatingTimeLineAdapter extends ListAdapter<TimeLinePoJo, FloatingTimeLineAdapter.MyViewHolder> {
    protected FloatingTimeLineAdapter() {
        super(new DiffUtil.ItemCallback<TimeLinePoJo>() {
            @Override
            public boolean areItemsTheSame(@NonNull TimeLinePoJo oldItem, @NonNull TimeLinePoJo newItem) {
                Log.d("myTag", "areItemsTheSame");
                return oldItem.getTimeLine().getId() == newItem.getTimeLine().getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull TimeLinePoJo oldItem, @NonNull TimeLinePoJo newItem) {
                boolean b =
                        oldItem.isCurrent() == newItem.isCurrent()
                                && oldItem.getTasksCount() == newItem.getTasksCount()
                                && oldItem.getTimeLine().getStartTime().equals(newItem.getTimeLine().getStartTime())
                                && oldItem.getTimeLine().getEndTime().equals(newItem.getTimeLine().getEndTime())
                                && oldItem.getTimeLine().getSummary().equals(newItem.getTimeLine().getSummary());
                return b;
            }
        });
    }

    ViewGroup parent;
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        this.parent = parent;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        itemView = layoutInflater.inflate(R.layout.cell_floating_timelines, parent, false);
        final MyViewHolder holder = new MyViewHolder(itemView);
        //监听每一列被按下
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TimeLinePoJo tl = getItem(position);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String summary = tl.getTimeLine().getSummary();
        holder.textViewSummary.setText(summary);
        holder.textViewStartTime.setText(sdf.format(tl.getTimeLine().getStartTime()));
        holder.textViewEndTime.setText(sdf.format(tl.getTimeLine().getEndTime()));
//        holder.chip.setText(String.valueOf(tl.getTasksCount()));
        holder.textViewTaskCount.setText(String.valueOf(tl.getTasksCount()));

        //高亮当前时间段
        if (tl.isCurrent()) {
            holder.itemView.setBackgroundColor(Color.argb(100, 80, 80, 80));
            holder.textViewSummary.setTextSize(24);
            holder.textViewStartTime.setTextColor(Color.WHITE);
            holder.textViewEndTime.setTextColor(Color.WHITE);
            holder.textViewSummary.setTextColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.textViewSummary.setTextSize(12);
            int color = parent.getResources().getColor(R.color.floating_text_color);
            holder.textViewStartTime.setTextColor(color);
            holder.textViewTaskCount.setTextColor(color);
            holder.textViewEndTime.setTextColor(color);
            holder.textViewSummary.setTextColor(color);
        }

        if (tl.getTasksCount() == 0) {
//            holder.chip.setVisibility(View.INVISIBLE);
            holder.textViewTaskCount.setVisibility(View.INVISIBLE);
        } else {
//            holder.chip.setVisibility(View.VISIBLE);
//            holder.chip.setTextColor(Color.RED);
            holder.textViewTaskCount.setVisibility(View.VISIBLE);
            holder.textViewTaskCount.setTextColor(Color.RED);
        }
    }



    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStartTime, textViewEndTime, textViewSummary, textViewTaskCount;
//        Chip chip;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStartTime = itemView.findViewById(R.id.tv_start);
            textViewEndTime = itemView.findViewById(R.id.tv_end);
            textViewSummary = itemView.findViewById(R.id.tv_summary);
//            chip = itemView.findViewById(R.id.chip_taskcount);
            textViewTaskCount = itemView.findViewById(R.id.tv_task_count);
        }
    }

}
