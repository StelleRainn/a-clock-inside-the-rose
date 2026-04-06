import re

with open('/Users/rainn/Projects/a-clock-inside-the-rose/appendix/ACIR完整实现权威概述.md', 'r') as f:
    content = f.read()

# find where "## 2. 数据库实现介绍" starts
idx = content.find("## 2. 数据库实现介绍")
if idx != -1:
    content = content[:idx]

new_content = """## 2. 数据库实现介绍

高交互的 Web 应用仰赖底层数据结构的严谨性。ACIR 的数据建模是对“人-事-时”三者复杂映射关系的降维解构。

### 2.1 实体关系抽象 (E-R 模型)

系统运转的轴心由多个核心实体咬合而成。用户 (`USERS`) 作为权限的拥有者，与任务 (`TASKS`)、专注流水 (`POMODORO_RECORDS`) 及标签 (`TAGS`) 保持着一对多的辐射关系。
- 为了赋予任务灵活性，标签实体被引入系统，与任务交织成多对多网络 (`TASK_TAGS`)。
- 为消解巨型任务阻力，引入了子任务 (`SUBTASKS`)。
- AI 对话历史 (`CHAT_SESSIONS` / `CHAT_MESSAGES`) 被持久化以提供上下文。
- 游戏化统计 (`USER_STATS`) 与主用户表形成一对一 (`1:1`) 的绑定，分离高频更新与低频基础信息。

```mermaid
erDiagram
    USERS ||--o{ TASKS : "creates"
    USERS ||--o{ POMODORO_RECORDS : "generates"
    USERS ||--o{ TAGS : "owns"
    USERS ||--o{ CHAT_SESSIONS : "initiates"
    USERS ||--|| USER_STATS : "has"
    USERS ||--o{ USER_ACHIEVEMENTS : "unlocks"
    
    TASKS ||--o{ SUBTASKS : "contains"
    TASKS ||--o{ POMODORO_RECORDS : "tracks"
    TASKS ||--o{ TASK_TAGS : "has"
    TAGS ||--o{ TASK_TAGS : "associated with"
    
    CHAT_SESSIONS ||--o{ CHAT_MESSAGES : "contains"
    ACHIEVEMENTS ||--o{ USER_ACHIEVEMENTS : "awarded as"
    
    USERS {
        bigint id PK
        varchar username
        varchar password
        varchar email
        varchar gemini_api_key
    }
    USER_STATS {
        bigint user_id PK, FK
        int level
        int total_focus_minutes
        int streak_days
    }
    TASKS {
        bigint id PK
        bigint user_id FK
        varchar title
        enum status
        enum priority
        int position
    }
    POMODORO_RECORDS {
        bigint id PK
        bigint user_id FK
        bigint task_id FK
        datetime start_time
        datetime end_time
        int duration_seconds
    }
    CHAT_SESSIONS {
        bigint id PK
        bigint user_id FK
        varchar title
    }
    CHAT_MESSAGES {
        bigint id PK
        bigint session_id FK
        varchar role
        text content
    }
```

### 2.2 核心物理表结构与技术设计

在将抽象模型沉淀为 MySQL 物理表时，对字段精度与安全防护进行了重点雕琢：

#### 2.2.1 用户表 (`users`)
存放用户基础信息及凭证资产。
- **技术要点 (安全防护)**：`password` 字段采用了强哈希的 **BCrypt 算法**进行单向加密落盘。由于 `gemini_api_key` 直接关乎调用成本，数据库级别的落盘数据采用了 **AES 对称加密算法**处理。这保证了即使被拖库，攻击者也无法直接盗用接口额度。

| 字段名 | 数据类型 | 描述说明 | 技术备注 |
| :--- | :--- | :--- | :--- |
| `id` | bigint | 用户唯一标识 | PK, AUTO_INCREMENT |
| `username` | varchar(50) | 用户登录名 | - |
| `password` | varchar(255) | 登录密码 | **BCrypt** 单向哈希加密 |
| `gemini_api_key`| varchar(255) | AI 密钥 | **AES** 对称加密存储 |

#### 2.2.2 任务表 (`tasks`)
系统的枢纽，设计难点在于状态控制与物理排序。
- **技术要点 (挤占式重排基石)**：表中预留了 `position` 整型排位索引字段。前端看板进行高频跨列拖拽时，后端会根据此字段执行“挤占式”重排的局部批量更新 SQL，通过 ACID 事务杜绝极端拖拽诱发的索引错乱。

| 字段名 | 数据类型 | 描述说明 | 技术备注 |
| :--- | :--- | :--- | :--- |
| `id` | bigint | 任务唯一标识 | PK, AUTO_INCREMENT |
| `user_id` | bigint | 关联用户 | FK |
| `status` | enum | 任务状态 | TODO / IN_PROGRESS / DONE |
| `position` | int | **拖拽排序位置索引** | 支撑拖拽排位算法的核心字段 |
| `due_date` | datetime | 截止日期 | AI 上下文分析的重要依据 |

#### 2.2.3 专注记录表 (`pomodoro_records`)
承受最大写入压力的核心流水表。
- **技术要点 (算力下推优化)**：为替长周期年度热力图渲染减负，冗余了 `duration_seconds` (有效专注读秒数) 字段。使得利用 MySQL 8.0 的 `DATE()` 函数进行按日聚合 `GROUP BY` 并 `SUM` 累加成为可能，将海量明细数据在离开数据库前高度压缩。

| 字段名 | 数据类型 | 描述说明 | 技术备注 |
| :--- | :--- | :--- | :--- |
| `id` | bigint | 记录唯一标识 | PK, AUTO_INCREMENT |
| `task_id` | bigint | 关联任务ID | FK, 允许为 NULL |
| `start_time` | datetime | 专注开始时间 | - |
| `end_time` | datetime | 专注结束时间 | - |
| `duration_seconds`| int | **专注持续秒数** | 用于热力图的高效聚合运算 |

#### 2.2.4 用户统计表 (`user_stats`)
建立长效激励闭环的数据表。
- **技术要点 (数据剥离策略)**：与主用户表形成 `1:1` 绑定。有效剥离了高频更新的游戏化指标（如经验值累加）与低频修改的账户基础信息，显著降低了高并发环境下的数据库行级锁竞争概率。

| 字段名 | 数据类型 | 描述说明 | 技术备注 |
| :--- | :--- | :--- | :--- |
| `user_id` | bigint | 关联用户ID | PK, FK |
| `level` | int | 当前用户等级 | 游戏化指标 |
| `current_xp` | int | 当前经验值 | 专注后高频累加 |
| `streak_days` | int | 连续专注天数 | 激励闭环 |

#### 2.2.5 关联表与 AI 消息表
- **`task_tags` (任务标签关联表)**：被优雅收敛为纯粹中间映射表，通过组合双外键 `(task_id, tag_id)` 形成联合主键，斩断笛卡尔积灾难，捍卫范式纯洁性。
- **`chat_messages` (AI 消息表)**：区分了 `role` (user/model) 与 `content`，精准复刻人机博弈的上下文链路，这也是大模型进行 **Context Injection (上下文注入)** 的历史数据源泉。

---

## 3. 后端实现介绍

后端是 ACIR 系统的心脏，负责承接前端的高并发请求、执行复杂的业务算法，并保障数据的强一致性与安全性。本项目基于 Spring Boot 3 框架构建，严格遵循了经典的 MVC（Model-View-Controller）分层架构。

### 3.1 后端目录结构与职能剖析

在 `back-end/src/main/java/com/stellerainn/backend` 目录下，各个文件夹各司其职，共同串联起了一个完整的后端生命周期：

- **`controller/`（控制层/表现层）**：
  - **职能**：这是后端的“前台接待员”。负责接收前端发送过来的 HTTP 请求（如 GET、POST），对入参进行初步校验，并将请求路由分发给对应的 Service。处理完成后，再将结果统一包装成 JSON 格式返回给前端。
  - **示例**：`TaskController.java` 处理前端的任务拉取请求。
- **`service/`（业务逻辑层）**：
  - **职能**：后端的“大脑”。负责处理核心的业务逻辑（如校验番茄钟是否作弊、计算拖拽重排的排位变化）。它将复杂的业务拆解，并指挥 Mapper 层去数据库拿数据。本层常被设计为接口与实现类分离（`impl/` 目录），以保证代码的高扩展性。
  - **示例**：`TaskService.java` 中封装了拖拽排序的挤占式算法。
- **`mapper/`（持久层/数据访问层）**：
  - **职能**：后端的“库管员”。负责与底层 MySQL 数据库直接打交道。本项目采用了 MyBatis 框架，在这个目录下，开发者通过简单的注解（如 `@Select`, `@Insert`）或编写 XML 文件，将 Java 方法映射为具体的 SQL 语句。
  - **示例**：`PomodoroMapper.java` 包含了各类复杂的统计查询 SQL。
- **`entity/`（实体层/模型层）**：
  - **职能**：这是 Java 里的“数据库表镜像”。这里的每一个 Java 类（POJO），其属性几乎与数据库表中的字段一一对应。用于在各层之间传递数据对象。
  - **示例**：`PomodoroRecord.java` 对应着专注流水表。
- **`common/`（公共基建层）**：
  - **职能**：存放所有模块共用的基础类。例如 `Result.java` 定义了全局统一的 API 返回格式（包含 `code`, `message`, `data`），确保前端能够用一套标准解析所有响应。
- **`util/`（工具类层）**：
  - **职能**：存放通用的、无状态的辅助工具。
  - **示例**：`EncryptionUtil.java` 提供了 AES 算法，用于给 AI 密钥加解密。

**“一个接口是怎么运行起来的？” (小白白话版)：**
当你在前端点击“查看统计图表”时，前端发起一个请求到 `Controller`。`Controller` 接收后，发现这需要业务计算，便把它扔给 `Service`。`Service` 判断需要查询今年的所有数据，便下达指令给 `Mapper`。`Mapper` 将指令翻译成 SQL 语句，去 MySQL 数据库里把数据捞出来，封装成 `Entity` 实体对象交还给 `Service`。`Service` 处理完毕后还给 `Controller`，`Controller` 最后用 `common` 里的 `Result` 包装好，变成一串漂亮的 JSON 丢回给前端渲染。

### 3.2 完整流转实例：年度热力图聚合算力下推

在论文的第五章中，我们着重提到了“复杂 SQL 聚合下推”。为了让你深刻理解上述的分层协作，我们以**“前端请求热力图数据”**这一核心接口为例，从头到尾走通这套流程：

**第一步：Controller 接收请求**
前端向 `/api/stats/focus-daily` 发起 GET 请求。`StatsController` 拦截该请求，并调用 Service：
```java
// StatsController.java
@RestController
@RequestMapping("/api/stats")
public class StatsController {
    @Autowired
    private StatsService statsService;

    @GetMapping("/focus-daily")
    public Result<List<DailyStats>> getDailyFocusStats(@RequestParam Long userId) {
        // 调用 Service 并用 Result 包装返回
        return Result.success(statsService.getDailyFocusStats(userId));
    }
}
```

**第二步：Service 调度业务**
`StatsService` 接收到指令。对于简单的查询，它可能直接透传；若有复杂逻辑，则会在此处进行数据组装。在这里，它直接调用了 Mapper 去数据库拿结果：
```java
// StatsService.java
@Service
public class StatsService {
    @Autowired
    private PomodoroMapper pomodoroMapper;

    public List<DailyStats> getDailyFocusStats(Long userId) {
        // 将查询任务下发给 Mapper
        return pomodoroMapper.getDailyFocusTime(userId);
    }
}
```

**第三步：Mapper 执行高阶 SQL（核心技术点！）**
为了防止将千万级数据全量拉到 Java 内存中导致 JVM 崩溃（OOM），我们在 `PomodoroMapper` 中写下了一段精妙的 SQL：
```java
// PomodoroMapper.java
@Mapper
public interface PomodoroMapper {
    // 聚合算力下推：利用 DATE() 截断时间戳，利用 SUM() 累加秒数
    @Select("SELECT DATE(start_time) as date, SUM(duration_seconds) as totalSeconds " +
            "FROM pomodoro_records " +
            "WHERE user_id = #{userId} AND status = 'COMPLETED' " +
            "AND start_time >= DATE_SUB(CURDATE(), INTERVAL 365 DAY) " +
            "GROUP BY DATE(start_time) " +
            "ORDER BY date ASC")
    List<DailyStats> getDailyFocusTime(Long userId);
}
```
**原理解析**：这正是论文中提到的“算力下推”策略。我们在 MySQL 层面直接按天（`GROUP BY DATE`）完成了所有数据的分组，并把有效时长累加（`SUM`）。当数据返回给 Java 时，已经是从海量明细压缩成仅有 365 条记录的高度精简的 `DailyStats` 数组了。

---

## 4. 前端实现介绍

前端作为直面用户的“视觉皮囊”与“交互中枢”，承担了营造“心流体验”的重任。本项目基于 Vue 3 (Composition API) 与 Vite 构建，并全面引入了 Pinia 进行状态管理。

### 4.1 前端目录结构与职能剖析

在 `front-end/src` 目录下，前端工程被清晰地切割为以下模块：

- **`api/`**：统一的接口管理中心。通过封装 Axios (`utils/request.js`)，将后端散落的 RESTful 接口按业务模块（如 `auth.js`, `task.js`）抽离成一个个 Promise 函数，方便组件调用并复用。
- **`assets/`**：存放静态资源。如本地的字体文件（`fonts.css`）、Logo 等，Vite 在打包时会对其进行优化与哈希处理。
- **`components/`**：全局可复用的 UI 组件库。
  - **Widgets**：如 `WidgetHeatmap.vue`（热力图组件），被用于构建首页的 Bento Grid（便当盒网格）。
  - **基础组件**：如 `HeaderNav.vue` (顶部导航)、`MobileNavBar.vue` (移动端底部 TabBar，响应式降级核心)。
- **`layout/`**：页面布局框架。`MainLayout.vue` 规定了页面的骨架（Header + RouterView），是所有核心路由的母版。
- **`locales/`**：国际化 (I18n) 语言包，包含 `zh.js` 与 `en.js`，支撑了全站的中英文一键切换。
- **`router/`**：Vue Router 路由配置文件 (`index.js`)。掌管着 URL 与 Vue 视图组件的映射关系，是前端的“指路牌”。
- **`stores/`**：基于 Pinia 的全局状态树。
  - `pomodoro.js`：管理倒计时的生命周期、绝对时间戳防漂移算法；
  - `ai.js`：管理 AI 会话的历史与隐式上下文注入；
  - `theme.js`：管控深浅色模式的切换与全局 CSS 变量。
- **`views/`**：页面级视图组件，根据路由被动态加载，是后续解析的重点。
- **`App.vue` & `main.js`**：前端应用的根组件与入口文件，负责挂载实例、注册路由、引入 Pinia 和 Element Plus。

### 4.2 核心路由页面深度解析

按照 `router/index.js` 的设计，我们从用户动线出发，依次深入解析每一个核心页面（视图）的作用与技术实现：

#### 1. 登录与鉴权 (`/login` -> `LoginView.vue`)
- **功能**：系统的入口，负责用户的注册与登录。
- **技术深度**：表单通过 Element Plus 提供校验规则。成功登录后，前端会截获后端返回的 JWT Token，并持久化到 LocalStorage 中。在随后的每次 Axios 请求拦截器中，该 Token 都会被静默塞入 `Authorization` 请求头中，实现无状态越权阻断。

#### 2. 数据大屏与沉浸式首页 (`/dashboard` -> `DashboardView.vue`)
- **功能**：作为首屏“焦点站 (Focus Station)”，提供全局的数据概览与快速进入专注的入口。
- **技术深度**：
  - **Hero Timer (`TimerSection.vue`)**：首屏被硕大的倒计时占据。结合心流哲学，一旦倒计时开始且检测到鼠标静止超时，系统将自动进入**“禅定模式”**，利用 CSS 动画隐去周围所有组件。
  - **Bento Grid (便当盒网格)**：通过 `DashboardWidgets.vue` 挂载了一系列微型组件（如快速待办、热力图缩略版）。利用 CSS Media Queries 实现了在移动端自动降级为单列流式布局。

#### 3. 交互式任务管理 (`/tasks` -> `TaskListView.vue`)
- **功能**：深度编排用户的待办、进行中与已完成任务。
- **技术深度**：这是系统交互最复杂的页面之一。它实现了经典的 **Kanban（看板）视图**。
  - 拖拽重排基于 `Sortablejs` 底层实现。
  - 当跨列拖拽发生时，前端不仅渲染占位残影（Ghost Element），还会立即向后端发起带有新 `position` 索引的请求，配合后端的“挤占式重排算法”维持前后端状态的强一致性（详见论文 6.2.1 节测试）。

#### 4. 专注番茄钟 (`/pomodoro` -> `PomodoroTimer.vue`)
- **功能**：独立的沉浸式计时页面，允许绑定特定任务。
- **技术深度**：
  - **防漂移算法**：由于原生 `setInterval` 容易因浏览器休眠而停摆，该页面高度依赖 `stores/pomodoro.js` 中的 Pinia 状态机。它记录了开始计时的“绝对时间戳锚点”，即便你关闭电脑后重新打开，剩余时间也会瞬间校准到真实的物理时间。
  - **Zen Note (防走神便签)**：允许在不打断专注的情况下，利用轻量输入框捕获突发灵感。

#### 5. 深度数据统计 (`/stats` -> `StatsOverview.vue`)
- **功能**：将枯燥流水具象化为高阶图表，践行“自我量化”。
- **技术深度**：高度依赖 ECharts 引擎。页面挂载了年度热力图 (Heatmap) 和南丁格尔玫瑰图 (Rose Chart)。
  - 在主题切换时，利用 `theme.js` 同步触发 ECharts 实例的 `setOption` 重绘，确保图表色彩与当前系统的深/浅模式（Dark/Light Mode）完美融合。

#### 6. 伴随式 AI 顾问 (`/intelligent` -> `AiAssistantView.vue`)
- **功能**：集成 Google Gemini 的智能对话面板，提供针对性效率诊断。
- **技术深度（上下文注入技术）**：打破传统 AI 的“空泛”，前端在发起对话请求前，会暗中提取 `pomodoroStore` 中的今日专注时长，以及 `userStore` 中的未完成的高优任务。这些数据被静默组装为一段 System Context（如：“当前用户已专注 300 分钟，仍有两项高优任务待办”），连同用户的真实提问一起推流给云端，彻底消除了 AI 的业务幻觉（详见论文 5.3 节）。

#### 7. 个人主页与配置 (`/profile` -> `ProfileSettings.vue`)
- **功能**：用户资料修改、数据导出（CSV）、AI 密钥托管。
- **技术深度**：引入了 **Glassmorphism (毛玻璃)** 视觉升级。API Key 采用了密码级的脱敏掩码显示（`••••••`）。用户在此录入的明文 Key，一旦提交便会在后端被 AES 加密锁死。

#### 8. 日历回顾视图 (`/calendar` -> `CalendarView.vue`)
- **功能**：独立的数据回顾页面，将历史专注数据与任务映射在日历组件上，提供直观的过往复盘。

"""
with open('/Users/rainn/Projects/a-clock-inside-the-rose/appendix/ACIR完整实现权威概述.md', 'w') as f:
    f.write(content + new_content)
