package com.tignioj.timelineapp.floating_timeline;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.config.GlobalConfiguration;
import com.tignioj.timelineapp.entity.TimeLinePoJo;
import com.tignioj.timelineapp.floating_tasks.FloatingTasksFragment;
import com.tignioj.timelineapp.utils.CommonUtils;
import com.tignioj.timelineapp.utils.WindowManagerUtils;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FloatingTimeLineFragment extends Fragment {
    public static final int UPDATE_TIMELINE_ON_DAY_CHANGE = 0x101;
    private static final int UPDATE_TIMELINE_ON_TIME_CHANGE = 0x102;
    MyViewModel myViewModel;
    View viewInWindowManager;
    RecyclerView rcv;
    Handler handler;

    public Handler getHandler() {
        return handler;
    }

    public MyViewModel getMyViewModel() {
        return myViewModel;
    }

    /**
     * 什么情况下需要更新数据？
     * 1. 时间变动
     * 时间比较:
     * 遍历数据
     * 2. 数据增删改查
     */
    public FloatingTimeLineFragment() {
        // Required empty public constructor
    }

    Observer observer = new Observer<List<TimeLinePoJo>>() {
        int i = 0;

        @Override
        public void onChanged(List<TimeLinePoJo> timeLinePoJos) {
            Log.d("myTag", "observe timeline" + i++);
            adapter.submitList(timeLinePoJos);
        }
    };


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        this.myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        viewInWindowManager = inflater.inflate(R.layout.fragment_floating_time_line, container, false);
        if (!myViewModel.isHasTimeLineFloating()) {
            WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = WindowManagerUtils.getFloatingTimeLinesWindowManagerParams();
            myViewModel.setHasTimeLineFloating(true);
            wm.addView(viewInWindowManager, p);
        }

        FragmentManager sf = requireActivity().getSupportFragmentManager();
        final FloatingTasksFragment floatingTasksFragment = (FloatingTasksFragment) sf.findFragmentByTag(GlobalConfiguration.FLOATING_TASKS_FRAGMENT_TAG);
        handler = new Handler(Looper.getMainLooper()) {
            int i = 0;

            @Override
            public void handleMessage(@NonNull Message msg) {
                if (isEnd) {
                    return;
                }
                super.handleMessage(msg);
                switch (msg.what) {
                    //此时天数改变，需要更新TimeLine的tasksCount, 也就意味着全部timeLine都要请求一遍
                    case UPDATE_TIMELINE_ON_DAY_CHANGE:
                        myViewModel.refreshFloatingTimeLines();
                        timeLinePoJoLiveData = myViewModel.getFloatingTimeLinePoJoListLiveData();
                        timeLinePoJoLiveData.observeForever(observer);
                        Log.d("myTag", "update all timeline because day change");
                        break;
                    //根据时间更新数据
                    case UPDATE_TIMELINE_ON_TIME_CHANGE:
                        List<TimeLinePoJo> value = timeLinePoJoLiveData.getValue();
                        if (value != null) {
                            for (int j = 0; j < value.size(); j++) {
                                TimeLinePoJo t = value.get(j);
                                if (isCurrentTimeLine(t)) {
                                    if (!t.isCurrent()) {
                                        t.setCurrent(true);
                                        adapter.notifyItemChanged(j);
                                        floatingTasksFragment.refreshTasks();
                                    }
                                } else {
                                    if (t.isCurrent()) {
                                        t.setCurrent(false);
                                        adapter.notifyItemChanged(j);
                                        floatingTasksFragment.refreshTasks();
                                    }
                                }
                            }
                        }
                        //发送消息
                        Message message = new Message();
                        //循环TimeChange
                        message.what = UPDATE_TIMELINE_ON_TIME_CHANGE;
                        sendMessageDelayed(message, 1000);
//                        Log.d("myTag", "update highlight timeline" + this.i++);
                        break;
                }
            }
        };
        return viewInWindowManager;
    }

    private boolean isCurrentTimeLine(TimeLinePoJo timeLinePoJo) {
        return CommonUtils.betweenStartTimeAndEndTime(timeLinePoJo.getTimeLine().getStartTime(), timeLinePoJo.getTimeLine().getEndTime());
    }


    FloatingTimeLineAdapter adapter;

    LiveData<List<TimeLinePoJo>> timeLinePoJoLiveData;

    //视图映射操作
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rcv = viewInWindowManager.findViewById(R.id.rcv);
    }

    //视图逻辑初始化
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //发送更新Current timeline的消息
        Message message = new Message();
        message.what = UPDATE_TIMELINE_ON_TIME_CHANGE;
        handler.sendMessage(message);

        super.onActivityCreated(savedInstanceState);
        adapter = new FloatingTimeLineAdapter();
        timeLinePoJoLiveData = myViewModel.getFloatingTimeLinePoJoListLiveData();
        timeLinePoJoLiveData.observeForever(observer);
        this.rcv.setAdapter(adapter);
        this.rcv.setLayoutManager(new LinearLayoutManager(requireActivity()));

    }

    private boolean isEnd = false;

    @Override
    public void onStart() {
        super.onStart();
        isEnd = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        Log.d("myTag", "destroy");
        super.onDestroy();
        //结束时，停止handler
        isEnd = true;

        if (viewInWindowManager.isAttachedToWindow()) {
            //移除悬浮窗
            Log.d("myTag", "remove Floating TimeLine");
            WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(viewInWindowManager);
        }
    }
}
