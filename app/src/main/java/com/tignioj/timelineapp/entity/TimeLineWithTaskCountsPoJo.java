package com.tignioj.timelineapp.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class TimeLineWithTaskCountsPoJo {
    @Embedded
    private TimeLine timeLine;

    public TimeLine getTimeLine() {
        return timeLine;
    }

    public void setTimeLine(TimeLine timeLine) {
        this.timeLine = timeLine;
    }

    public int getTasksCount() {
        return tasksCount;
    }

    public void setTasksCount(int tasksCount) {
        this.tasksCount = tasksCount;
    }

    //    @Relation(parentColumn = "id", entityColumn = "timeline_id")
//    public List<MyTask> myTasks;

    @ColumnInfo(name = "is_current")
    private boolean isCurrent;


    @ColumnInfo(name = "tasks_count")
    private int tasksCount;

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}
