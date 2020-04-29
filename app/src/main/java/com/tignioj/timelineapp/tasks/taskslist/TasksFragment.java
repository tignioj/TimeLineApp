package com.tignioj.timelineapp.tasks.taskslist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.MyTask;
import com.google.android.material.snackbar.Snackbar;
import com.tignioj.timelineapp.tasks.taskslist.popup.TaskRemindDatePopupWindow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TasksFragment extends Fragment {
    private static final String SHP_SHOW_OLD = "is_show_old_tasks";
    private static final String SHP_SHOW_FINISHED = "is_show_finished_tasks";
    private static final String DB_TASKS_SHOWING_SETTING = "db_tasks_show_setting";
    private MyViewModel myViewModel;
    private long timeLineId;
    LiveData<List<MyTask>> myTaskLiveData;

    //列表视图
    private RecyclerView recyclerView;

    //任务输入框
    EditText et;

    //是否重复
    CheckBox checkBoxRepeat;

    //提醒日期
    Chip chipRemindMeDate;

    ///数据源，不负责显示，只负责存取，在撤销的时候可以获取这个可以避免拿到空数据/
    private List<MyTask> allTasks;
    //是否撤销
    private boolean undoAction = false;

    TasksAdapter tasksAdapter;

    private boolean showOld;
    private boolean showCompleted;

    public TasksFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    public TasksFragment getTasksFragmentInstance() {
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_tasks, container, false);
        recyclerView = inflate.findViewById(R.id.rcv_tasks);
        checkBoxRepeat = inflate.findViewById(R.id.checkBoxRepeat);
        chipRemindMeDate = inflate.findViewById(R.id.chipTaskPickDate);
        return inflate;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_checkbox_show_old_tasks:
                //加载用户偏好
                CheckBox checkBox = (CheckBox) item.getActionView();
                if (item.isChecked()) {
                    checkBox.setChecked(false);
                    item.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                    item.setChecked(true);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_menu, menu);
        //加载用户偏好
        final SharedPreferences shp = requireActivity().getSharedPreferences(DB_TASKS_SHOWING_SETTING, Context.MODE_PRIVATE);

        boolean isShowOld = shp.getBoolean(SHP_SHOW_OLD, false);
        boolean isShowFinished = shp.getBoolean(SHP_SHOW_FINISHED, false);

        Switch aSwitch = (Switch) menu.findItem(R.id.menu_switch).getActionView().findViewById(R.id.switchButton);
        aSwitch.setChecked(isShowFinished);

        //在Menu里面,item和checkBox是两个东西，checkBox的值和item的值并不同步，因此要设置两个值
        MenuItem checkItem = menu.findItem(R.id.menu_checkbox_show_old_tasks);
        CheckBox checkBox = (CheckBox) menu.findItem(R.id.menu_checkbox_show_old_tasks).getActionView();
        checkBox.setChecked(isShowOld);
        checkItem.setChecked(isShowOld);

        //监听checkbox
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor edit = shp.edit();
                myTaskLiveData.removeObservers(requireActivity());
                //是否显示旧数据
                if (!isChecked) {
                    showOld = false;
                } else {
                    showOld = true;
                }
                edit.putBoolean(SHP_SHOW_OLD, showOld);
                myTaskLiveData = myViewModel.getTodayMyTaskLiveDataByTimeLineId(timeLineId, showOld, showCompleted);
                myTaskLiveData.observe(requireActivity(), new Observer<List<MyTask>>() {
                    @Override
                    public void onChanged(List<MyTask> myTasks) {
                        allTasks = myTasks;
                        tasksAdapter.submitList(myTasks);
                    }
                });
                edit.apply();
            }
        });

        //监听Switch
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor edit = shp.edit();
                myTaskLiveData.removeObservers(requireActivity());
                if (isChecked) {
                    showCompleted = true;
                } else {
                    showCompleted = false;
                }
                edit.putBoolean(SHP_SHOW_FINISHED, showCompleted);
                myTaskLiveData = myViewModel.getTodayMyTaskLiveDataByTimeLineId(timeLineId, showOld, showCompleted);
                myTaskLiveData.observe(requireActivity(), new Observer<List<MyTask>>() {
                    @Override
                    public void onChanged(List<MyTask> myTasks) {
                        allTasks = myTasks;
                        tasksAdapter.submitList(myTasks);
                    }
                });
                edit.apply();
            }
        });

        //监听菜单栏上的checkbox-是否显示旧数据


        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * 进入页面时，编辑框获取检点以及请求键盘
     */
    @Override
    public void onStart() {
        super.onStart();
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();

        this.timeLineId = arguments.getLong("timeLineId");
        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);

        SharedPreferences shp = requireActivity().getSharedPreferences(DB_TASKS_SHOWING_SETTING, Context.MODE_PRIVATE);
        boolean isShowOld = shp.getBoolean(SHP_SHOW_OLD, false);
        boolean isShowFinished = shp.getBoolean(SHP_SHOW_FINISHED, false);

        myTaskLiveData = myViewModel.getTodayMyTaskLiveDataByTimeLineId(timeLineId, isShowOld, isShowFinished);

        et = requireView().findViewById(R.id.et_task);
        chipRemindMeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TaskRemindDatePopupWindow(TasksFragment.this, chipRemindMeDate);
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        tasksAdapter = new TasksAdapter(myViewModel);
        recyclerView.setAdapter(tasksAdapter);
        myTaskLiveData.observe(requireActivity(), new Observer<List<MyTask>>() {

            @Override
            public void onChanged(List<MyTask> myTasks) {
                allTasks = myTasks;
                int currentItemCount = tasksAdapter.getItemCount();
                //用户撤销时不滚动视图
                if (currentItemCount < myTasks.size() && !undoAction) {
                    //向上滚动一点给用户反馈表明数据已插入
                    recyclerView.smoothScrollBy(0, -200);
                }
                tasksAdapter.submitList(myTasks);
                undoAction = false;
            }

        });

        //修复插入时下标不更新的问题
        //当动画完成时，设置回调函数
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public void onAnimationFinished(@NonNull RecyclerView.ViewHolder viewHolder) {
                super.onAnimationFinished(viewHolder);
                //刷新序列号
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                //遍历列表
                if (linearLayoutManager == null) return;

                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                int lastCompletelyVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                for (int i = firstVisibleItemPosition; i <= lastCompletelyVisibleItemPosition; i++) {
                    TasksAdapter.MyViewHolder holder = (TasksAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                    if (holder == null) {
                        continue;
                    }
                    holder.getTextViewSequenceNumber().setText(String.valueOf(i + 1));
                }
            }
        });


        requireView().findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = et.getText().toString().trim();
                if (s.length() == 0) {
                    Toast.makeText(v.getContext(), "你还没有输入任务", Toast.LENGTH_SHORT).show();
                    //请求显示键盘
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
                    return;
                } else {
                    //TODO 增加MyTask的字段：提醒日期，是否重复

                    Date remindMeDate;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        remindMeDate = sdf.parse((String) chipRemindMeDate.getText());
                    } catch (ParseException e) {
                        remindMeDate = new Date();
                    }
                    remindMeDate = (remindMeDate == null ? new Date() : remindMeDate);

                    MyTask myTask = new MyTask(timeLineId, s, false, remindMeDate, new Date(), checkBoxRepeat.isChecked());
                    et.setText("");
                    myViewModel.insertTasks(myTask);
                    //请求隐藏键盘
