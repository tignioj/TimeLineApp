package com.tignioj.timelineapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.config.GlobalConfiguration;
import com.tignioj.timelineapp.database.MyTasksRepository;
import com.tignioj.timelineapp.database.TimeLineRepository;
import com.tignioj.timelineapp.entity.MyTask;
import com.tignioj.timelineapp.entity.TimeLinePoJo;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;
import com.tignioj.timelineapp.floating_tasks.FloatingTasksAdapter;
import com.tignioj.timelineapp.floating_tasks.FloatingTasksFragment;
import com.tignioj.timelineapp.floating_timeline.FloatingTimeLineFragment;
import com.tignioj.timelineapp.timeline.timelinelist.TimeLineFragment;
import com.tignioj.timelineapp.utils.CommonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class UpdateService extends Service {

    private static final String CHANNEL_ID = "channel_id_timeline_1";
    private static final int NOTIFICATION_ID = 0x100;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    NotificationCompat.Builder builder;
    MyViewModel myViewModel;
    MyTasksRepository myTasksRepository;
    //响应增删改查数据
    LiveData<List<MyTask>> todayAllMyTasksLive;
    //保存在内存上今天的数据
    List<MyTask> todayAllMyTasks;

    ArrayList<TimeLinePoJo> notificationList;

    TimeLineRepository timeLineRepository;

    public static final int UPDATE_TIMELINE_ON_DAY_CHANGE = 0x1000;
    public static final int UPDATE_TIMELINE_ON_TIME_CHANGE = 0x10001;
    private static final int UPDATE_TODAY_TASKS = 0x10002;


    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();

         builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("timeline")
                .setContentText("timeline content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        createNotificationChannel();

        startForeground(NOTIFICATION_ID, builder.build());


        myViewModel = new MyViewModel(getApplication());


        handler = new Handler(Looper.getMainLooper()) {
            int i = 0;

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);



                LiveData<List<TimeLinePoJo>> floatingTimeLinePoJoListLiveData = timeLineRepository.getAllFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive();


//                myViewModel.getFloatingTimeLinePoJoListLiveData();
                switch (msg.what) {
                    //此时天数改变，需要更新TimeLine的tasksCount, 也就意味着全部timeLine都要请求一遍
                    case UPDATE_TIMELINE_ON_DAY_CHANGE:
                        timeLineRepository.refreshFloatingTimeLines();
                        timeLineRepository.refreshTimeLines();

                        myTasksRepository.refreshFloatingTasks();
//                        floatingTimeLinePoJoListLiveData.observeForever(observer);
                        Log.d("myTag", "update all timeline because day change");
                        break;
                    //根据时间更新数据
                    case UPDATE_TIMELINE_ON_TIME_CHANGE:
                        checkTimeLineChange(floatingTimeLinePoJoListLiveData.getValue(), CHECK_FLAG_TIMELINE_TIME_CHANGE);
                        sendEmptyMessageDelayed(UPDATE_TIMELINE_ON_TIME_CHANGE, 2000);

                        break;
                    case UPDATE_TODAY_TASKS:
                        myTasksRepository.refreshFloatingTasks();
                        break;

                }
            }


        };
        handler.sendEmptyMessage(UPDATE_TIMELINE_ON_TIME_CHANGE);

        myTasksRepository = MyTasksRepository.getInstance(getApplicationContext());

        timeLineRepository = TimeLineRepository.getInstance(getApplicationContext());

        MutableLiveData<List<TimeLinePoJo>> allFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive = timeLineRepository.getAllFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive();
        allFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive.observeForever(new Observer<List<TimeLinePoJo>>() {
            @Override
            public void onChanged(List<TimeLinePoJo> timeLinePoJos) {
                Log.d("VibrateService", "db change");
                checkTimeLineChange(timeLinePoJos, CHECK_FLAG_TIMELINE_DB_CHANGE);
            }
        });


        todayAllMyTasksLive = myTasksRepository.getTodayAllMyRepeatTasksLive();

        todayAllMyTasksLive.observeForever(new Observer<List<MyTask>>() {
            @Override
            public void onChanged(List<MyTask> myTasks) {
                todayAllMyTasks = myTasks;
            }
        });




        initDayStoreInShp(getApplicationContext());


        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        //每秒钟判断一次日期是否变化
                        long dayDiff = dayDiff(getApplicationContext());


                        //表明今天没变化
                        if (dayDiff == 0) {
                            continue;
                        }

                        updateFloatingTimeLine();
//                        updateTimeLine();

                        List<MyTask> newDayTasks = myTasksRepository.getTodayAllMyTask();

                        if (dayDiff > 0) {
                            Log.d("myTag", "update tasks..");
                            //更新数据库数据
                            for (int i = 0; i < todayAllMyTasks.size(); i++) {
                                MyTask myTask = todayAllMyTasks.get(i);
                                if (newDayTasks.contains(myTask) || !myTask.isRepeat()) {
                                    continue;
                                }

                                myTask.setId(0);
                                myTask.setRemindMeDate(new Date());
                                myTask.setHasFinish(false);
                                myTasksRepository.insertMyTasks(myTask);
                            }
                        }
                        //更新内存上今天的数据
