package com.tignioj.timelineapp.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

//TODO 增加重复字段
@Entity(
        indices = {@Index("timeline_id")},
        foreignKeys = @ForeignKey(entity = TimeLine.class, parentColumns = "id", childColumns = "timeline_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE))
public class MyTask {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "timeline_id")
    private long timeline;

    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "has_finished")
    private boolean hasFinish;

    @ColumnInfo(name = "remind_me_date")
    private Date remindMeDate;

    @ColumnInfo(name = "repeat")
    private boolean repeat;




    public MyTask(long timelineId, String content, boolean hasFinish, Date remindMeDate, boolean repeat) {
        this.timeline = timelineId;
        this.content = content;
        this.hasFinish = hasFinish;
        this.remindMeDate = remindMeDate;
        this.repeat = repeat;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public Date getRemindMeDate() {
        return remindMeDate;
    }

    public void setRemindMeDate(Date remindMeDate) {
        this.remindMeDate = remindMeDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimeline() {
        return timeline;
    }

    public void setTimeline(long timeline) {
        this.timeline = timeline;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHasFinish() {
        return hasFinish;
    }

    public void setHasFinish(boolean hasFinish) {
        this.hasFinish = hasFinish;
    }

    public MyTask() {
    }


    @Override
    public String toString() {
        return "MyTask{" +
                "id=" + id +
                ", timeline=" + timeline +
                ", content='" + content + '\'' +
                ", hasFinish=" + hasFinish +
                ", createTime=" + remindMeDate +
                ", repeat=" + repeat +
                '}';
    }
}