//                    InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });


        /**
         * 0: 不支持拖动
         *ItemTouchHelper.START | ItemTouchHelper.END  支持向左划以及向右滑
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //TODO 数据的拖动，有bug，先不做了
//                Log.d("myTag", viewHolder.getAdapterPosition() + ":" + target.getAdapterPosition());
//                Word wordFrom = allWords.get(viewHolder.getAdapterPosition());
//                Word wordTo = allWords.get(target.getAdapterPosition());
//                //数据库层面的交换id
//                int idTemp = wordFrom.getId();
//                wordFrom.setId(wordTo.getId());
//                wordTo.setId(idTemp);
//
//                //视图会自动更新吗？会，但是我们上面的observe没有考虑到交换的情况，所以observe那里更新不了了
//                wordViewModel.updateWords(wordFrom, wordTo);
//
//                //手动通知视图，有内容交换位置了
//                myAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
//                myAdapter2.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
//                Word wordToDelete = filterWords.getValue().get(viewHolder.getAdapterPosition());
                final MyTask currentSwipeTask = allTasks.get(viewHolder.getAdapterPosition());
                //注意，ViewModel从Repository拿数据，而Repository从数据库拿到数据，数据库返回的数据是一个可观察对象
                //在拿到数据的时候，我们已经对它进行监听  wordViewModel.getAllWordsLive().observe ....
                //因此我们只需要调用wordViewModel的delete方法，最终数据库更行数据，观察者发现数据变化，然后做出了变化

                switch (direction) {
                    //左滑动删除任务
                    case ItemTouchHelper.START:
                        myViewModel.deleteTasks(currentSwipeTask);

                        //撤销功能
                        Snackbar.make(
                                /*参数1：显示在哪个组件上？*/
                                requireView().findViewById(R.id.fragment_tasks_view),
                                /*参数2：显示的文字*/
                                "删除了一个任务",
                                /*参数3：显示时长*/
                                Snackbar.LENGTH_SHORT
                                /*监听按钮*/
                        ).setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myViewModel.insertTasks(currentSwipeTask);
                                undoAction = true;
                            }
                            /*不要忘记调用show()方法*/
                        }).show();
                        break;
                    case ItemTouchHelper.END:
