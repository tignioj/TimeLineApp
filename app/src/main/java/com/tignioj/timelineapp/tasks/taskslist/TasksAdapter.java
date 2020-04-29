package com.tignioj.timelineapp.tasks.taskslist;

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


public class TasksAdapter extends ListAdapter<MyTask, TasksAdapter.MyViewHolder> {
    private MyViewModel myViewModel;

    protected TasksAdapter(MyViewModel myViewModel) {
        super(new DiffUtil.ItemCallback<MyTask>() {
            @Override
            public boolean areItemsTheSame(@NonNull MyTask oldItem, @NonNull MyTask newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull MyTask oldItem, @NonNull MyTask newItem) {
                return oldItem.getContent().equals(newItem.getContent())
                        && oldItem.getTimeline() == newItem.getTimeline()
                        && oldItem.getRemindMeDate().equals(newItem.getRemindMeDate())
                        ;
            }
        });
        this.myViewModel = myViewModel;
    }


    //每一项
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        itemView = layoutInflater.inflate(R.layout.cell_tasks, parent, false);
        final MyViewHolder holder = new MyViewHolder(itemView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("myTag", v.toString());
            }
        });
        holder.checkBoxHasFinished.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyTask item = getItem(holder.getAdapterPosition());
                item.setHasFinish(holder.checkBoxHasFinished.isChecked());

                myViewModel.updateTasks(item);
            }
        });
        return holder;
    }

    //更改视图数据
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.checkBoxHasFinished.setChecked(getItem(position).isHasFinish());
        holder.textViewTaskContent.setText(getItem(position).getContent());
//        holder.textViewSequenceNumber.setText(String.valueOf(position + 1));
        //解决序号显示错误问题(每次添加都是1)
        holder.textViewSequenceNumber.setText(String.valueOf(holder.getAdapterPosition() + 1));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSequenceNumber, textViewTaskContent;
        private CheckBox checkBoxHasFinished;

        public TextView getTextViewSequenceNumber() {
            return textViewSequenceNumber;
        }

        public TextView getTextViewTaskContent() {
            return textViewTaskContent;
        }

        public CheckBox getCheckBoxHasFinished() {
            return checkBoxHasFinished;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSequenceNumber = itemView.findViewById(R.id.tv_sequence_number);
            textViewTaskContent = itemView.findViewById(R.id.tv_content);
            checkBoxHasFinished = itemView.findViewById(R.id.checkbox_hasfinished);
        }
    }
}
