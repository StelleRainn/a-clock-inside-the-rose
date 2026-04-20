package com.stellerainn.backend.mapper;

import com.stellerainn.backend.entity.DailyStats;
import com.stellerainn.backend.entity.PomodoroRecord;
import com.stellerainn.backend.entity.TagFocusStats;
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

    /**
     * 论文对应：5.2.1 年度热力图的底层聚合优化 (算力下推)
     * 在这里，我们将庞大的明细数据（长达一年的流水）统计工作下推至数据库持久层。
     * 使用 DATE() 函数截断毫秒，直接在 DB 层完成以天为维度的 GROUP BY，并用 SUM() 累加有效读秒数。
     * 使得离开数据库返回给 Java 的数据已经是一个轻量的二维统计数组，避免了 JVM 内存溢出风险。
     */
    @Select("SELECT DATE(start_time) as date, SUM(duration_seconds) as totalSeconds " +
            "FROM pomodoro_records " +
            "WHERE user_id = #{userId} AND status = 'COMPLETED' " +
            "AND start_time >= DATE_SUB(CURDATE(), INTERVAL 365 DAY) " +
            "GROUP BY DATE(start_time) " +
            "ORDER BY date ASC")
    List<DailyStats> getDailyFocusTime(Long userId);

    /**
     * 论文对应：5.2.2 多表映射与玫瑰图分步解析
     * 这里通过三表联查 (pomodoro_records -> task_tags -> tags) 直接算出了标签的专注时长分布。
     * (在早期版本中，可能是采用 Java 内存分组，目前版本已利用 SQL 优化)。
     */
    @Select("SELECT t.name as tagName, t.color as tagColor, SUM(pr.duration_seconds) as totalSeconds " +
            "FROM pomodoro_records pr " +
            "JOIN task_tags tt ON pr.task_id = tt.task_id " +
            "JOIN tags t ON tt.tag_id = t.id " +
            "WHERE pr.user_id = #{userId} AND pr.status = 'COMPLETED' " +
            "GROUP BY t.id, t.name, t.color " +
            "ORDER BY totalSeconds DESC")
    List<TagFocusStats> getTagFocusStats(Long userId);
}
