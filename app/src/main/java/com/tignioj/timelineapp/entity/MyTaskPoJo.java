package com.tignioj.timelineapp.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class MyTaskPoJo {

    @ColumnInfo(name = "summary")
    private String title;

    @Embedded
    private MyTask myTask;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MyTask getMyTask() {
        return myTask;
    }

    public void setMyTask(MyTask myTask) {
        this.myTask = myTask;
    }
}
