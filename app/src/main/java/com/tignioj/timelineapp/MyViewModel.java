package com.tignioj.timelineapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tignioj.timelineapp.database.MyTasksRepository;
import com.tignioj.timelineapp.database.TimeLineRepository;
import com.tignioj.timelineapp.database.TimeLineUIRepository;
import com.tignioj.timelineapp.entity.MyTask;
import com.tignioj.timelineapp.entity.MyTaskPoJo;
import com.tignioj.timelineapp.entity.TimeLine;
import com.tignioj.timelineapp.entity.TimeLinePoJo;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;

import java.util.Date;
import java.util.List;

public class MyViewModel extends AndroidViewModel {
    private LiveData<List<TimeLine>> timeLineListLiveData;
    private LiveData<List<TimeLineWithTaskCountsPoJo>> timeLineWithTodayHasNoFinishedTasksCount;

    private TimeLineRepository timeLineRepository;
    private MyTasksRepository myTasksRepository;
    private TimeLineUIRepository timeLineUIRepository;

    //配置文件中, 用户是否开启悬浮窗
    private MutableLiveData<Boolean> isFloating;

    public MutableLiveData<Boolean> getIsFloating() {
        if (isFloating == null) {
            isFloating = timeLineUIRepository.getIsFloating();
        }
        return isFloating;
    }

    public boolean isHasTimeLineFloating() {
        return timeLineUIRepository.isHasTimeLineFloating();
    }

    public void setHasTimeLineFloating(boolean hasTimeLineFloating) {
        timeLineUIRepository.setHasTimeLineFloating(hasTimeLineFloating);
    }

    public boolean isHasTasksFloating() {
        return timeLineUIRepository.getIsHasTasksFloating();
    }

    public void setHasTasksFloating(boolean hasTasksFloating) {
        timeLineUIRepository.setHasTasksFloating(hasTasksFloating);
    }

    public MyViewModel(@NonNull Application application) {
        super(application);
        timeLineUIRepository = TimeLineUIRepository.getInstance(application);
        myTasksRepository = MyTasksRepository.getInstance(application);
        timeLineRepository = TimeLineRepository.getInstance(application);
        this.timeLineListLiveData = timeLineRepository.getAllTimeLinesLive();
        this.timeLineWithTodayHasNoFinishedTasksCount = timeLineRepository.getAllTimeLineWithTodayTaskHasNoFinishedCountLive();
    }

    public LiveData<List<MyTask>> getMyTaskLiveData(long timeLineId) {
        return myTasksRepository.getAllMyTasksLiveByTimeLine(timeLineId);
    }


    public LiveData<List<TimeLine>> getTimeLineListLiveData() {
        return timeLineListLiveData;
    }

    public LiveData<List<TimeLineWithTaskCountsPoJo>> getTimeLineWithTodayHasNoFinishedTasksCount() {
        return timeLineWithTodayHasNoFinishedTasksCount;
    }


    public void insertAll() {
        for (int i = 0; i < 100; i++) {
            TimeLine t1 = new TimeLine(new Date(), new Date(), "timeline" + i);
            timeLineRepository.insertTimeLines(t1);
        }

        for (TimeLine t : timeLineRepository.getAllTimeLines()) {
            for (int i = 0; i < 30; i++) {
                myTasksRepository.insertMyTasks(new MyTask(t.getId(), "task" + i, false, new Date(), new Date(), false));
            }
        }
    }

    public void insertTimeLines(TimeLine... timeLines) {
        timeLineRepository.insertTimeLines(timeLines);
    }

    public void insertTasks(MyTask... tasks) {
        myTasksRepository.insertMyTasks(tasks);
    }


    public void deleteAll() {
        myTasksRepository.deleteAllMyTasks();
        timeLineRepository.deleteAllTimeLines();
    }


    public void updateTasks(MyTask... myTasks) {
        myTasksRepository.updateMyTasks(myTasks);
    }

    public void deleteTasks(MyTask... myTasks) {
        myTasksRepository.deleteMyTasks(myTasks);
    }

    public void deleteTimeLines(TimeLine... timeLineToDelete) {
        timeLineRepository.deleteTimeLines(timeLineToDelete);

    }

    public void updateTimeLines(TimeLine... timeLines) {
        timeLineRepository.updateTimeLines(timeLines);
    }

    public LiveData<List<MyTask>> getTodayMyTaskLiveDataByTimeLineId(long timeLineId, boolean showOld, boolean showCompleted, boolean showFuture) {
        return myTasksRepository.getTodayAllMyTasksLiveByTimeLineId(timeLineId, showOld, showCompleted,  showFuture);
    }

    /**
     * 根据当前时间获取当前Tasks
     *
     * @return
     */
    public LiveData<List<MyTaskPoJo>> getTodayMyTaskLiveDataByCurrentTimeLine() {
        return myTasksRepository.getTodayAllMyTasksLiveByCurrentTimeLine();
    }

    /**
     * 获取当前TimeLine
     * @return
     */
    public LiveData<List<TimeLinePoJo>> getFloatingTimeLinePoJoListLiveData() {
        return timeLineRepository.getAllFloatingTimeLineWithTodayTaskHasNoFinishedCountLive();
    }

    public void refreshFloatingTasks() {
        myTasksRepository.refreshFloatingTasks();
    }

    public void refreshFloatingTimeLines() {
        timeLineRepository.refreshFloatingTimeLines();
    }

    public void refreshTimeLines() {
        timeLineRepository.refreshTimeLines();
    }
}
