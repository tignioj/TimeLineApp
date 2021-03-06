package com.tignioj.timelineapp.database;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.tignioj.timelineapp.entity.MyTask;
import com.tignioj.timelineapp.entity.MyTaskPoJo;

import java.util.ArrayList;
import java.util.List;

public class MyTasksRepository {
    private LiveData<List<MyTask>> allMyTaskLive;
    private TasksDao tasksDao;
    MutableLiveData<List<MyTaskPoJo>> todayAllTasksLiveByCurrentTimeLineMutableLive;

    private MyTasksRepository(Context context) {
        TimeLineDataBase timeLineDataBase = TimeLineDataBase.getDataBase(context);
        this.tasksDao = timeLineDataBase.getTasksDao();
        this.allMyTaskLive = tasksDao.getAllTasksLive();
        todayAllTasksLiveByCurrentTimeLineMutableLive = new MutableLiveData<>();
        updateTodayTasks();
    }

    private void updateTodayTasks() {
        LiveData<List<MyTaskPoJo>> todayAllTasksLiveByCurrentTimeLine = tasksDao.getTodayAllTasksLiveByCurrentTimeLine();
        //移除旧的
        todayAllTasksLiveByCurrentTimeLineMutableLive.removeObserver(myObserver);
        //添加到新的
        todayAllTasksLiveByCurrentTimeLine.observeForever(myObserver);
    }

    private final Observer myObserver = new Observer<List<MyTaskPoJo>>() {
        @Override
        public void onChanged(List<MyTaskPoJo> myTaskPoJos) {
            Log.d("myTasksRepo", "refresh" + myTaskPoJos.size());
            todayAllTasksLiveByCurrentTimeLineMutableLive.setValue(myTaskPoJos);
        }
    };


    private static MyTasksRepository INSTANCE;

    public static MyTasksRepository getInstance(Context context) {

        if (INSTANCE == null) {
            synchronized (MyTasksRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MyTasksRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<MyTask>> getTodayAllMyTasksLiveByTimeLineId(long timeLineId, boolean showOld, boolean showCompleted, boolean showFuture) {
        String sql = "select * from myTask where timeline_id=?";
        ArrayList objs = new ArrayList();
        objs.add(timeLineId);

        if (!showCompleted) {
            sql += " and  has_finished = ?";
            objs.add(0);
        }


        String nsql = "";
        if (!showOld && !showFuture) {
            //只显示今天的
            nsql = " and  date(datetime(remind_me_date / 1000 , 'unixepoch', 'localtime')) = date('now', 'localtime')  ";
        } else if (!showOld && showFuture) {
            //显示今天的和以后的
            nsql = " and  date(datetime(remind_me_date / 1000 , 'unixepoch', 'localtime')) >= date('now', 'localtime')  ";
        } else if (showOld && !showFuture) {
            //只显示今天的和以往的
            nsql = " and  date(datetime(remind_me_date / 1000 , 'unixepoch', 'localtime')) <= date('now', 'localtime')  ";
        } else if (showCompleted && showFuture) {
            //显示所有
//            nsql = " and  date(datetime(remind_me_date / 1000 , 'unixepoch', 'localtime')) = date('now', 'localtime')  ";
        }


        sql += nsql;

//        if (!showFuture) {
//            sql += " and  date(datetime(remind_me_date / 1000 , 'unixepoch', 'localtime')) = date('now', 'localtime')  ";
//        }


        Log.d("myTag", sql);

        SimpleSQLiteQuery simpleSQLiteQuery = new SimpleSQLiteQuery(sql, objs.toArray());

        return tasksDao.getTodayAllTasksLiveByTimeLine(simpleSQLiteQuery);
    }

    /**
     * 获取当前时间的任务
     *
     * @return
     */
    public MutableLiveData<List<MyTaskPoJo>> getTodayAllMyTasksLiveByCurrentTimeLine() {
        return todayAllTasksLiveByCurrentTimeLineMutableLive;
    }

    /**
     * 刷新当前浮动任务数据
     */
    public void refreshFloatingTasks() {
        updateTodayTasks();
    }

//    /**
//     * 更新第二天的数据
//     */
//    public List<MyTask> updateRepeatTasks() {
//        return getTodayAllMyTasks();
//    }

    public LiveData<List<MyTask>> getTodayAllMyRepeatTasksLive() {
        return tasksDao.getTodayAllRepeatTasksLive();
    }

    public List<MyTask> getTodayAllMyTask() {
        return tasksDao.getTodayAllTasks();
    }

    static class InsertAsyncTask extends AsyncTask<MyTask, Void, Void> {
        private TasksDao tasksDao;

        public InsertAsyncTask(TasksDao tasksDao) {
            this.tasksDao = tasksDao;
        }

        @Override
        protected Void doInBackground(MyTask... tasks) {
            tasksDao.insertTasks(tasks);
            return null;
        }
    }


    static class UpdateAsyncTask extends AsyncTask<MyTask, Void, Void> {
        private TasksDao tasksDao;

        public UpdateAsyncTask(TasksDao tasksDao) {
            this.tasksDao = tasksDao;
        }

        @Override
        protected Void doInBackground(MyTask... tasks) {
            tasksDao.updateTasks(tasks);
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<MyTask, Void, Void> {
        private TasksDao tasksDao;

        public DeleteAsyncTask(TasksDao tasksDao) {
            this.tasksDao = tasksDao;
        }

        @Override
        protected Void doInBackground(MyTask... tasks) {
            tasksDao.deleteTasks(tasks);
            return null;
        }
    }

    static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private TasksDao tasksDao;

        public DeleteAllAsyncTask(TasksDao tasksDao) {
            this.tasksDao = tasksDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            tasksDao.deleteAllTasks();
            return null;
        }
    }


    public LiveData<List<MyTask>> getAllMyTasksLiveByTimeLine(long timeLineId) {
        return tasksDao.getAllTasksLiveByTimeLine(timeLineId);
    }

    public void insertMyTasks(MyTask... tasks) {
        new InsertAsyncTask(tasksDao).execute(tasks);
    }

    public void updateMyTasks(MyTask... tasks) {
        new UpdateAsyncTask(tasksDao).execute(tasks);
    }

    public void deleteAllMyTasks() {
        new DeleteAllAsyncTask(tasksDao).execute();
    }

    public void deleteMyTasks(MyTask... tasks) {
        new DeleteAsyncTask(tasksDao).execute(tasks);
    }
}
