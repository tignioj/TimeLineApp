package com.tignioj.timelineapp.timeline.timelinelist;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeLineAdapter extends ListAdapter<TimeLineWithTaskCountsPoJo, TimeLineAdapter.MyViewHolder> {
    private static final String LOG_TAG = "myTag";
    private MyViewModel myViewModel;


    public TimeLineAdapter(MyViewModel myViewModel) {
        super(new DiffUtil.ItemCallback<TimeLineWithTaskCountsPoJo>() {
            @Override

            public boolean areItemsTheSame(@NonNull TimeLineWithTaskCountsPoJo oldItem, @NonNull TimeLineWithTaskCountsPoJo newItem) {
                return oldItem.timeLine.getId() == newItem.timeLine.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull TimeLineWithTaskCountsPoJo oldItem, @NonNull TimeLineWithTaskCountsPoJo newItem) {
                return
                        oldItem.isCurrent() == newItem.isCurrent()
                                && oldItem.tasksCount == newItem.tasksCount
                                && oldItem.timeLine.getStartTime().equals(newItem.timeLine.getStartTime())
                                && oldItem.timeLine.getEndTime().equals(newItem.timeLine.getEndTime())
                                && oldItem.timeLine.getSummary().equals(newItem.timeLine.getSummary())
                        ;
            }
        });
        this.myViewModel = myViewModel;
    }

    //返回每一项
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        itemView = layoutInflater.inflate(R.layout.cell_timeline, parent, false);
        final MyViewHolder holder = new MyViewHolder(itemView);
        //监听每一列被按下
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                TimeLineWithTaskCountsPoJo item = getItem(holder.getAdapterPosition());
                bundle.putLong("timeLineId", item.timeLine.getId());
                Navigation.findNavController(v).navigate(R.id.action_timeLineFragment_to_tasksFragment, bundle);
            }
        });
        return holder;
    }

    //更改视图数据
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final TimeLineWithTaskCountsPoJo tl = getItem(position);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
        String summary = tl.timeLine.getSummary();
        holder.textViewSummary.setText(summary);
        holder.textViewStartTime.setText(sdf.format(tl.timeLine.getStartTime()));
        holder.textViewEndTime.setText(sdf.format(tl.timeLine.getEndTime()));
        holder.chip.setText(String.valueOf(tl.tasksCount));


        //高亮当前时间段
//        if (betweenStartTimeAndEndTime(tl.timeLine.getStartTime(), tl.timeLine.getEndTime())) {
        if (tl.isCurrent()) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
//            ColorDrawable background = (ColorDrawable) holder.itemView.getRootView().getBackground();
//            background.getBounds();

//            final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
//                    holder.itemView.getRootView(),
//                    "x",
//                    0, 0
//            );
//            objectAnimator.setDuration(10000);
//
//            objectAnimator.setFloatValues(
//                    /*关键帧1*/
//                    holder.itemView.getX()-400,
//
//                    /*关键帧2*/
//                    holder.itemView.getX() + 400
//                    /*可以继续写关键帧*/
//            );
//            /*开启动画*/
//            objectAnimator.start();
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        //高亮未完成任务
        if (tl.tasksCount > 0) {
            holder.chip.setTextAppearance(R.style.fontForNotificationLandingPage);
        }
    }

    /**
     * 判断现在时间是否在传入的两个时间段内，排除日期(年月日)，只判断时间(时分秒)
     *
     * @param startTime
     * @param endTime
     * @return
     */
    private boolean betweenStartTimeAndEndTime(Date startTime, Date endTime) {
        Calendar cNow = Calendar.getInstance();
        Calendar cStart = Calendar.getInstance();
        Calendar cEnd = Calendar.getInstance();
        Date now = new Date();

        cNow.setTime(now);
        cStart.setTime(startTime);
        cEnd.setTime(endTime);

        //设置成相同第日期，这时候只有时间不同了
        cNow.set(1970, 1, 1);
        cStart.set(1970, 1, 1);
        cEnd.set(1970, 1, 1);

        //开始时间比现在早， 现在比结束时间早
        return cStart.before(cNow) && cNow.before(cEnd);
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStartTime, textViewEndTime, textViewSummary;
        Chip chip;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStartTime = itemView.findViewById(R.id.tv_start);
            textViewEndTime = itemView.findViewById(R.id.tv_end);
            textViewSummary = itemView.findViewById(R.id.tv_summary);
            chip = itemView.findViewById(R.id.chip_taskcount);
        }
    }
}
