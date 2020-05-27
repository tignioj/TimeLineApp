package com.tignioj.timelineapp.tasks;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.tignioj.timelineapp.MainActivity;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.config.GlobalConfiguration;
import com.tignioj.timelineapp.database.MyTasksRepository;
import com.tignioj.timelineapp.entity.MyTask;
import com.tignioj.timelineapp.floating_timeline.FloatingTimeLineFragment;
import com.tignioj.timelineapp.timeline.timelinelist.TimeLineFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateTasksService extends Service {

    /**
     * 作为Activity和Service的中介
     * 当Activity绑定Service的时候，执行onServiceConnected(ComponentName name, IBinder service)
     * 其中第二个参数可以强转为这个类型，进而调用这个类的方法
     * 由于这个类属于MyService的一个内部类，因此它的实例可以访问到Service的成员
     */
    public class FloatingWindowIBinder extends Binder {
        MainActivity mainActivity;

        public void hello() {
            Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_SHORT).show();
        }

        public void startFloating() {
            //显示悬浮窗
//            getSupportFragmentManager().beginTransaction().add(new FloatingWindowFragment(), "tag1").commit();
        }

        public void setContext(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }
    }

    FloatingWindowIBinder floatingWindowIBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        floatingWindowIBinder = new FloatingWindowIBinder();
        return floatingWindowIBinder;
    }

    MyTasksRepository myTasksRepository;
    //响应增删改查数据
    LiveData<List<MyTask>> todayAllMyTasksLive;
    //保存在内存上今天的数据
    List<MyTask> todayAllMyTasks;
    private boolean isEnd;

    @Override
    public void onDestroy() {
        super.onDestroy();
        isEnd = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isEnd = true;
        return super.onUnbind(intent);
    }


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

    @Override
    public void onCreate() {
        super.onCreate();
        myTasksRepository = new MyTasksRepository(getApplicationContext());

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
                    while (!isEnd) {
                        Thread.sleep(1000);
                        //每秒钟判断一次日期是否变化
                        long dayDiff = dayDiff(getApplicationContext());

//                        Log.d("myTag", "diff:" + dayDiff);

                        //表明今天没变化
                        if (dayDiff == 0) {
                            continue;
                        }


                        updateFloatingTimeLine();
                        updateTimeLine();

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
                        todayAllMyTasks = myTasksRepository.getTodayAllMyTask();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).

                start();
    }

    private void updateTimeLine() {
        TimeLineFragment timeLineFragment = (TimeLineFragment) floatingWindowIBinder.mainActivity.getSupportFragmentManager().findFragmentById(R.id.timeLineFragment);
        if (timeLineFragment == null) {
            return;
        }
        Handler handler = timeLineFragment.getHandler();
        Message message = new Message();
        message.what = TimeLineFragment.UPDATE_TIMELINE_LIST_ON_DAY_CHANGE;
        handler.sendMessage(message);
        Log.d("myTag", "update timeline");
    }

    private void updateFloatingTimeLine() {
        FloatingTimeLineFragment floatingTimeLineFragment = (FloatingTimeLineFragment) floatingWindowIBinder.mainActivity.getSupportFragmentManager().findFragmentByTag(GlobalConfiguration.FLOATING_TIME_LINE_FRAGMENT_TAG);
        if (floatingTimeLineFragment == null) {
            return;
        }
        Handler handler = floatingTimeLineFragment.getHandler();
        Message message = new Message();
        message.what = FloatingTimeLineFragment.UPDATE_TIMELINE_ON_DAY_CHANGE;
        handler.sendMessage(message);
        Log.d("myTag", "update timeline");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isEnd = false;
        return super.onStartCommand(intent, flags, startId);
    }
}
