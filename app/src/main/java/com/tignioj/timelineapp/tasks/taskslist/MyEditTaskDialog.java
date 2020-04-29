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
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.MyTask;

/**
 * 自定义Dialog
 */
public class MyEditTaskDialog extends DialogFragment {
    private MyViewModel myViewModel;
    private TasksAdapter tasksAdapter;
    private RecyclerView.ViewHolder viewHolder;
    private MyTask currentSwipeTask;

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
        editText.setText(currentSwipeTask.getContent());


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    inflate.findViewById(R.id.edit_tasks_dialog_ok_button).setEnabled(true);
                } else {
                    inflate.findViewById(R.id.edit_tasks_dialog_ok_button).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inflate.findViewById(R.id.edit_tasks_dialog_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取EditText的内容
                currentSwipeTask.setContent(editText.getText().toString().trim());
                myViewModel.updateTasks(currentSwipeTask);
                requireDialog().dismiss();
            }
        });
        inflate.findViewById(R.id.edit_tasks_dialog_cacnle_button).setOnClickListener(new View.OnClickListener() {
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

        return inflate;
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