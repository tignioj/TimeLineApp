package com.tignioj.timelineapp.tasks.taskslist.popup;

import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.tignioj.timelineapp.MainActivity;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.tasks.taskslist.TasksFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TaskPopupWindow extends PopupWindow {
    TasksFragment context;
    Chip chip;

    public Chip getChip() {
        return chip;
    }


    public TaskPopupWindow(final TasksFragment context, View inflate, final Chip chip) {
        super(inflate, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        this.context = context;
        this.chip = chip;
        textViewToday = getContentView().findViewById(R.id.tv_today);
        textViewTomorrow = getContentView().findViewById(R.id.tv_tomorrow);
        textViewPickDate = getContentView().findViewById(R.id.tv_pick_date);

        //在参数指定视图的左下角显示

        int location[] = new int[2];
        chip.getLocationOnScreen(location);
        //在参数指定视图的左下角显示
//        showAsDropDown(chip, 0, -offsetY);
        showAtLocation(chip.getRootView(), Gravity.NO_GRAVITY, location[0], location[1] + chip.getHeight());

        textViewToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String format = simpleDateFormat.format(new Date());
                Toast.makeText(context.requireActivity(), textViewToday.getText().toString(), Toast.LENGTH_SHORT).show();
                chip.setText(format);
                chip.setCloseIconVisible(true);
                dismiss();
            }
        });

        textViewTomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Toast.makeText(context.requireActivity(), textViewTomorrow.getText().toString(), Toast.LENGTH_SHORT).show();
                Calendar calendar = simpleDateFormat.getCalendar();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, 1);
                String format1 = simpleDateFormat.format(calendar.getTime());
                chip.setText(format1);
                chip.setCloseIconVisible(true);
                dismiss();
            }
        });

        textViewPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskDatePicker taskDatePicker = new TaskDatePicker(TaskPopupWindow.this);
                taskDatePicker.show(context.requireActivity().getSupportFragmentManager().beginTransaction(), "pick date");
            }
        });


        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chip.setCloseIconVisible(false);
                chip.setText("Show popup");
            }
        });
    }
    TextView textViewToday, textViewTomorrow, textViewPickDate;
    CheckBox checkBoxRepeat;
}
