-- MySQL dump 10.13  Distrib 9.5.0, for macos15 (arm64)
--
-- Host: 127.0.0.1    Database: ACIR
-- ------------------------------------------------------
-- Server version	9.5.0-commercial

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ 'b1321cf8-d5ad-11f0-96b1-ef1fc9958e07:1-235';

--
-- Table structure for table `achievements`
--

DROP TABLE IF EXISTS `achievements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `achievements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `criteria_type` varchar(50) DEFAULT NULL,
  `criteria_value` int NOT NULL,
  `icon_name` varchar(50) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `achievements`
--

LOCK TABLES `achievements` WRITE;
/*!40000 ALTER TABLE `achievements` DISABLE KEYS */;
INSERT INTO `achievements` (`id`, `name`, `description`, `criteria_type`, `criteria_value`, `icon_name`, `created_at`) VALUES (1,'Novice Focus','Complete 25 minutes of focus','TOTAL_FOCUS_TIME',25,'medal','2026-03-06 15:42:45'),(2,'Focus Master','Complete 1000 minutes of focus','TOTAL_FOCUS_TIME',1000,'trophy','2026-03-06 15:42:45'),(3,'Consistency is Key','Reach a 3-day streak','STREAK',3,'calendar','2026-03-06 15:42:45'),(4,'Task Crusher','Complete 10 tasks','TASK_COMPLETED',10,'finished','2026-03-06 15:42:45');
/*!40000 ALTER TABLE `achievements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `role` varchar(50) NOT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `session_id` (`session_id`),
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
INSERT INTO `chat_messages` (`id`, `session_id`, `role`, `content`, `created_at`) VALUES (1,1,'user','你好！','2026-03-16 14:43:58'),(2,1,'model','你好，**rosaone**！我是你的生产力助手 ACIR。\n\n---\n\n### 📋 当前状态概览\n**模式：** 专注模式 (Focus)\n**今日专注时长：** `0 分钟`\n\n---\n\n### 🚀 任务情报\n你目前有 **4 项高优先级任务** 待处理：\n1.  **毕业论文编纂** 🎓\n2.  **Learning Python** 🐍\n3.  **Graduation Design** 🎨\n4.  **实习手册盖章** ✍️\n\n---\n\n### 💡 ACIR 建议\n既然今天还没正式开始计时，现在正是切入**毕业论文**或**毕业设计**的好时机。\n\n**“万事开头难，先专注 25 分钟如何？”**\n\n准备好开始了吗？点击开始，我会为你记录每一分进步。','2026-03-16 14:44:03'),(3,2,'user','我最近有什么任务需要加急？','2026-03-16 14:44:30'),(4,2,'model','你好，**rosaone**。根据当前的任务列表，你有 **4 项高优先级任务** 需要尽快推进：\n\n### 🚨 待办加急清单\n*   **[HIGH] 毕业论文编纂** —— 建议作为今日首要攻克目标。\n*   **[HIGH] Graduation Design** —— 与论文进度高度关联，需同步推进。\n*   **[HIGH] Learning Python** —— 保持学习节奏，避免知识断层。\n*   **[HIGH] 实习手册盖章** —— 涉及外部沟通，建议在办公时间内优先处理。\n\n---\n\n### 💡 ACIR 智能建议\n> **当前状态：** 专注时长 0 分钟。\n> \n> 你刚刚完成了“火旋风-UI3-大作业”，状态正佳。建议立即开启 **25分钟番茄钟**，从最核心的 **毕业论文** 开始，打破今日的“零记录”。\n\n**准备好进入深度工作模式了吗？**','2026-03-16 14:44:36'),(5,2,'user','毕业论文编纂有什么技巧？可以按步骤分别说说吗？','2026-03-16 14:46:04'),(6,2,'model','你好，**rosaone**。毕业论文编纂是一项系统工程，建议采用“**模块化构建**”策略。\n\n以下是 ACIR 为你拆解的五个高效步骤：\n\n### 📘 毕业论文编纂五步法\n\n#### 1. 骨架先行 (Outlining)\n*   **技巧：** 不要直接写正文。先建立一个完整的 **三级标题大纲**。\n*   **重点：** 确保逻辑链条完整（现状分析 → 提出问题 → 研究方法 → 实验/论证 → 结论）。\n\n#### 2. 文献驱动 (Literature Management)\n*   **技巧：** 使用 Zotero 或 EndNote 管理文献。\n*   **重点：** 边读边在文献旁标注“可引用点”。写作时，直接将观点填入大纲对应的位置，而非重新翻找。\n\n#### 3. 模块化填空 (Drafting)\n*   **技巧：** **先完成，再完美。** 避开按顺序写作，先写你最熟悉的章节（如：实验步骤、数据分析或结论）。\n*   **重点：** 每天设定一个“字数下限”，保持手感。\n\n#### 4. 格式同步 (Formatting)\n*   **技巧：** **提前配置样式表。** 设置好各级标题、正文、图表说明的字体和间距。\n*   **重点：** 避免在最后一天才处理格式，那样极易出错且压力巨大。\n\n#### 5. 逻辑润色 (Polishing)\n*   **技巧：** 完成初稿后，放置 24 小时再回头读。\n*   **重点：** 检查段落间的衔接词，确保论据能支撑论点。\n\n---\n\n### 💡 ACIR 智能建议\n> **当前状态：** 专注时长 0 分钟。\n> \n> 既然已经掌握了步骤，不如现在就开始 **第一步：搭建大纲**。\n>\n> 建议开启 **30分钟深度专注模式**。只需列出前三章的标题，你就已经战胜了拖延！\n\n**准备好点击“启动”开启你的学术时刻了吗？**','2026-03-16 14:46:11');
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_sessions`
--

DROP TABLE IF EXISTS `chat_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) DEFAULT 'New Chat',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `chat_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_sessions`
--

LOCK TABLES `chat_sessions` WRITE;
/*!40000 ALTER TABLE `chat_sessions` DISABLE KEYS */;
INSERT INTO `chat_sessions` (`id`, `user_id`, `title`, `created_at`, `updated_at`) VALUES (1,2,'你好！','2026-03-16 14:43:58','2026-03-16 14:43:58'),(2,2,'我最近有什么任务需要加急？','2026-03-16 14:44:30','2026-03-16 14:44:30');
/*!40000 ALTER TABLE `chat_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pomodoro_records`
--

DROP TABLE IF EXISTS `pomodoro_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pomodoro_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_id` bigint DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `duration_seconds` int NOT NULL,
  `status` enum('COMPLETED','INTERRUPTED') DEFAULT 'COMPLETED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `task_id` (`task_id`),
  CONSTRAINT `pomodoro_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `pomodoro_records_ibfk_2` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pomodoro_records`
--

LOCK TABLES `pomodoro_records` WRITE;
/*!40000 ALTER TABLE `pomodoro_records` DISABLE KEYS */;
INSERT INTO `pomodoro_records` (`id`, `user_id`, `task_id`, `start_time`, `end_time`, `duration_seconds`, `status`, `created_at`) VALUES (1,2,NULL,'2026-03-04 22:56:14','2026-03-04 22:56:24',10,'COMPLETED','2026-03-04 14:56:24'),(2,2,NULL,'2026-03-04 23:18:56','2026-03-04 23:19:56',60,'COMPLETED','2026-03-04 15:19:56'),(3,2,NULL,'2026-03-05 22:55:16','2026-03-05 22:56:16',60,'COMPLETED','2026-03-05 14:56:15'),(4,2,NULL,'2026-03-05 23:14:35','2026-03-05 23:15:35',60,'COMPLETED','2026-03-05 15:15:34'),(5,2,NULL,'2026-03-06 23:10:44','2026-03-06 23:11:44',60,'COMPLETED','2026-03-06 15:11:44'),(6,2,NULL,'2026-03-06 23:29:52','2026-03-06 23:30:52',60,'COMPLETED','2026-03-06 15:30:51'),(7,2,NULL,'2026-03-07 00:00:41','2026-03-07 00:01:41',60,'COMPLETED','2026-03-06 16:01:40'),(8,2,NULL,'2026-03-09 23:02:43','2026-03-09 23:17:43',900,'COMPLETED','2026-03-09 15:17:42'),(9,2,NULL,'2026-03-10 22:09:00','2026-03-10 22:24:00',900,'COMPLETED','2026-03-10 14:24:00'),(10,2,NULL,'2026-03-10 22:54:16','2026-03-10 23:09:16',900,'COMPLETED','2026-03-10 15:09:15'),(11,2,4,'2026-03-10 23:27:24','2026-03-10 23:42:24',900,'COMPLETED','2026-03-10 15:42:23'),(12,2,NULL,'2026-03-11 00:06:26','2026-03-11 00:07:26',60,'COMPLETED','2026-03-10 16:07:25'),(13,2,3,'2026-03-11 23:11:38','2026-03-11 23:36:38',1500,'COMPLETED','2026-03-11 15:36:38'),(14,2,11,'2026-03-11 23:46:12','2026-03-11 23:56:12',600,'COMPLETED','2026-03-11 15:56:12'),(15,2,4,'2026-03-12 22:38:07','2026-03-12 22:58:07',1200,'COMPLETED','2026-03-12 14:58:07'),(16,2,4,'2026-03-13 00:14:04','2026-03-13 00:24:04',600,'COMPLETED','2026-03-12 16:24:04'),(17,2,NULL,'2026-03-13 23:15:27','2026-03-13 23:40:27',1500,'COMPLETED','2026-03-13 15:40:27'),(18,2,NULL,'2026-03-14 22:36:37','2026-03-14 23:01:37',1500,'COMPLETED','2026-03-14 15:01:36'),(19,2,NULL,'2026-03-16 22:30:27','2026-03-16 22:55:27',1500,'COMPLETED','2026-03-16 14:55:26'),(20,2,12,'2026-03-16 23:49:27','2026-03-17 00:14:27',1500,'COMPLETED','2026-03-16 16:14:26'),(21,2,8,'2026-03-17 22:48:44','2026-03-17 23:13:44',1500,'COMPLETED','2026-03-17 15:13:44');
/*!40000 ALTER TABLE `pomodoro_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subtasks`
--

DROP TABLE IF EXISTS `subtasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subtasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `completed` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `task_id` (`task_id`),
  CONSTRAINT `subtasks_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subtasks`
--

LOCK TABLES `subtasks` WRITE;
/*!40000 ALTER TABLE `subtasks` DISABLE KEYS */;
INSERT INTO `subtasks` (`id`, `task_id`, `title`, `completed`, `created_at`) VALUES (1,7,'趁有空',1,'2026-03-09 15:53:53'),(2,8,'先写大纲',1,'2026-03-09 15:54:14'),(3,8,'然后按大纲一段一段写',0,'2026-03-09 15:54:30'),(4,8,'最后记得排版',0,'2026-03-09 15:54:37');
/*!40000 ALTER TABLE `subtasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `name` varchar(50) NOT NULL,
  `color` varchar(20) DEFAULT '#409eff',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_tag` (`user_id`,`name`),
  CONSTRAINT `tags_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tags`
--

LOCK TABLES `tags` WRITE;
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` (`id`, `user_id`, `name`, `color`, `created_at`) VALUES (1,1,'Work','#409eff','2026-03-09 15:10:46'),(2,1,'Study','#67c23a','2026-03-09 15:10:46'),(3,1,'Personal','#e6a23c','2026-03-09 15:10:46'),(4,1,'Urgent','#f56c6c','2026-03-09 15:10:46'),(5,2,'# Study','#b3e19d','2026-03-09 15:12:01'),(6,2,'# Life','#b3e19d','2026-03-10 16:27:40'),(7,2,'# Programming','#409eff','2026-03-11 15:25:16'),(8,2,'# idea','#a0cfff','2026-03-11 15:25:48'),(9,2,'# Tech','#409eff','2026-03-11 15:29:43'),(10,2,'# Music','#409eff','2026-03-11 15:45:39'),(11,2,'# Games','#909399','2026-03-11 16:06:22'),(12,2,'# Education','#f56c6c','2026-03-11 16:07:39'),(13,2,'#Work','#909399','2026-03-11 16:09:44');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_tags`
--

DROP TABLE IF EXISTS `task_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_tags` (
  `task_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`task_id`,`tag_id`),
  KEY `tag_id` (`tag_id`),
  CONSTRAINT `task_tags_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `task_tags_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_tags`
--

LOCK TABLES `task_tags` WRITE;
/*!40000 ALTER TABLE `task_tags` DISABLE KEYS */;
INSERT INTO `task_tags` (`task_id`, `tag_id`) VALUES (4,5),(8,5),(5,6),(7,6),(9,6),(3,7),(10,8),(6,9),(11,10),(12,11),(13,12),(14,13);
/*!40000 ALTER TABLE `task_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text,
  `status` enum('TODO','IN_PROGRESS','DONE') DEFAULT 'TODO',
  `priority` enum('LOW','MEDIUM','HIGH') DEFAULT 'MEDIUM',
  `due_date` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `position` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `tasks_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks`
--

LOCK TABLES `tasks` WRITE;
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
INSERT INTO `tasks` (`id`, `user_id`, `title`, `description`, `status`, `priority`, `due_date`, `created_at`, `updated_at`, `position`) VALUES (1,1,'Finish ACIR Proposal',NULL,'DONE','HIGH',NULL,'2026-03-03 15:23:17','2026-03-03 15:23:17',0),(2,1,'Implement Frontend',NULL,'IN_PROGRESS','HIGH',NULL,'2026-03-03 15:23:17','2026-03-03 15:23:17',0),(3,2,'Learning Python','Spend some learning Python 3','IN_PROGRESS','HIGH',NULL,'2026-03-03 15:53:39','2026-03-16 16:24:43',3),(4,2,'Graduation Design','毕业设计','IN_PROGRESS','HIGH',NULL,'2026-03-04 15:34:49','2026-03-16 16:24:43',5),(5,2,'Wirte Journal','写日记','DONE','HIGH','2026-03-07 16:00:00','2026-03-04 15:35:33','2026-03-16 16:24:43',9),(6,2,'下载Genmini','','DONE','LOW','2026-03-07 16:00:00','2026-03-04 15:36:13','2026-03-16 16:24:43',6),(7,2,'和姐姐、父母打视频电话','聊聊家常，保持情感链接','DONE','MEDIUM','2026-03-08 08:00:00','2026-03-05 15:27:04','2026-03-16 16:24:43',10),(8,2,'毕业论文编纂','内容、整理、排版','TODO','HIGH','2026-03-15 08:00:00','2026-03-09 15:12:25','2026-03-16 16:24:43',0),(9,2,'整理相册','','DONE','MEDIUM','2026-03-14 13:30:00','2026-03-10 15:37:58','2026-03-16 16:24:43',7),(10,2,'[Zen Note] 添加什么功能好呢？','Captured during focus session','TODO','LOW',NULL,'2026-03-10 16:34:53','2026-03-16 16:24:43',11),(11,2,'听音乐','','DONE','MEDIUM','2026-03-13 16:00:00','2026-03-11 15:45:58','2026-03-16 16:24:43',4),(12,2,'原神启动！','Genshin Impact','TODO','LOW','2026-03-14 16:00:00','2026-03-11 16:06:38','2026-03-16 16:24:43',8),(13,2,'实习手册盖章','','IN_PROGRESS','HIGH','2026-03-21 08:00:00','2026-03-11 16:07:59','2026-03-16 16:24:43',2),(14,2,'火旋风-UI3-大作业','','DONE','HIGH','2026-03-13 05:00:00','2026-03-11 16:10:00','2026-03-16 16:24:43',1);
/*!40000 ALTER TABLE `tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_achievements`
--

DROP TABLE IF EXISTS `user_achievements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_achievements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `achievement_id` bigint NOT NULL,
  `unlocked_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_achievement` (`user_id`,`achievement_id`),
  KEY `achievement_id` (`achievement_id`),
  CONSTRAINT `user_achievements_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_achievements_ibfk_2` FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_achievements`
--

LOCK TABLES `user_achievements` WRITE;
/*!40000 ALTER TABLE `user_achievements` DISABLE KEYS */;
INSERT INTO `user_achievements` (`id`, `user_id`, `achievement_id`, `unlocked_at`) VALUES (2,2,3,'2026-03-06 15:56:39'),(3,2,1,'2026-03-10 14:42:38');
/*!40000 ALTER TABLE `user_achievements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_stats`
--

DROP TABLE IF EXISTS `user_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_stats` (
  `user_id` bigint NOT NULL,
  `level` int DEFAULT '1',
  `current_xp` int DEFAULT '0',
  `next_level_xp` int DEFAULT '100',
  `total_focus_minutes` int DEFAULT '0',
  `streak_days` int DEFAULT '0',
  `last_focus_date` date DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `user_stats_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_stats`
--

LOCK TABLES `user_stats` WRITE;
/*!40000 ALTER TABLE `user_stats` DISABLE KEYS */;
INSERT INTO `user_stats` (`user_id`, `level`, `current_xp`, `next_level_xp`, `total_focus_minutes`, `streak_days`, `last_focus_date`, `updated_at`) VALUES (2,4,39,103,257,2,'2026-03-17','2026-03-17 15:13:44');
/*!40000 ALTER TABLE `user_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_url` varchar(255) DEFAULT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `bio` varchar(255) DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `gemini_api_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `username`, `password`, `email`, `created_at`, `updated_at`, `avatar_url`, `nickname`, `bio`, `gender`, `website`, `gemini_api_key`) VALUES (1,'admin','admin123','admin@example.com','2026-03-03 15:23:17','2026-03-03 15:23:17',NULL,NULL,NULL,NULL,NULL,NULL),(2,'rosaone','123456',NULL,'2026-03-03 15:49:43','2026-03-15 04:20:03','https://avatars.githubusercontent.com/u/178400519?s=400&u=7caa5d3cb1f07a2bd96d4f9ff3cda54eea9cd578&v=4','RosaONE','While the world sleeps, you dream.','OTHER','https://stellerainn.vercel.app/','AIzaSyCc8gHd8hP-U038T5vlI6McgfAgHB5x-3c');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-18  0:09:26
