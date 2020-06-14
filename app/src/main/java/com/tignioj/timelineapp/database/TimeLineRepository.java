package com.tignioj.timelineapp.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.tignioj.timelineapp.entity.TimeLine;
import com.tignioj.timelineapp.entity.TimeLinePoJo;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;

import java.util.List;

public class TimeLineRepository {

    private LiveData<List<TimeLine>> allTimeLineLive;
    private TimeLineDao timeLineDao;
    //主页用的TimeLine
    private MutableLiveData<List<TimeLineWithTaskCountsPoJo>> allTimeLineWithTodayTaskHasNoFinishedCountLive;
    //悬浮窗用的TimeLine
//    private LiveData<List<TimeLinePoJo>> allFloatingTimeLineWithTodayTaskHasNoFinishedCountLive;

    //
    private MutableLiveData<List<TimeLinePoJo>> allFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive;

    private final Observer myObserver = new Observer<List<TimeLineWithTaskCountsPoJo>>() {
        @Override
        public void onChanged(List<TimeLineWithTaskCountsPoJo> timeLineWithTaskCountsPoJos) {
            Log.d("TimeLineRepository", "update allTimeLineWithTodayTaskHasNoFinishedCountLive");
            allTimeLineWithTodayTaskHasNoFinishedCountLive.setValue(timeLineWithTaskCountsPoJos);
        }
    };

    public MutableLiveData<List<TimeLinePoJo>> getAllFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive() {
        return allFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive;
    }



    private static TimeLineRepository INSTANCE;

    public static TimeLineRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TimeLineRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TimeLineRepository(context);
                }
            }
        }
        return INSTANCE;
    }


    private TimeLineRepository(Context context) {
        TimeLineDataBase timeLineDataBase = TimeLineDataBase.getDataBase(context);
        this.timeLineDao = timeLineDataBase.getTimeLineDao();
        this.allTimeLineLive = timeLineDao.getAllTimeLinesLive();
        this.allFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive = new MutableLiveData<>();
        updateMemoryFloatingTimeLines();

        allTimeLineWithTodayTaskHasNoFinishedCountLive = new MutableLiveData<>();
        updateMemoryTimeLines();
    }

    private void updateMemoryFloatingTimeLines() {
        LiveData<List<TimeLinePoJo>> allFloatingTimeLinesWithTodayTasksHasNoFinishedCountLive = timeLineDao.getAllFloatingTimeLinesWithTodayTasksHasNoFinishedCountLive();

        allFloatingTimeLinesWithTodayTasksHasNoFinishedCountLive.observeForever(new Observer<List<TimeLinePoJo>>() {
            @Override
            public void onChanged(List<TimeLinePoJo> timeLinePoJos) {
                allFloatingTimeLineWithTodayTaskHasNoFinishedCountMutableLive.setValue(timeLinePoJos);
            }
        });
    }

    private void updateMemoryTimeLines() {
        LiveData<List<TimeLineWithTaskCountsPoJo>> allTimeLinesWithTodayTasksHasNoFinishedCountLive = timeLineDao.getAllTimeLinesWithTodayTasksHasNoFinishedCountLive();
        //移除旧的
        allTimeLineWithTodayTaskHasNoFinishedCountLive.removeObserver(myObserver);
        //添加到新的
        allTimeLinesWithTodayTasksHasNoFinishedCountLive.observeForever(myObserver);
    }

    /**
     * 当一天改变时，调用这个刷新悬浮窗的数据
     */
    public void refreshFloatingTimeLines() {
        updateMemoryFloatingTimeLines();
    }

    /**
     * 当一天改变时，调用这个刷新非悬浮窗数据的TimeLine
     */
    public void refreshTimeLines() {
        updateMemoryTimeLines();
    }

    static class InsertAsyncTask extends AsyncTask<TimeLine, Void, Void> {
        private TimeLineDao timeLineDao;

        public InsertAsyncTask(TimeLineDao timeLineDao) {
            this.timeLineDao = timeLineDao;
        }

        @Override
        protected Void doInBackground(TimeLine... timeLines) {
            timeLineDao.insertTimeLine(timeLines);
            return null;
        }
    }

    static class UpdateAsyncTask extends AsyncTask<TimeLine, Void, Void> {
        private TimeLineDao timeLineDao;

        public UpdateAsyncTask(TimeLineDao timeLineDao) {
            this.timeLineDao = timeLineDao;
        }

        @Override
        protected Void doInBackground(TimeLine... timeLines) {
            timeLineDao.updateTimeLine(timeLines);
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<TimeLine, Void, Void> {
        private TimeLineDao timeLineDao;

        public DeleteAsyncTask(TimeLineDao timeLineDao) {
            this.timeLineDao = timeLineDao;
        }

        @Override
        protected Void doInBackground(TimeLine... timeLines) {
            timeLineDao.deleteTimeLine(timeLines);
            return null;
        }
    }

    static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private TimeLineDao timeLineDao;

        public DeleteAllAsyncTask(TimeLineDao timeLineDao) {
            this.timeLineDao = timeLineDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            timeLineDao.deleteAllTimeLine();
            return null;
        }
    }

    public LiveData<List<TimeLine>> getAllTimeLinesLive() {
        return this.allTimeLineLive;
    }

    public void insertTimeLines(TimeLine... timeLines) {
        new InsertAsyncTask(timeLineDao).execute(timeLines);
    }

    public void updateTimeLines(TimeLine... timeLines) {
        new UpdateAsyncTask(timeLineDao).execute(timeLines);
    }

    public void deleteAllTimeLines() {
        new DeleteAllAsyncTask(timeLineDao).execute();
    }

    public void deleteTimeLines(TimeLine... timeLines) {
        new DeleteAsyncTask(timeLineDao).execute(timeLines);
    }

    public List<TimeLine> getAllTimeLines() {
        return timeLineDao.getAllTimeLines();
    }

    public MutableLiveData<List<TimeLineWithTaskCountsPoJo>> getAllTimeLineWithTodayTaskHasNoFinishedCountLive() {
        return allTimeLineWithTodayTaskHasNoFinishedCountLive;
    }
}
