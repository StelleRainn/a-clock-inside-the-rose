package com.stellerainn.backend.mapper;

import com.stellerainn.backend.entity.DailyStats;
import com.stellerainn.backend.entity.PomodoroRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PomodoroMapper {

    @Insert("INSERT INTO pomodoro_records(user_id, task_id, start_time, end_time, duration_seconds, status) " +
            "VALUES(#{userId}, #{taskId}, #{startTime}, #{endTime}, #{durationSeconds}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PomodoroRecord record);

    @Select("SELECT * FROM pomodoro_records WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<PomodoroRecord> findByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM pomodoro_records WHERE user_id = #{userId} AND status = 'COMPLETED' AND DATE(created_at) = CURDATE()")
    Integer countTodayCompleted(Long userId);

    @Select("SELECT COALESCE(SUM(duration_seconds), 0) FROM pomodoro_records WHERE user_id = #{userId} AND status = 'COMPLETED' AND DATE(created_at) = CURDATE()")
    Long sumTodayDurationSeconds(Long userId);

    @Select("SELECT DATE(start_time) as date, SUM(duration_seconds) as totalSeconds " +
            "FROM pomodoro_records " +
            "WHERE user_id = #{userId} AND status = 'COMPLETED' " +
            "AND start_time >= DATE_SUB(CURDATE(), INTERVAL 365 DAY) " +
            "GROUP BY DATE(start_time) " +
            "ORDER BY date ASC")
    List<DailyStats> getDailyFocusTime(Long userId);
}
