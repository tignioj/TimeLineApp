package com.tignioj.timelineapp.floating_timeline;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.TimeLinePoJo;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;
import com.tignioj.timelineapp.floating_tasks.FloatingTasksFragment;
import com.tignioj.timelineapp.utils.CommonUtils;
import com.tignioj.timelineapp.utils.WindowManagerUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FloatingTimeLineFragment extends Fragment {
    private static final int UPDATE_TIMELINE = 0x101;
    MyViewModel myViewModel;
    View viewInWindowManager;
    RecyclerView rcv;
    Handler handler;

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

    private static final String SHP_TODAY_STRING = "TODAY_STRING";
    private static final String SHP_DB_MY_DATE = "SHP_DB_MY_DATE";
    private String dayStoreInShp;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        SharedPreferences shp = requireActivity().getSharedPreferences(SHP_DB_MY_DATE, Context.MODE_PRIVATE);
        dayStoreInShp = shp.getString(SHP_TODAY_STRING, null);
        //如果还没有存储今天的时间数据，则存一个进去
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (dayStoreInShp == null) {
            SharedPreferences.Editor edit = shp.edit();
            dayStoreInShp = sdf.format(new Date());
            edit.putString(SHP_TODAY_STRING, dayStoreInShp);
            edit.commit();
        }


        this.myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        viewInWindowManager = inflater.inflate(R.layout.fragment_floating_time_line, container, false);
        if (!myViewModel.isHasTimeLineFloating()) {
            WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = WindowManagerUtils.getWindowManagerTimeLineParams();
            myViewModel.setHasTimeLineFloating(true);
            wm.addView(viewInWindowManager, p);
        }

        FragmentManager sf = requireActivity().getSupportFragmentManager();
        final FloatingTasksFragment floatingTasksFragment = (FloatingTasksFragment) sf.findFragmentByTag("floating_tasks");
        handler = new Handler(Looper.getMainLooper()) {
            int i = 0;

            @Override
            public void handleMessage(@NonNull Message msg) {
                if (isEnd) {
                    return;
                }
                super.handleMessage(msg);
                if (msg.what == UPDATE_TIMELINE) {
                    //TODO 根据时间变化，taskCount数量变化问题
                    if (isDayChange()) {
                        myViewModel.refreshFloatingTimeLines();
                        timeLinePoJoLiveData = myViewModel.getFloatingTimeLinePoJoListLiveData();
                        timeLinePoJoLiveData.observeForever(observer);
                    }

                    //根据时间更新数据
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
                    message.what = UPDATE_TIMELINE;
                    sendMessageDelayed(message, 1000);
                    Log.d("myTag", "update timeline" + this.i++);
                }
            }
        };
        return viewInWindowManager;
    }

    private boolean isCurrentTimeLine(TimeLinePoJo timeLinePoJo) {
        return CommonUtils.betweenStartTimeAndEndTime(timeLinePoJo.getTimeLine().getStartTime(), timeLinePoJo.getTimeLine().getEndTime());
    }

    /**
     * 判断是不是第二天了
     * @return
     */
    private boolean isDayChange() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        boolean b = today.equals(dayStoreInShp);

        if (!b) {
            Log.d("myTag", dayStoreInShp + "->" + today);
            SharedPreferences shp = requireActivity().getSharedPreferences(SHP_DB_MY_DATE, Context.MODE_PRIVATE);
            dayStoreInShp = today;
            SharedPreferences.Editor edit = shp.edit();
            edit.putString(SHP_TODAY_STRING, today);
            edit.commit();
        }
        return !b;
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
        //发送消息
        Message message = new Message();
        message.what = UPDATE_TIMELINE;
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
    public void onDestroyView() {
        super.onDestroyView();
        //结束时，停止handler
        isEnd = true;

        //移除悬浮窗
        WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
        wm.removeView(viewInWindowManager);
    }
}
