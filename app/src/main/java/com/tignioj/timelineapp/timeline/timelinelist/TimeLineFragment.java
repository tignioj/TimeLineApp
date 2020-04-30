package com.tignioj.timelineapp.timeline.timelinelist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.config.GlobalConfiguration;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tignioj.timelineapp.floating_tasks.FloatingTasksFragment;
import com.tignioj.timelineapp.floating_timeline.FloatingTimeLineFragment;
import com.tignioj.timelineapp.utils.CommonUtils;
import com.tignioj.timelineapp.utils.WindowManagerUtils;

import java.util.List;


public class TimeLineFragment extends Fragment {
    private static final String DB_TIMELINE_SHOWING_SETTING = "db_timeline_showing_setting";
    private static final int MY_PERMISSIONS_REQUEST_FLOATING_WINDOW = 0x1000;
    private static final String SHP_SHOW_FLOATING = "show_floating";
    public static final int UPDATE_TIMELINE_LIST_ON_DAY_CHANGE = 0x100;
    private static final int UPDATE_TIMELINE_LIST_ON_TIME_CHANGE = 0x101;
    private MyViewModel myViewModel;
    private RecyclerView recyclerView;
    //滑动时边界的内容
    private DividerItemDecoration dividerItemDecoration;
    private List<TimeLineWithTaskCountsPoJo> allTimeLines;

    LiveData<List<TimeLineWithTaskCountsPoJo>> timeLineWithTodayHasNoFinishedTasksCount;


    Handler handler;

    public Handler getHandler() {
        return handler;
    }

