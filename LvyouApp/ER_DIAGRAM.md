# 旅游 App 数据库 ER 图（基于 9 张核心表）

## 实体关系图（Mermaid）

以下 ER 图基于：用户、动态、活动、攻略、评论、积分记录、活动参与者、用户关注、通知 共 9 张表。

```mermaid
erDiagram
    users ||--o{ posts : "发布"
    users ||--o{ activities : "组织"
    users ||--o{ guides : "撰写"
    users ||--o{ comments : "发表"
    users ||--o{ points_records : "拥有"
    users ||--o{ activity_participants : "参与"
    users ||--o{ notifications : "接收"
    users ||--o{ user_follows : "作为关注者(follower)"
    users ||--o{ user_follows : "作为被关注者(following)"
    posts ||--o{ comments : "包含"
    comments ||--o{ comments : "父评论-子评论"
    activities ||--o{ activity_participants : "拥有参与者"

    users {
        bigint id PK "用户唯一标识"
        varchar username UK "用户名"
        varchar email UK "邮箱"
        varchar password "加密密码"
        varchar phone "手机"
        varchar nickname "昵称"
        varchar avatar "头像URL"
        varchar role "角色"
        varchar status "状态"
        int points "积分"
        int followers_count "粉丝数"
        int following_count "关注数"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }

    posts {
        bigint id PK "动态唯一标识"
        bigint user_id FK "发布用户ID"
        varchar content "动态内容"
        varchar location "位置"
        varchar type "动态类型"
        varchar status "审核状态"
        int like_count "点赞数"
        int comment_count "评论数"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }

    activities {
        bigint id PK "活动唯一标识"
        bigint organizer_id FK "组织者用户ID"
        varchar title "活动标题"
        varchar description "活动描述"
        varchar destination "目的地"
        datetime start_time "开始时间"
        datetime end_time "结束时间"
        datetime registration_deadline "报名截止"
        int max_participants "最大人数"
        int current_participants "当前人数"
        varchar type "活动类型"
        varchar status "活动状态"
        varchar level "难度等级"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }

    guides {
        bigint id PK "攻略唯一标识"
        bigint author_id FK "作者用户ID"
        varchar title "攻略标题"
        text content "攻略内容"
        varchar summary "摘要"
        varchar type "攻略类型"
        varchar status "发布状态"
        int like_count "点赞数"
        int comment_count "评论数"
        int view_count "浏览数"
        datetime created_at "创建时间"
        datetime updated_at "更新时间"
    }

    comments {
        bigint id PK "评论唯一标识"
        bigint post_id FK "动态ID"
        bigint user_id FK "评论用户ID"
        varchar content "评论内容"
        bigint parent_id FK "父评论ID"
        int like_count "点赞数"
        varchar status "评论状态"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }

    points_records {
        bigint id PK "记录唯一标识"
        bigint user_id FK "用户ID"
        int points "积分变化量"
        varchar type "积分类型"
        varchar description "描述"
        varchar related_type "关联类型"
        bigint related_id "关联ID"
        timestamp created_at "创建时间"
    }

    activity_participants {
        bigint id PK "记录唯一标识"
        bigint activity_id FK "活动ID"
        bigint user_id FK "用户ID"
        varchar status "参与状态"
        varchar application_note "申请备注"
        varchar rejection_reason "拒绝原因"
        timestamp applied_at "申请时间"
        timestamp approved_at "批准时间"
    }

    user_follows {
        bigint id PK "记录唯一标识"
        bigint follower_id FK "关注者用户ID"
        bigint following_id FK "被关注者用户ID"
        timestamp created_at "关注时间"
    }

    notifications {
        bigint id PK "通知唯一标识"
        bigint user_id FK "接收用户ID"
        varchar title "通知标题"
        varchar content "通知内容"
        varchar type "通知类型"
        varchar status "通知状态"
        varchar related_url "相关链接"
        timestamp created_at "创建时间"
    }
```

## 关系说明

| 关系 | 类型 | 说明 |
|------|------|------|
| **users → posts** | 1 : N | 一个用户可发布多条动态 |
| **users → activities** | 1 : N | 一个用户可组织多个活动（organizer_id） |
| **users → guides** | 1 : N | 一个用户可撰写多篇攻略（author_id） |
| **users → comments** | 1 : N | 一个用户可发表多条评论 |
| **users → points_records** | 1 : N | 一个用户有多条积分记录 |
| **users → activity_participants** | 1 : N | 一个用户可参与多个活动 |
| **users → notifications** | 1 : N | 一个用户可接收多条通知 |
| **users ↔ users（user_follows）** | N : M | 用户之间多对多关注（关注者-被关注者） |
| **posts → comments** | 1 : N | 一条动态可有多条评论 |
| **comments → comments** | 1 : N | 评论自关联，支持多级回复（parent_id） |
| **activities → activity_participants** | 1 : N | 一个活动可有多个参与者 |

## 简版 ER 图（仅实体与连线）

若渲染属性过多导致显示异常，可使用下方仅含实体与关系的简版：

```mermaid
erDiagram
    users ||--o{ posts : "发布"
    users ||--o{ activities : "组织"
    users ||--o{ guides : "撰写"
    users ||--o{ comments : "评论"
    users ||--o{ points_records : "积分记录"
    users ||--o{ activity_participants : "参与活动"
    users ||--o{ notifications : "接收通知"
    users ||--o{ user_follows : "follower"
    users ||--o{ user_follows : "following"
    posts ||--o{ comments : "被评论"
    comments ||--o{ comments : "回复"
    activities ||--o{ activity_participants : "参与者"
```

## 外键汇总

| 从表 | 外键字段 | 主表 | 主键 |
|------|----------|------|------|
| posts | user_id | users | id |
| activities | organizer_id | users | id |
| guides | author_id | users | id |
| comments | user_id | users | id |
| comments | post_id | posts | id |
| comments | parent_id | comments | id |
| points_records | user_id | users | id |
| activity_participants | user_id | users | id |
| activity_participants | activity_id | activities | id |
| user_follows | follower_id | users | id |
| user_follows | following_id | users | id |
| notifications | user_id | users | id |

---

*基于文档中给出的 9 张表结构整理，与项目实体类一致。*
