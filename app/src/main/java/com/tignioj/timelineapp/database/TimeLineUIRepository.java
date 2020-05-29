package com.tignioj.timelineapp.database;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

public class TimeLineUIRepository {
    private static  TimeLineUIRepository  INSTANCE;

    public static TimeLineUIRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TimeLineUIRepository(context);
        }
        return INSTANCE;
    }
    Context context;

    private MutableLiveData<Boolean> isFloating;
    private TimeLineUIRepository(Context context) {
        this.context = context;
        this.isFloating = new MutableLiveData<>();
        isFloating.setValue(false);
    }

    public MutableLiveData<Boolean> getIsFloating() {
        return isFloating;
    }
}