    public TimeLineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_line, container, false);
    }


    /**
     * 检查悬浮窗权限
     */
    private void checkFloatingPermission(Boolean b) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireActivity()) && b) {
                Toast.makeText(requireActivity(), "请授予悬浮窗权限哦~, 设置->应用->TimeLine App->允许显示在其它应用上层", Toast.LENGTH_SHORT).show();
                // Check if Android M or higher
                // Show alert dialog to the user saying a separate permission is needed
                // Launch the settings activity if the user prefers
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(myIntent, MY_PERMISSIONS_REQUEST_FLOATING_WINDOW);
            } else {
                showFloating();
            }
        } else {
            Toast.makeText(requireActivity(), "请授予悬浮窗权限哦~, 设置->应用->TimeLine App->允许显示在其它应用上层", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_PERMISSIONS_REQUEST_FLOATING_WINDOW) {
            if (!Settings.canDrawOverlays(requireActivity())) {
                // You don't have permission
                Toast.makeText(requireActivity(), "没有悬浮窗的TimeLineApp 是没有灵魂嘀~", Toast.LENGTH_SHORT).show();
                //加载用户偏好
                final SharedPreferences shp = requireActivity().getSharedPreferences(DB_TIMELINE_SHOWING_SETTING, Context.MODE_PRIVATE);
                aSwitch.setChecked(false);
                SharedPreferences.Editor edit = shp.edit();
                myViewModel.getIsFloating().setValue(false);
                edit.putBoolean(SHP_SHOW_FLOATING, myViewModel.getIsFloating().getValue());
                edit.apply();
            } else {
                // Do as per your logic
                //显示悬浮窗
                showFloating();
            }
        }
    }


    /**
     * 显示悬浮窗
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloating() {
        if (!Settings.canDrawOverlays(requireActivity())) {
            return;
        }
        Boolean value = myViewModel.getIsFloating().getValue();
        FragmentManager sf = requireActivity().getSupportFragmentManager();
        Fragment floating_tasks = sf.findFragmentByTag(GlobalConfiguration.FLOATING_TASKS_FRAGMENT_TAG);
        Fragment floating_timeline = sf.findFragmentByTag(GlobalConfiguration.FLOATING_TIME_LINE_FRAGMENT_TAG);
        if (floating_tasks == null) floating_tasks = new FloatingTasksFragment();
        if (floating_timeline == null) floating_timeline = new FloatingTimeLineFragment();

        FragmentTransaction ft = sf.beginTransaction();
        if (value) {
            Toast.makeText(requireActivity().getApplicationContext(), "悬浮窗状态:开启", Toast.LENGTH_SHORT).show();
            if (floating_tasks.getView() == null) {
                ft.add(floating_tasks, GlobalConfiguration.FLOATING_TASKS_FRAGMENT_TAG);
            } else if (!floating_tasks.getView().isAttachedToWindow()) {
                WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = WindowManagerUtils.getFloatingTasksWindowManagerParams();
                wm.addView(floating_tasks.getView(), p);
            }
            if (floating_timeline.getView() == null) {
                ft.add(floating_timeline, GlobalConfiguration.FLOATING_TIME_LINE_FRAGMENT_TAG);
            } else if (!floating_timeline.getView().isAttachedToWindow()) {
                WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = WindowManagerUtils.getFloatingTimeLinesWindowManagerParams();
                wm.addView(floating_timeline.getView(), p);
            }


        } else {
            if (floating_tasks.getView() != null) {
                ft.remove(floating_tasks);
            }
            if (floating_timeline.getView() != null) {
                ft.remove(floating_timeline);
            }
            Toast.makeText(requireActivity().getApplicationContext(), "悬浮窗状态:关闭", Toast.LENGTH_SHORT).show();
        }
        ft.commit();
    }


    //悬浮窗开关
    Switch aSwitch;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.timeline_menu, menu);

        //加载用户偏好
        final SharedPreferences shp = requireActivity().getSharedPreferences(DB_TIMELINE_SHOWING_SETTING, Context.MODE_PRIVATE);
        aSwitch = (Switch) menu.findItem(R.id.time_line_switch_floating).getActionView().findViewById(R.id.switchButton);
        boolean isShowFloating = shp.getBoolean(SHP_SHOW_FLOATING, false);
        aSwitch.setChecked(isShowFloating);

        myViewModel.getIsFloating().setValue(isShowFloating);
        Log.d("myTag", "onCreateOptionsMenu");
        myViewModel.getIsFloating().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.d("myTag", "checkFloatingPermission");
                checkFloatingPermission(aBoolean);
            }
        });


        //监听是否关闭悬浮窗
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor edit = shp.edit();
                if (isChecked) {
                    myViewModel.getIsFloating().setValue(true);
                } else {
                    myViewModel.getIsFloating().setValue(false);
                }
                edit.putBoolean(SHP_SHOW_FLOATING, myViewModel.getIsFloating().getValue());
                edit.apply();
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    //如果程序在后台，则不更新界面的数据
    boolean isProgramInBackground;
    Observer observer;

    TimeLineAdapter timeLineAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


        setHasOptionsMenu(true);


        recyclerView = requireView().findViewById(R.id.rcv_show);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);


        timeLineAdapter = new TimeLineAdapter(myViewModel);

        observer = new Observer<List<TimeLineWithTaskCountsPoJo>>() {
            @Override
            public void onChanged(List<TimeLineWithTaskCountsPoJo> timeLineWithTaskCountsPoJos) {
                allTimeLines = timeLineWithTaskCountsPoJos;
                timeLineAdapter.submitList(timeLineWithTaskCountsPoJos);
            }
        };

        recyclerView.setAdapter(timeLineAdapter);

        timeLineWithTodayHasNoFinishedTasksCount = myViewModel.getTimeLineWithTodayHasNoFinishedTasksCount();
        timeLineWithTodayHasNoFinishedTasksCount.observe(requireActivity(), observer);

        //更新界面的数据
        handler = new Handler(Looper.getMainLooper()) {
            int i = 0;

            @Override
            public void handleMessage(@NonNull Message msg) {
                if (isProgramInBackground) {
                    return;
                }
                switch (msg.what) {
                    case UPDATE_TIMELINE_LIST_ON_DAY_CHANGE:
                        myViewModel.refreshTimeLines();
                        timeLineWithTodayHasNoFinishedTasksCount = myViewModel.getTimeLineWithTodayHasNoFinishedTasksCount();
                        timeLineWithTodayHasNoFinishedTasksCount.observe(requireActivity(), observer);
                        break;
                    case UPDATE_TIMELINE_LIST_ON_TIME_CHANGE:
                        List<TimeLineWithTaskCountsPoJo> value = timeLineWithTodayHasNoFinishedTasksCount.getValue();
                        if (value != null) {
                            for (int j = 0; j < value.size(); j++) {
                                TimeLineWithTaskCountsPoJo t = value.get(j);
                                if (isCurrentTimeLine(t)) {
                                    if (!t.isCurrent()) {
                                        t.setCurrent(true);
                                        timeLineAdapter.notifyItemChanged(j);
                                    }
                                } else {
                                    if (t.isCurrent()) {
                                        t.setCurrent(false);
                                        timeLineAdapter.notifyItemChanged(j);
                                    }
                                }
                            }
                        }
                        Message message = new Message();
                        message.what = UPDATE_TIMELINE_LIST_ON_TIME_CHANGE;
                        sendMessageDelayed(message, 1000);
//                        Log.d("myTag", "update timeline list" + i++);
                }
            }

            private boolean isCurrentTimeLine(TimeLineWithTaskCountsPoJo t) {
                return CommonUtils.betweenStartTimeAndEndTime(t.timeLine.getStartTime(), t.timeLine.getEndTime());
            }
        };


        //初始化滑动边界组件
        dividerItemDecoration = new DividerItemDecoration(
                requireActivity(),
                /*列表的方向*/
                DividerItemDecoration.VERTICAL
        );

        //添加滑动修饰
        recyclerView.addItemDecoration(dividerItemDecoration);


        ((FloatingActionButton) requireView().findViewById(R.id.floatingActionButtonAddTimeLine)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_timeLineFragment_to_addTimeLineFragment);
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
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
//                Word wordToDelete = filterWords.getValue().get(viewHolder.getAdapterPosition());
                final TimeLineWithTaskCountsPoJo currentSwipeTimeLine = allTimeLines.get(viewHolder.getAdapterPosition());
                //注意，ViewModel从Repository拿数据，而Repository从数据库拿到数据，数据库返回的数据是一个可观察对象
                //在拿到数据的时候，我们已经对它进行监听  wordViewModel.getAllWordsLive().observe ....
                //因此我们只需要调用wordViewModel的delete方法，最终数据库更行数据，观察者发现数据变化，然后做出了变化

                if (ItemTouchHelper.START == direction) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setTitle("确认删除吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myViewModel.deleteTimeLines(currentSwipeTimeLine.timeLine);
                        }
                    });

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //刷新界面
                            timeLineAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    });
                    AlertDialog dialog = builder.create();

                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            timeLineAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    });
                    dialog.show();
                } else {
                    //编辑页面
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(getString(R.string.timeline_to_edit), currentSwipeTimeLine.timeLine);
                    timeLineAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    Navigation.findNavController(requireView()).navigate(R.id.action_timeLineFragment_to_addTimeLineFragment, bundle);
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

                //右滑动删除
                if (dX > 0) {
                    backLeft = itemView.getLeft();
                    backRight = itemView.getLeft() + (int) dX;
                    background.setBounds(backLeft, backTop, backRight, backBottom);
                    iconLeft = itemView.getLeft() + iconMargin;
                    iconRight = iconLeft + currentIcon.getIntrinsicWidth();
                    currentIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    //左滑动编辑
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        isProgramInBackground = true;
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        isProgramInBackground = false;
        Message message = new Message();
        message.what = UPDATE_TIMELINE_LIST_ON_TIME_CHANGE;
        handler.sendMessageDelayed(message, 1000);
    }
}
