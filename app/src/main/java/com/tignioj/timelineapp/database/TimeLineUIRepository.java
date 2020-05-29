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

    private MutableLiveData<Boolean> isHasTimeLineFloating;
    private MutableLiveData<Boolean> isHasTasksFloating;

    private TimeLineUIRepository(Context context) {
        this.context = context;
        this.isFloating = new MutableLiveData<>();
        isFloating.setValue(false);

        this.isHasTimeLineFloating = new MutableLiveData<>();
        isHasTimeLineFloating.setValue(false);

        this.isHasTasksFloating = new MutableLiveData<>();
        this.isHasTasksFloating.setValue(false);
    }



    public MutableLiveData<Boolean> getIsFloating() {
        return isFloating;
    }

    public boolean isHasTimeLineFloating() {
        return isHasTimeLineFloating.getValue();
    }


    public void setHasTimeLineFloating(boolean hasTimeLineFloating) {
        this.isHasTimeLineFloating.setValue(hasTimeLineFloating);
    }

    public boolean getIsHasTasksFloating() {
        return isHasTasksFloating.getValue();
    }

    public void setHasTasksFloating(boolean hasTasksFloating) {
        this.isHasTasksFloating.setValue(hasTasksFloating);

    }
}
