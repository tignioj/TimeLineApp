package com.tignioj.timelineapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.tignioj.timelineapp.entity.TimeLine;
import com.tignioj.timelineapp.entity.TimeLinePoJo;
import com.tignioj.timelineapp.entity.TimeLineWithTaskCountsPoJo;

import java.util.List;

@Dao
public interface TimeLineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTimeLine(TimeLine... timeLines);

    @Update
    void updateTimeLine(TimeLine... timeLines);

    @Delete
    void deleteTimeLine(TimeLine... timeLines);

    @Query("DELETE FROM timeline")
    void deleteAllTimeLine();

    @Query("SELECT * FROM TIMELINE order by  strftime('%H:%M:%S', startTime / 1000, 'unixepoch', 'localtime')")
    LiveData<List<TimeLine>> getAllTimeLinesLive();

    //按照时间排序而不是日期
    @Query("SELECT * FROM TIMELINE order by  strftime('%H:%M:%S', startTime / 1000, 'unixepoch', 'localtime')")
    List<TimeLine> getAllTimeLines();


    /**
     * 查询所有的Timeline以及今日未完成的任务的数量
     * @return
     */
    @Transaction
    @Query("select " +
            " CAST( " +
            "  sum( " +
            "   CASE WHEN  " +
            "    ( " +
            "     time('now',  'localtime') >= time((startTime/1000),  'unixepoch',  'localtime') AND " +
            "     time('now',  'localtime') <= time((endTime/1000),  'unixepoch',  'localtime') " +
            "    ) " +
            "   THEN 1 ELSE 0 end " +
            "   )as bit " +
            "  ) as is_current ," +
            "count(m.id) tasks_count, tl.* from timeline tl left outer join " +
            " (" +
            " SELECT * from MyTask WHERE has_finished = 0" +
            " AND date(datetime(remind_me_date / 1000 , 'unixepoch', 'localtime')) = date('now', 'localtime')" +
            ")" +
            " m on tl.id = m.timeline_id  GROUP by tl.id " +
            "  order by  strftime('%H:%M:%S', startTime / 1000, 'unixepoch', 'localtime');")
    LiveData<List<TimeLineWithTaskCountsPoJo>> getAllTimeLinesWithTodayTasksHasNoFinishedCountLive();


    /*
    select
	count(m.id) tasks_count ,
	tl.*,
	CAST(
		sum(
			CASE WHEN
				-- 条件开始
				(
					time('04:51') > time((startTime/1000),  'unixepoch') AND
					time('04:51') < time((endTime/1000),  'unixepoch')
				)
				-- 条件结束
			THEN 1 ELSE 0 end
			)as bit
		) as isCurrent

from timeline tl left outer join
            (
             SELECT * from MyTask WHERE has_finished = 0
             AND date(datetime(create_time / 1000 , 'unixepoch')) = date('now')
             )
             m on tl.id = m.timeline_id
GROUP by tl.id
              order by  strftime('%H:%M:%S', startTime / 1000, 'unixepoch', 'localtime');
     */
    /**
     * 悬浮窗用的
     * 查询所有的Timeline以及今日未完成的任务的数量
     *
     * @return
     */
//    @Transaction
//    @Query("select " +
//            " CAST( " +
//            "  sum( " +
//            "   CASE WHEN  " +
//            "    ( " +
//            "     time('now',  'localtime') >= time((startTime/1000),  'unixepoch',  'localtime') AND " +
//            "     time('now',  'localtime') <= time((endTime/1000),  'unixepoch',  'localtime') " +
//            "    ) " +
//            "   THEN 1 ELSE 0 end " +
//            "   )as bit " +
//            "  ) as is_current ," +
//            "count(m.id) tasks_count , tl.* from timeline tl left outer join " +
//            " (" +
//            " SELECT * from MyTask WHERE has_finished = 0" +
//            " AND date(datetime(create_time / 1000 , 'unixepoch')) = date('now')" +
//            " ) " +
//            " m on tl.id = m.timeline_id  GROUP by tl.id " +
//            "  order by  strftime('%H:%M:%S', startTime / 1000, 'unixepoch', 'localtime');")
//    LiveData<List<TimeLinePoJo>> getAllFloatingTimeLinesWithTodayTasksHasNoFinishedCountLive();
    @Transaction
    @Query("select " +


            " CAST( " +
            "  sum( " +
            "   CASE WHEN  " +
            "    ( " +
            "     time('now',  'localtime') >= time((startTime/1000),  'unixepoch',  'localtime') AND " +
            "     time('now',  'localtime') <= time((endTime/1000),  'unixepoch',  'localtime') " +
            "    ) " +
            "   THEN 1 ELSE 0 end " +
            "   )as bit " +
            "  ) as is_current ," +


            "count(m.id) tasks_count , tl.* from timeline tl left outer join " +
            " (" +
            " SELECT * from MyTask WHERE has_finished = 0" +
            " AND date(datetime(remind_me_date / 1000 , 'unixepoch', 'localtime')) = date('now', 'localtime')" +
            " ) " +
            " m on tl.id = m.timeline_id  GROUP by tl.id " +
            "  order by  strftime('%H:%M:%S', startTime / 1000, 'unixepoch', 'localtime');")
    LiveData<List<TimeLinePoJo>> getAllFloatingTimeLinesWithTodayTasksHasNoFinishedCountLive();
}
