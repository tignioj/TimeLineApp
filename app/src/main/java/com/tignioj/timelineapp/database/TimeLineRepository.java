package com.tignioj.timelineapp.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.tignioj.timelineapp.entity.TimeLine;
import com.tignioj.timelineapp.entity.TimeLinePoJo;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;

import java.util.List;

public class TimeLineRepository {

    private LiveData<List<TimeLine>> allTimeLineLive;
    private TimeLineDao timeLineDao;
    //主页用的TimeLine
    private LiveData<List<TimeLineWithTaskCountsPoJo>> allTimeLineWithTodayTaskHasNoFinishedCountLive;
    //悬浮窗用的TimeLine
    private LiveData<List<TimeLinePoJo>> allFloatingTimeLineWithTodayTaskHasNoFinishedCountLive;

    public TimeLineRepository(Context context) {
        TimeLineDataBase timeLineDataBase = TimeLineDataBase.getDataBase(context);
        this.timeLineDao = timeLineDataBase.getTimeLineDao();
        this.allTimeLineLive = timeLineDao.getAllTimeLinesLive();
        this.allTimeLineWithTodayTaskHasNoFinishedCountLive = timeLineDao.getAllTimeLinesWithTodayTasksHasNoFinishedCountLive();
        this.allFloatingTimeLineWithTodayTaskHasNoFinishedCountLive = timeLineDao.getAllFloatingTimeLinesWithTodayTasksHasNoFinishedCountLive();
    }


    public LiveData<List<TimeLinePoJo>> getAllFloatingTimeLineWithTodayTaskHasNoFinishedCountLive() {
        return allFloatingTimeLineWithTodayTaskHasNoFinishedCountLive;
    }

    /**
     * 当一天改变时，调用这个刷新悬浮窗的数据
     */
    public void refreshFloatingTimeLines() {
        this.allFloatingTimeLineWithTodayTaskHasNoFinishedCountLive = timeLineDao.getAllFloatingTimeLinesWithTodayTasksHasNoFinishedCountLive();
    }

    /**
     * 当一天改变时，调用这个刷新非悬浮窗数据的TimeLine
     */
    public void refreshTimeLines() {
        this.allTimeLineWithTodayTaskHasNoFinishedCountLive = timeLineDao.getAllTimeLinesWithTodayTasksHasNoFinishedCountLive();
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

    public LiveData<List<TimeLineWithTaskCountsPoJo>> getAllTimeLineWithTodayTaskHasNoFinishedCountLive() {
        return allTimeLineWithTodayTaskHasNoFinishedCountLive;
    }
}