//                        showEditDialog(viewHolder, currentSwipeTask);
//                        showEditDialogByAlertDialog(viewHolder, currentSwipeTask);
                        MyEditTaskDialog myEditTaskDialog = new MyEditTaskDialog(myViewModel, tasksAdapter, viewHolder, currentSwipeTask);
                        myEditTaskDialog.show(requireActivity().getSupportFragmentManager(), "missile");
                        break;
                }
            }

            //在滑动的时候，画出浅灰色背景和垃圾桶图标，增强删除的视觉效果
            Drawable trashIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delete_forever_black_24dp);
            Drawable editIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_edit_black_24dp);
            Drawable background = new ColorDrawable(Color.LTGRAY);


            //删除的时候绘图
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                Drawable currentIcon;
                if (dX > 0) {
                    currentIcon = editIcon;
                } else {
                    currentIcon = trashIcon;
                }
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - currentIcon.getIntrinsicHeight()) / 2;

                int iconLeft, iconRight, iconTop, iconBottom;
                int backTop, backBottom, backLeft, backRight;
                backTop = itemView.getTop();
                backBottom = itemView.getBottom();
                iconTop = itemView.getTop() + (itemView.getHeight() - currentIcon.getIntrinsicHeight()) / 2;
                iconBottom = iconTop + currentIcon.getIntrinsicHeight();
                if (dX > 0) {
                    backLeft = itemView.getLeft();
                    backRight = itemView.getLeft() + (int) dX;
                    background.setBounds(backLeft, backTop, backRight, backBottom);
                    iconLeft = itemView.getLeft() + iconMargin;
                    iconRight = iconLeft + currentIcon.getIntrinsicWidth();
                    currentIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else if (dX < 0) {
                    backRight = itemView.getRight();
                    backLeft = itemView.getRight() + (int) dX;
                    background.setBounds(backLeft, backTop, backRight, backBottom);
                    iconRight = itemView.getRight() - iconMargin;
                    iconLeft = iconRight - currentIcon.getIntrinsicWidth();
                    currentIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else {
                    background.setBounds(0, 0, 0, 0);
                    currentIcon.setBounds(0, 0, 0, 0);
                }
                background.draw(c);
                currentIcon.draw(c);
            }
        }).attachToRecyclerView(recyclerView); /*将它和视图关联*/
    }
}
