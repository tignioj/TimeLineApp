package com.tignioj.timelineapp.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class TimeLineWithTaskCountsPoJo {
    @Embedded
    public TimeLine timeLine;

//    @Relation(parentColumn = "id", entityColumn = "timeline_id")
//    public List<MyTask> myTasks;

    @ColumnInfo(name = "is_current")
    private boolean isCurrent;


    @ColumnInfo(name = "tasks_count")
    public int tasksCount;

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}
