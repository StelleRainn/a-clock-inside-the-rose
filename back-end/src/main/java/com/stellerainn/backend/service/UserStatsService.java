package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.UserStats;
import com.stellerainn.backend.mapper.UserStatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 用户统计服务层 (UserStatsService)
 * 核心功能是“游戏化”(Gamification) 机制的实现：包括等级 (Level)、经验值 (XP)、
 * 以及连续打卡天数 (Streak Days) 的算法计算。
 */
@Service
public class UserStatsService {

    @Autowired
    private UserStatsMapper userStatsMapper;

    /**
     * 获取用户的游戏化统计数据
     * 亮点：采用了类似“懒加载/懒初始化”的设计。
     * @param userId 用户 ID
     * @return 包含等级、XP、连续天数等信息的实体
     */
    public UserStats getUserStats(Long userId) {
        UserStats stats = userStatsMapper.findByUserId(userId);
        if (stats == null) {
            // 如果在 user_stats 表中未找到记录（如新注册用户首次访问相关页面时），
            // 则自动初始化并赋予默认初始值，避免在注册时就执行冗余插入。
            // Create default stats if not exists
            stats = new UserStats();
            stats.setUserId(userId);
            stats.setLevel(1);
            stats.setCurrentXp(0);
            stats.setNextLevelXp(100);
            stats.setTotalFocusMinutes(0);
            stats.setStreakDays(0);
            userStatsMapper.insert(stats);
        }
        return stats;
    }

    /**
     * 增加用户的专注时长并触发游戏化数据的重新计算
     * 这是整个模块中业务逻辑最密集的地方。
     * @param userId 用户 ID
     * @param durationSeconds 本次番茄钟的持续秒数
     */
    public void addFocusTime(Long userId, Integer durationSeconds) {
        UserStats stats = getUserStats(userId);
        
        // 1. 更新总专注时长 (Total Focus Time)
        // 将秒转换为分钟进行累计
        int minutes = durationSeconds / 60;
        stats.setTotalFocusMinutes(stats.getTotalFocusMinutes() + minutes);

        // 2. 更新连续打卡天数 (Streak Days) 的核心算法
        LocalDate today = LocalDate.now();
        if (stats.getLastFocusDate() == null) {
            // 第一次专注，Streak 设为 1
            stats.setStreakDays(1);
        } else if (stats.getLastFocusDate().equals(today.minusDays(1))) {
            // 完美接力：上次专注正好是昨天，Streak + 1
            stats.setStreakDays(stats.getStreakDays() + 1);
        } else if (!stats.getLastFocusDate().equals(today)) {
            // 中断判定：上次专注既不是昨天，也不是今天，说明中间断更了，Streak 重置为 1
            // Reset streak if missed a day (and not today)
            stats.setStreakDays(1);
        }
        // 如果上次专注也是今天，则不进行任何操作（保持原 Streak），最后统一步骤将 last_focus_date 刷新为今天。
        stats.setLastFocusDate(today);

        // 3. 更新经验值与等级计算 (XP & Leveling Curve)
        // Rule: 1 minute = 1 XP (1 分钟专注 = 1 经验值)
        int xpGained = minutes; 
        stats.setCurrentXp(stats.getCurrentXp() + xpGained);

        // 升级判定循环：如果获得的 XP 让当前 XP 超过了下一级所需的阈值，触发升级
        while (stats.getCurrentXp() >= stats.getNextLevelXp()) {
            // 溢出的经验值保留，而不是简单清零（比如差 10 XP 升级，获得了 30 XP，升一级后还剩 20 XP）
            stats.setCurrentXp(stats.getCurrentXp() - stats.getNextLevelXp());
            // 等级 +1
            stats.setLevel(stats.getLevel() + 1);
            // 升级曲线算法 (Leveling curve)：下一级所需的经验值是当前级的 1.2 倍
            // Simple leveling curve: Next level needs 20% more XP
            stats.setNextLevelXp((int) (stats.getNextLevelXp() * 1.2));
        }

        // 将最终计算出的所有状态（总时长、连续天数、新等级、新经验等）打包落库
        userStatsMapper.update(stats);
    }
}
