package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.Achievement;
import com.stellerainn.backend.entity.UserStats;
import com.stellerainn.backend.mapper.AchievementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成就系统服务层 (AchievementService)
 * 负责获取用户的成就列表，并在用户数据更新时进行成就的自动判定和解锁。
 * 采用了典型的规则引擎/策略模式的雏形，基于配置的 criteria 进行动态匹配。
 */
@Service
public class AchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private UserStatsService userStatsService;

    /**
     * 获取用户的全量成就列表
     * @param userId 用户 ID
     * @return 包含解锁状态 (unlocked) 的成就列表
     */
    public List<Achievement> getUserAchievements(Long userId) {
        // 通过 Mapper 执行左连接 (LEFT JOIN) 查询，拉取所有的预设成就，以及当前用户的解锁记录
        List<Achievement> list = achievementMapper.findAllWithUserStatus(userId);
        for (Achievement a : list) {
            // 如果 unlockedAt 字段不为空，说明关联表中存在解锁记录，将其布尔状态设为 true
            a.setUnlocked(a.getUnlockedAt() != null);
        }
        return list;
    }

    /**
     * 核心判定逻辑：检查并解锁成就
     * 此方法通常在关键业务节点（如完成一个番茄钟、打卡一天等）被异步或同步触发。
     * @param userId 用户 ID
     */
    public void checkAndUnlock(Long userId) {
        // 1. 获取用户当前的实时统计数据（如总专注时长、连续打卡天数等）
        UserStats stats = userStatsService.getUserStats(userId);
        
        // 2. 获取所有的成就（包含用户的解锁状态）
        List<Achievement> achievements = achievementMapper.findAllWithUserStatus(userId);

        // 3. 遍历成就字典进行条件匹配
        for (Achievement a : achievements) {
            // 优化：如果已经解锁，直接跳过，避免重复判定和写库
            if (a.getUnlockedAt() != null) continue; // Already unlocked

            boolean unlocked = false;
            
            // 策略路由：根据成就配置表中不同的条件类型 (CriteriaType) 进行对应的业务指标校验
            // 在大型企业级应用中，这里通常会被重构为策略模式 (Strategy Pattern) 或者引入 Drools 规则引擎
            switch (a.getCriteriaType()) {
                case "TOTAL_FOCUS_TIME":
                    // 判断：总专注时间 >= 成就要求的阈值
                    if (stats.getTotalFocusMinutes() >= a.getCriteriaValue()) unlocked = true;
                    break;
                case "STREAK":
                    // 判断：连续打卡天数 >= 成就要求的阈值
                    if (stats.getStreakDays() >= a.getCriteriaValue()) unlocked = true;
                    break;
                // 扩展点：未来可以在这里非常方便地添加新的判定策略，如 TASK_COMPLETED (完成任务数)
                // Add more cases like TASK_COMPLETED
            }

            // 4. 一旦条件满足，执行写库操作（向中间表插入一条解锁记录）
            if (unlocked) {
                achievementMapper.unlockAchievement(userId, a.getId());
            }
        }
    }
}
