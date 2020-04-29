package com.tignioj.timelineapp.tasks.taskslist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.MyTask;
import com.tignioj.timelineapp.tasks.taskslist.popup.TaskRemindDatePopupWindow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义Dialog
 */
public class MyEditTaskDialog extends DialogFragment {
    private MyViewModel myViewModel;
    private TasksAdapter tasksAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private MyTask currentSwipeTask;
    private Chip chipRemindMeDate;
    private CheckBox checkBoxRepeat;

    public MyEditTaskDialog(MyViewModel viewModel, TasksAdapter tasksAdapter, RecyclerView.ViewHolder viewHolder, MyTask currentSwipeTask) {
        super();
        this.myViewModel = viewModel;
        this.tasksAdapter = tasksAdapter;
        this.viewHolder = viewHolder;
        this.currentSwipeTask = currentSwipeTask;
    }

    EditText editText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //读取xml
        final View inflate = inflater.inflate(R.layout.edit_tasks_dialog, container, false);
        editText = inflate.findViewById(R.id.et_task_dialog_edit_tasks);
        chipRemindMeDate = inflate.findViewById(R.id.chip_edit_task_remind_date);
        checkBoxRepeat = inflate.findViewById(R.id.checkBox_edit_task_repeat);

        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText.setText(currentSwipeTask.getContent());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        chipRemindMeDate.setText(sdf.format(currentSwipeTask.getRemindMeDate()));

        chipRemindMeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TaskRemindDatePopupWindow(MyEditTaskDialog.this, chipRemindMeDate);
            }
        });

        checkBoxRepeat.setChecked(currentSwipeTask.isRepeat());

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    view.findViewById(R.id.edit_tasks_dialog_ok_button).setEnabled(true);
                } else {
                    view.findViewById(R.id.edit_tasks_dialog_ok_button).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        view.findViewById(R.id.edit_tasks_dialog_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取EditText的内容
                currentSwipeTask.setContent(editText.getText().toString().trim());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date parse;
                try {
                    parse = sdf.parse(chipRemindMeDate.getText().toString());
                    currentSwipeTask.setRemindMeDate(parse);
                } catch (ParseException e) {
                }
                currentSwipeTask.setRepeat(checkBoxRepeat.isChecked());

                myViewModel.updateTasks(currentSwipeTask);
                requireDialog().dismiss();
            }
        });
        view.findViewById(R.id.edit_tasks_dialog_cacnle_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireDialog().dismiss();
            }
        });
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);


        editText.requestFocus();
        //请求键盘
        InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        tasksAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
        super.onDismiss(dialog);
    }


    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }
}