//                        todayAllMyTasks = myTasksRepository.getTodayAllMyTask();
                        handler.sendEmptyMessage(UPDATE_TIMELINE_ON_DAY_CHANGE);
                        handler.sendEmptyMessage(UPDATE_TODAY_TASKS);

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }


    private boolean isCurrentTimeLine(TimeLinePoJo t) {
        return CommonUtils.betweenStartTimeAndEndTime(t.getTimeLine().getStartTime(), t.getTimeLine().getEndTime());
    }

    //数据库增删改查
    private final int CHECK_FLAG_TIMELINE_DB_CHANGE = 0x10000;

    //时间变化
    private final int CHECK_FLAG_TIMELINE_TIME_CHANGE = 0x10001;

    private void checkTimeLineChange(List<TimeLinePoJo> value, int checkFlag) {
        boolean changeTimeLine = false;
        boolean isVibrate = false;
        notificationList = new ArrayList<>();
        MutableLiveData<List<TimeLineWithTaskCountsPoJo>> allTimeLineWithTodayTaskHasNoFinishedCountLive = timeLineRepository.getAllTimeLineWithTodayTaskHasNoFinishedCountLive();
        List<TimeLineWithTaskCountsPoJo> timeLines = allTimeLineWithTodayTaskHasNoFinishedCountLive.getValue();

        if (value != null) {



            Log.d("VibrateService", "running");
            for (int j = 0; j < value.size(); j++) {
                TimeLinePoJo t = value.get(j);
                if (isCurrentTimeLine(t)) {
                    notificationList.add(t);
                    if (!t.isCurrent()) {
                        //更新FloatingTimeLine的数据
                        t.setCurrent(true);
                        //更新TimeLine的数据，因为它们都是以startTime作为排序，因此下标是一样的
                        if (timeLines != null) {
                            timeLines.get(j).setCurrent(true);
                        }
                        changeTimeLine = true;
                        isVibrate = isVibrate | t.getTimeLine().isEnableVibrate();
                    }
                } else {
                    if (t.isCurrent()) {
                        t.setCurrent(false);
                        if (timeLines != null) {
                            timeLines.get(j).setCurrent(false);
                        }
                        changeTimeLine = true;
                    }
                }
            }

            switch (checkFlag) {
                case CHECK_FLAG_TIMELINE_DB_CHANGE:

                    if (builder != null) {
                        Date startD = null, endD = null;
                        StringBuilder content = new StringBuilder();
                        content.append("[");
                        //状态栏通知
                        for (int j = 0; j < notificationList.size(); j++) {
                            TimeLinePoJo timeLinePoJo = notificationList.get(j);
                            content.append(timeLinePoJo.getTimeLine().getSummary());
                            if (j < notificationList.size() -1) {
                                content.append(",");
                            }
                            Date tlStart = timeLinePoJo.getTimeLine().getStartTime();
                            Date tlEnd = timeLinePoJo.getTimeLine().getEndTime();
                            if (j == 0) {
                                startD = tlStart;
                                endD = tlEnd;
                            } else {
                                if (CommonUtils.aAfterBWithinDay(startD, tlStart)) {
                                    startD = tlStart;
                                }

                                if (CommonUtils.aAfterBWithinDay(tlEnd, endD)) {
                                    endD = tlEnd;
                                }
                            }
                        }
                        if (startD != null) {
                            String title = CommonUtils.format(startD) + "-" + CommonUtils.format(endD);
                            builder.setContentTitle(title);
                            content.append("]");
                            builder.setContentText(content.toString());
                            startForeground(NOTIFICATION_ID, builder.build());
                        } else {
                            builder.setContentTitle(null);
                            builder.setContentText("当前无任务");
                            startForeground(NOTIFICATION_ID, builder.build());
                        }
                    }

                    //更新今日任务
//                    myTasksRepository.refreshFloatingTasks();
                    handler.sendEmptyMessage(UPDATE_TODAY_TASKS);

                    break;
                case CHECK_FLAG_TIMELINE_TIME_CHANGE:
                    if (changeTimeLine) {
                        Log.d("VibrateService", "update!" + isVibrate + builder + ", " + notificationList.size());
                        //发出通知
                        if (isVibrate) {
                            long[] pattern = new long[]{1000, 1000, 1000, 1000};
                            myVibrate(getApplicationContext(), pattern);
                        }
                        if (builder != null) {
                            Date startD = null, endD = null;
                            StringBuilder content = new StringBuilder();
                            content.append("[");
                            //状态栏通知
                            for (int j = 0; j < notificationList.size(); j++) {
                                TimeLinePoJo timeLinePoJo = notificationList.get(j);
                                content.append(timeLinePoJo.getTimeLine().getSummary());
                                if (j < notificationList.size() -1) {
                                    content.append(",");
                                }
                                Date tlStart = timeLinePoJo.getTimeLine().getStartTime();
                                Date tlEnd = timeLinePoJo.getTimeLine().getEndTime();
                                if (j == 0) {
                                    startD = tlStart;
                                    endD = tlEnd;
                                } else {
                                    if (CommonUtils.aAfterBWithinDay(startD, tlStart)) {
                                        startD = tlStart;
                                    }

                                    if (CommonUtils.aAfterBWithinDay(tlEnd, endD)) {
                                        endD = tlEnd;
                                    }
                                }
                            }
                            if (startD != null) {
                                String title = CommonUtils.format(startD) + "-" + CommonUtils.format(endD);
                                builder.setContentTitle(title);
                                content.append("]");
                                builder.setContentText(content.toString());
                                startForeground(NOTIFICATION_ID, builder.build());
                            } else {
                                builder.setContentTitle(null);
                                builder.setContentText(getString(R.string.no_tasks_notification_text));
                                startForeground(NOTIFICATION_ID, builder.build());
                            }
                        }


                        //更新FloatingTimeLine
                        timeLineRepository.getAllFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive().setValue(value);

                        //更新TimeLine
                        allTimeLineWithTodayTaskHasNoFinishedCountLive.setValue(timeLines);

                        //更新今日任务
                        myTasksRepository.refreshFloatingTasks();
//                        handler.sendEmptyMessage(UPDATE_TODAY_TASKS);

                    }
                    break;

            }

            //发送消息
            //循环TimeChange
//                        Log.d("myTag", "update highlight timeline" + this.i++);
        }
    }

    //==========================================================================
    //shp文件, 存放今天日期字符串
    private static final String SHP_DB_MY_DATE = "SHP_DB_MY_DATE";
    //存储在shp里面的日期的key
    private static final String SHP_TODAY_STRING = "TODAY_STRING";
    //把存储在shp里面的日期加载到内存, 用来记录日期是否变化
    private static String dayStoreInShp;

    /**
     * 注意：此方法在每次天数变化时只能调用一次！
     * 判断日期是否变更，只判断DAY
     * 如果没变，返回0
     * 如果时间倒退了，返回-1
     * 如果时间前进了，返回1
     *
     * @return
     */
    private static long dayDiff(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String today = sdf.format(new Date());
        boolean b = today.equals(dayStoreInShp);

        if (!b) {
            try {
                SharedPreferences shp = context.getSharedPreferences(SHP_DB_MY_DATE, Context.MODE_PRIVATE);
                //如果更改后的时间比原来的时间要早，代表时间倒退了，返回-1, 否则返回1
                int i = new Date().compareTo(sdf.parse(dayStoreInShp));
                Log.d("myTag", dayStoreInShp + "->" + today + " ,i=" + i);
                dayStoreInShp = today;
                SharedPreferences.Editor edit = shp.edit();
                edit.putString(SHP_TODAY_STRING, today);
                edit.apply();
                return i;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            return 0;
        }
    }

    /**
     * 初始化今天
     *
     * @param context
     */
    public static void initDayStoreInShp(Context context) {
        SharedPreferences shp = context.getSharedPreferences(SHP_DB_MY_DATE, Context.MODE_PRIVATE);
        dayStoreInShp = shp.getString(SHP_TODAY_STRING, null);
        //如果还没有存储今天的时间数据，则存一个进去
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

        if (dayStoreInShp == null) {
            SharedPreferences.Editor edit = shp.edit();
            dayStoreInShp = sdf.format(new Date());
            edit.putString(SHP_TODAY_STRING, dayStoreInShp);
            edit.apply();
        }
    }


//    private void updateTimeLine() {
//        Message message = new Message();
//        message.what = TimeLineFragment.UPDATE_TIMELINE_LIST_ON_DAY_CHANGE;
//        handler.sendMessage(message);
//        Log.d("myTag", "update timeline");
//    }

    private void updateFloatingTimeLine() {
        Message message = new Message();
        message.what = FloatingTimeLineFragment.UPDATE_TIMELINE_ON_DAY_CHANGE;
        handler.sendMessage(message);
        Log.d("myTag", "update timeline");
    }


    //===============================================================
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name_keep_running);
            String description = getString(R.string.channel_description_keep_running);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(false);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 震动
     *
     * @param context
     * @param pattern
     */
    public static void myVibrate(Context context, long[] pattern) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // -1 : Play exactly once
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
//                vibrator.vibrate(VibrationEffect.createOneShot(pattern[0], VibrationEffect.DEFAULT_AMPLITUDE));
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

}
