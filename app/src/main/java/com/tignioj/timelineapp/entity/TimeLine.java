package com.tignioj.timelineapp.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "timeline")
public class TimeLine implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private Date startTime;
    private Date endTime;
    private String summary;

    protected TimeLine(Parcel in) {
        id = in.readLong();
        summary = in.readString();
    }

    public static final Creator<TimeLine> CREATOR = new Creator<TimeLine>() {
        @Override
        public TimeLine createFromParcel(Parcel in) {
            return new TimeLine(in);
        }

        @Override
        public TimeLine[] newArray(int size) {
            return new TimeLine[size];
        }
    };

    @Override
    public String toString() {
        return "TimeLine{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", summary='" + summary + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public TimeLine() {
    }

    @Ignore
    public TimeLine(Date startTime, Date endTime, String summary) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.summary = summary;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(summary);
    }
}
