package com.tignioj.timelineapp.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Ignore;

import java.util.List;

public class TimeLinePoJo {
    @ColumnInfo(name = "tasks_count")
    private int tasksCount;

    @Embedded
    private TimeLine timeLine;

//    @ColumnInfo(name = "is_current")
    @Ignore
    private boolean isCurrent;

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    @Ignore
    private List<MyTask> tasks;

    public TimeLine getTimeLine() {
        return timeLine;
    }

    public int getTasksCount() {
        return tasksCount;
    }

    public void setTasksCount(int tasksCount) {
        this.tasksCount = tasksCount;
    }

    public void setTimeLine(TimeLine timeLine) {
        this.timeLine = timeLine;
    }

    public List<MyTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<MyTask> tasks) {
        this.tasks = tasks;
    }
}
