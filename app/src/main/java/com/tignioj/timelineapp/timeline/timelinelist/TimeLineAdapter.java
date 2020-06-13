package com.tignioj.timelineapp.timeline.timelinelist;

import android.app.Application;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;

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
                return oldItem.getTimeLine().getId() == newItem.getTimeLine().getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull TimeLineWithTaskCountsPoJo oldItem, @NonNull TimeLineWithTaskCountsPoJo newItem) {
                return
                        oldItem.isCurrent() == newItem.isCurrent()
                                && oldItem.getTasksCount() == newItem.getTasksCount()
                                && oldItem.getTimeLine().getStartTime().equals(newItem.getTimeLine().getStartTime())
                                && oldItem.getTimeLine().getEndTime().equals(newItem.getTimeLine().getEndTime())
                                && oldItem.getTimeLine().getSummary().equals(newItem.getTimeLine().getSummary())
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
                bundle.putLong("timeLineId", item.getTimeLine().getId());
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
        String summary = tl.getTimeLine().getSummary();
        holder.textViewSummary.setText(summary);
        holder.textViewStartTime.setText(sdf.format(tl.getTimeLine().getStartTime()));
        holder.textViewEndTime.setText(sdf.format(tl.getTimeLine().getEndTime()));
//        holder.chip.setText(String.valueOf(tl.tasksCount));
        holder.textViewTasksCount.setText(String.valueOf(tl.getTasksCount()));


        //高亮当前时间段
//        if (betweenStartTimeAndEndTime(tl.timeLine.getStartTime(), tl.timeLine.getEndTime())) {
        if (tl.isCurrent()) {
//            holder.itemView.setBackgroundColor(Color.rgb(0xC5, 0x89, 0x33));
//            C58933
            holder.itemView.setBackgroundColor(Color.rgb(0x79, 0x86, 0xCB));

        } else {
            //获取背景
            //https://stackoverflow.com/questions/37987732/programmatically-set-selectableitembackground-on-android-view
            TypedValue outValue = new TypedValue();
            holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            holder.itemView.setBackgroundResource(outValue.resourceId);
        }

        //高亮未完成任务
        if (tl.getTasksCount() > 0) {
//            holder.chip.setTextAppearance(R.style.fontForNotificationLandingPage);
            holder.textViewTasksCount.setTextColor(Color.RED);
        } else {
            TypedValue outValue = new TypedValue();
            holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColor, outValue, true);

            TypedValue typedValue = new TypedValue();
            Application application = myViewModel.getApplication();
            Resources.Theme theme = application.getTheme();
            theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
            TypedArray arr =
                    application.obtainStyledAttributes(typedValue.data, new int[]{
                            android.R.attr.textColorPrimary});
            int primaryColor = arr.getColor(0, -1);
            holder.textViewTasksCount.setTextColor(primaryColor);
            //回收资源
            arr.recycle();

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
        TextView textViewStartTime, textViewEndTime, textViewSummary, textViewTasksCount;
//        Chip chip;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStartTime = itemView.findViewById(R.id.tv_start);
            textViewEndTime = itemView.findViewById(R.id.tv_end);
            textViewSummary = itemView.findViewById(R.id.tv_summary);
//            chip = itemView.findViewById(R.id.chip_taskcount);
            textViewTasksCount = itemView.findViewById(R.id.tv_time_line_tasks_count);
        }
    }
}
