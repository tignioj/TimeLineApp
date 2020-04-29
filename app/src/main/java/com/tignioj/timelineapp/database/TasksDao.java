package com.tignioj.timelineapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.tignioj.timelineapp.entity.MyTask;
import com.tignioj.timelineapp.entity.MyTaskPoJo;

import java.util.List;

@Dao
public interface TasksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTasks(MyTask... myTasks);

    @Update
    void updateTasks(MyTask... myTasks);

    @Delete
    void deleteTasks(MyTask... myTasks);

    @Query("DELETE FROM mytask")
    void deleteAllTasks();

    @Query("SELECT * FROM MyTask ORDER BY id")
    LiveData<List<MyTask>> getAllTasksLive();


    @Query("SELECT * FROM MyTask WHERE timeline_id=:timeline_id ORDER BY id ")
    LiveData<List<MyTask>> getAllTasksLiveByTimeLine(long timeline_id);


    /**
     * 获取今天的数据
     *
     * @return
     */
    @Query("SELECT * FROM MyTask WHERE timeline_id=:timeline_id AND date(datetime(remind_me_date / 1000 , 'unixepoch')) = date('now')  ORDER BY id ")
    LiveData<List<MyTask>> getTodayAllTasksLiveByTimeLine(long timeline_id);

    //    @Query("SELECT * FROM MyTask WHERE timeline_id=:timeline_id  AND date(datetime(create_time / 1000 , 'unixepoch')) = date('now')  ORDER BY id ")
    @RawQuery(observedEntities = MyTask.class)
    LiveData<List<MyTask>> getTodayAllTasksLiveByTimeLine(SupportSQLiteQuery sql);

    /**
     * 获取当前TimeLine还没完成的任务
     *
     * @return
     */
    @Query("select timeline.summary, MyTask.* FROM timeline" +
            " INNER JOIN MyTask ON timeline.id = MyTask.timeline_id " +
            " WHERE time('now', 'localtime') BETWEEN  time((datetime(startTime/1000,'unixepoch', 'localtime')))" +
            " AND time((datetime(endTime/1000,'unixepoch', 'localtime'))) " +
            " AND MyTask.has_finished = 0" +
            " AND date(datetime(remind_me_date / 1000 , 'unixepoch', 'localtime')) = date('now', 'localtime')")
    LiveData<List<MyTaskPoJo>> getTodayAllTasksLiveByCurrentTimeLine();
}
