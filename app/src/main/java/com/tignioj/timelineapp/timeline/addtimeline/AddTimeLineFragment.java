package com.tignioj.timelineapp.timeline.addtimeline;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.TimeLine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddTimeLineFragment extends Fragment {

    private static final String START_TIME_PICKER_TAG = "start_time_picker_tag";
    private static final String END_TIME_PICKER_TAG = "end_time_picker_tag";
    private EditText editTextEndTime;
    private EditText editTextStartTime;
    private EditText editTextSummary;
    private MyViewModel myViewModel;
    //是否为编辑页面
    private boolean isEdit;
    private TimeLine timeLine;
    CheckBox checkBoxEnableVibrate;

    public AddTimeLineFragment() {
        // Required empty public constructor
    }

    public AddTimeLineFragment getFragmentInstance() {
        return this;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_add_time_line, container, false);

        editTextEndTime = inflate.findViewById(R.id.et_end_time);
        editTextStartTime = inflate.findViewById(R.id.et_start_time);
        editTextSummary = inflate.findViewById(R.id.et_summary);

        checkBoxEnableVibrate = inflate.findViewById(R.id.addTimeline_checkBoxEnableVibrate);
        return inflate;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        timeLine = new TimeLine();



        super.onActivityCreated(savedInstanceState);
        ((Button) requireActivity().findViewById(R.id.button_save)).setEnabled(false);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        //检测是否有TimeLine对象传过来, 如果有说明是编辑页面
        Bundle arguments = getArguments();
        if (arguments != null) {
            TimeLine timeLine = arguments.getParcelable(getString(R.string.timeline_to_edit));
            if (timeLine != null) {
                this.timeLine = timeLine;
                isEdit = true;
                editTextStartTime.setText(sdf.format(timeLine.getStartTime()));
                editTextEndTime.setText(sdf.format(timeLine.getEndTime()));
                editTextSummary.setText(timeLine.getSummary());
                ((Button) requireActivity().findViewById(R.id.button_save)).setEnabled(true);
                checkBoxEnableVibrate.setChecked(this.timeLine.isEnableVibrate());
            }
        }

        ((Button) requireActivity().findViewById(R.id.button_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigateUp();
            }
        });
        ((Button) requireActivity().findViewById(R.id.button_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String summary = ((EditText) requireActivity().findViewById(R.id.et_summary)).getText().toString().trim();
                timeLine.setSummary(summary);
                timeLine.setEnableVibrate(checkBoxEnableVibrate.isChecked());

                if (isEdit) {

                    myViewModel.updateTimeLines(timeLine);
                    Navigation.findNavController(v).navigateUp();
                    Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    myViewModel.insertTimeLines(timeLine);
                    Navigation.findNavController(v).navigateUp();
                    Toast.makeText(requireContext(), "添加成功", Toast.LENGTH_SHORT).show();
                }
            }
        });


        editTextStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment(getFragmentInstance());
                newFragment.show(requireActivity().getSupportFragmentManager(), START_TIME_PICKER_TAG);
            }
        });
        editTextEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment(getFragmentInstance());
                newFragment.show(requireActivity().getSupportFragmentManager(), END_TIME_PICKER_TAG);
            }
        });

        editTextStartTime.addTextChangedListener(textWatcher);
        editTextEndTime.addTextChangedListener(textWatcher);
        editTextSummary.addTextChangedListener(textWatcher);
    }

    //监听所有的输入框，当全部都有内容时，按钮设置为可用
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(editTextEndTime.getText().toString().trim())
                    && !TextUtils.isEmpty(editTextStartTime.getText().toString().trim())
                    && !TextUtils.isEmpty(editTextSummary.getText().toString().trim())
            ) {
                ((Button) requireActivity().findViewById(R.id.button_save)).setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        AddTimeLineFragment fragment;
        Calendar c;
        Date time;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        public TimePickerFragment(AddTimeLineFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (this.getTag().equals(START_TIME_PICKER_TAG)) {
                time = this.fragment.timeLine.getStartTime();
            } else {
                time = this.fragment.timeLine.getEndTime();
            }

            sdf.format(time == null ? new Date() : time);
            c = sdf.getCalendar();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            switch (this.getTag()) {
                case START_TIME_PICKER_TAG:
                    fragment.editTextStartTime.setText(sdf.format(c.getTime()));
                    fragment.timeLine.setStartTime(c.getTime());
                    break;
                case END_TIME_PICKER_TAG:
                    fragment.editTextEndTime.setText(sdf.format(c.getTime()));
                    fragment.timeLine.setEndTime(c.getTime());
                    break;
            }
        }
    }
}
