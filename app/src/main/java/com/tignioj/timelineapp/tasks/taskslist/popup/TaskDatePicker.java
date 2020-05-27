package com.tignioj.timelineapp.tasks.taskslist.popup;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;

import java.util.Calendar;
import java.util.Objects;

public class TaskDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private TaskRemindDatePopupWindow taskRemindDatePopupWindow;

    public TaskDatePicker(TaskRemindDatePopupWindow taskRemindDatePopupWindow) {
        this.taskRemindDatePopupWindow = taskRemindDatePopupWindow;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Calendar instance = Calendar.getInstance();
            int y = instance.get(Calendar.YEAR);
            int m = instance.get(Calendar.MONTH);
            int d = instance.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(requireContext(), this, y, m, d);
        }
        return null;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd");
        Chip chip = taskRemindDatePopupWindow.getChip();
        chip.setText(year + "-" + (month+1) + "-" + dayOfMonth);
        chip.setCloseIconVisible(true);
        taskRemindDatePopupWindow.dismiss();
    }
}
