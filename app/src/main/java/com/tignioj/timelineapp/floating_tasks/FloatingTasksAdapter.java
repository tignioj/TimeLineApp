package com.tignioj.timelineapp.floating_tasks;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.MyTask;
import com.tignioj.timelineapp.entity.MyTaskPoJo;

public class FloatingTasksAdapter extends ListAdapter<MyTaskPoJo, FloatingTasksAdapter.MyViewHolder> {
    private MyViewModel myViewModel;
    private FloatingTasksFragment floatingTasksFragment;

    protected FloatingTasksAdapter(MyViewModel myViewModel, FloatingTasksFragment floatingTasksFragment) {
        super(new DiffUtil.ItemCallback<MyTaskPoJo>() {
            @Override
            public boolean areItemsTheSame(@NonNull MyTaskPoJo oldItem, @NonNull MyTaskPoJo newItem) {
                return oldItem.getMyTask().getId() == newItem.getMyTask().getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull MyTaskPoJo oldItem, @NonNull MyTaskPoJo newItem) {
                return oldItem.getMyTask().getContent().equals(newItem.getMyTask().getContent())
                        && oldItem.getMyTask().getRemindMeDate().equals(newItem.getMyTask().getRemindMeDate())
                        && oldItem.getMyTask().isHasFinish() == newItem.getMyTask().isHasFinish()
                        ;
            }

        });
        this.myViewModel = myViewModel;
        this.floatingTasksFragment = floatingTasksFragment;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        itemView = layoutInflater.inflate(R.layout.cell_floating_tasks, parent, false);
        final MyViewHolder holder = new MyViewHolder(itemView);


        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.getCheckBox().setChecked(getItem(holder.getAdapterPosition()).getMyTask().isHasFinish());
        holder.getTextViewContent().setText(getItem(position).getMyTask().getContent());


        holder.getCheckBox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyTaskPoJo item = getItem(holder.getAdapterPosition());
                MyTask myTask = item.getMyTask();
                myTask.setHasFinish(isChecked);
                myViewModel.updateTasks(myTask);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("myTag", "clickItem"+ v.toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //打开程序
                    ((ActivityManager) v.getContext().getSystemService(Context.ACTIVITY_SERVICE)).getAppTasks().get(0).moveToFront();
                }

//                Intent intent = new Intent(v.getContext(), MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                v.getContext().startActivity(intent);
//                ((MainActivity) v.getContext()).startActivityIfNeeded(intent, 0);

//                NavHostFragment.findNavController(floatingWindowFragment).navigate(R.id.timeLineFragment);
//                Navigation.findNavController(v).navigate(R.id.timeLineFragment);

            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewContent;
        private CheckBox checkBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_cell_floating);
            textViewContent = itemView.findViewById(R.id.tv_cell_floating);
        }

        public TextView getTextViewContent() {
            return textViewContent;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }
}
