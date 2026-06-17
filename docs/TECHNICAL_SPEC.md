# 畅言论坛 技术说明书

> **文档版本**: 1.0  
> **最后更新**: 2026-06-17  
> **适用版本**: v1.0 (VersionCode 1)

---

## 目录

1. [项目概述](#1-项目概述)
2. [系统架构](#2-系统架构)
3. [技术栈](#3-技术栈)
4. [项目结构](#4-项目结构)
5. [数据库设计](#5-数据库设计)
6. [核心模块设计](#6-核心模块设计)
7. [UI 架构](#7-ui-架构)
8. [依赖库清单](#8-依赖库清单)
9. [数据库版本迁移历史](#9-数据库版本迁移历史)
10. [已知技术债务](#10-已知技术债务)

---

## 1. 项目概述

| 属性 | 值 |
|------|-----|
| 应用名称 | 畅言论坛 (CA for Android) |
| 包名 | `com.forum.cy` |
| 开发语言 | Java 1.8 (JDK 8) |
| 最低 SDK | API 21 (Android 5.0 Lollipop) |
| 目标 SDK | API 33 (Android 13) |
| 编译 SDK | API 34 (Android 14) |
| 版本号 | 1.0 (VersionCode: 1) |
| 构建工具 | Gradle 8.0+ (Version Catalog) |
| 许可证 | MIT |

**应用定位**: 一款纯本地化的校园公告论坛 Android 应用，提供公告浏览、动态发布、工具分享等功能，所有数据存储在设备本地 SQLite 数据库中，无需后端服务器。

---

## 2. 系统架构

### 2.1 架构模式

采用 **MVC (Model-View-Controller)** 架构模式，结合 Android Jetpack Navigation Component 进行页面导航管理。

### 2.2 层级职责

| 层级 | 职责 | 组件 |
|------|------|------|
| **Model (数据层)** | 数据实体定义、数据库 CRUD 操作 | `Post`, `Reply`, `DatabaseHelper` |
| **View (视图层)** | UI 布局、用户交互界面 | Activity, Fragment, XML Layout, Adapter |
| **Controller (控制层)** | 业务逻辑、用户输入处理、状态管理 | Activity/Fragment 直接承担 |

### 2.3 架构特点

- **无 ViewModel**: Activity/Fragment 直接管理 UI 状态和业务逻辑，未使用 MVVM 模式
- **无 Repository 模式**: 数据库访问层 (`DatabaseHelper`) 直接耦合在 Activity/Fragment 中
- **无依赖注入**: 手动创建依赖实例（如 `new DatabaseHelper(context)`）
- **导航管理**: 使用 Jetpack Navigation Component (2.7.5) 进行 Fragment 切换和页面导航
- **本地优先**: 完全离线应用，所有数据操作均在本地完成，无网络请求

---

## 3. 技术栈

### 3.1 开发与构建

| 类别 | 技术 | 版本 |
|------|------|------|
| 开发语言 | Java | 1.8 (JDK 8) |
| 构建工具 | Gradle (AGP) | 8.0+ |
| 版本管理 | Version Catalog | libs.versions.toml |

### 3.2 UI 框架

| 库 | 版本 | 用途 |
|----|------|------|
| Material Design 3 | 1.9.0 | 设计规范、UI 组件库 |
| ConstraintLayout | 2.1.4 | 约束布局 |
| LinearLayout / FrameLayout | 系统组件 | 线性布局、帧布局 |
| RecyclerView | 系统组件 | 列表展示 |
| BottomNavigationView | 系统组件 | 底部导航栏 |

### 3.3 导航

| 库 | 版本 | 用途 |
|----|------|------|
| Navigation Fragment | 2.7.5 | Fragment 导航容器 |
| Navigation UI | 2.7.5 | 导航 UI 绑定 |

### 3.4 数据存储

| 技术 | 实现方式 | 用途 |
|------|----------|------|
| SQLite | `SQLiteOpenHelper` (原生) | 本地数据库，5 张表 |
| SharedPreferences | `AuthManager` 封装 | 登录状态、主题偏好持久化 |
| 本地文件存储 | `FileOutputStream` | 头像图片、帖子图片存储 |

### 3.5 特殊说明

- **无网络库**: 未引入 Retrofit、OkHttp、Volley 等网络库
- **无图片加载库**: 未引入 Glide、Picasso 等图片加载库
- **无异步库**: 未引入 RxJava、Kotlin Coroutines 等异步处理库
- **毛玻璃效果**: 原生 View 实现（`BlurUtil`），无第三方模糊库

---

## 4. 项目结构

```
CAforAndroid/
├── app/
│   ├── build.gradle                          # App 级构建配置
│   └── src/main/
│       ├── AndroidManifest.xml               # 应用清单（5 Activity + 权限声明）
│       ├── java/com/forum/cy/
│       │   │
│       │   ├── [Activity 层]
│       │   ├── MainActivity.java             # 主界面 — 底部导航容器，承载 4 个 Fragment
│       │   ├── LoginActivity.java            # 登录页面
│       │   ├── RegisterActivity.java         # 注册页面
│       │   ├── PostDetailActivity.java       # 帖子详情页 — 多 ViewType RecyclerView + 底部回复栏
│       │   ├── FavoritesActivity.java        # 我的收藏列表页
│       │   ├── NotificationsActivity.java    # 消息通知列表页
│       │   │
│       │   ├── [Fragment 层]
│       │   ├── AnnounceFragment.java         # 公告列表（type=0）
│       │   ├── DynamicFragment.java          # 动态列表（type=1），支持匿名发帖
│       │   ├── ToolsFragment.java            # 工具列表（type=2），带实时搜索
│       │   ├── ProfileFragment.java          # 个人中心 — 头像、收藏、通知、夜间模式
│       │   │
│       │   ├── [Dialog 层]
│       │   ├── PublishPostDialog.java        # 发帖弹窗（支持图片选择、匿名开关）
│       │   ├── LoginDialog.java              # 登录弹窗（磨砂背景，已被 LoginActivity 替代）
│       │   ├── ReplyDialog.java              # 回复弹窗（备用）
│       │   │
│       │   ├── [Adapter 层]
│       │   ├── adapter/
│       │   │   ├── PostAdapter.java          # 通用帖子列表适配器
│       │   │   ├── PostDetailAdapter.java    # 帖子详情页多 ViewType 适配器（头部+回复）
│       │   │   ├── ReplyAdapter.java         # 独立回复列表适配器（带删除按钮）
│       │   │   └── NotificationAdapter.java  # 通知列表适配器（显示未读红点）
│       │   │
│       │   ├── [数据层]
│       │   ├── data/
│       │   │   └── DatabaseHelper.java       # SQLite 数据库（5 张表，v3，580 行）
│       │   │
│       │   ├── [模型层]
│       │   ├── model/
│       │   │   ├── Post.java                 # 帖子实体类（9 个字段）
│       │   │   └── Reply.java                # 回复实体类（6 个字段）
│       │   │
│       │   └── [工具层]
│       │       └── util/
│       │           ├── AuthManager.java      # 认证管理（SharedPreferences 封装）
│       │           └── BlurUtil.java         # 毛玻璃/亚克力效果实现（原生）
│       │
│       └── res/
│           ├── layout/                       # 19 个 XML 布局文件
│           ├── drawable/                     # 10+ 自定义 Drawable 资源
│           ├── navigation/nav_graph.xml      # 底部导航图（4 个 Fragment）
│           ├── anim/                         # 8 个动画资源
│           ├── menu/bottom_nav_menu.xml      # 底部导航菜单
│           ├── values/                       # colors.xml, themes.xml, strings.xml
│           └── values-night/                 # 夜间模式配色
│
├── build.gradle                             # 项目级构建配置
├── settings.gradle                          # 模块配置
├── gradle.properties                        # Gradle 属性
├── README.md                                # 项目说明文档
└── docs/
    ├── TECHNICAL_SPEC.md                    # 本文档 — 技术说明书
    └── PROJECT_INTRODUCTION.md              # 项目介绍文档
```

---

## 5. 数据库设计

### 5.1 基本信息

| 属性 | 值 |
|------|-----|
| 数据库名 | `campus_announcement.db` |
| 当前版本 | 3 |
| 实现方式 | `SQLiteOpenHelper` (原生) |

### 5.2 数据表结构

#### 5.2.1 posts 表（帖子表）

| 字段 | 类型 | 默认值 | 约束 | 说明 |
|------|------|--------|------|------|
| `id` | INTEGER | 自增 | PRIMARY KEY | 帖子 ID |
| `type` | INTEGER | - | - | 帖子类型: 0=公告, 1=动态, 2=工具 |
| `title` | TEXT | - | - | 帖子标题 |
| `content` | TEXT | - | - | 帖子内容 |
| `time` | INTEGER | - | - | 发布时间（毫秒时间戳） |
| `author` | TEXT | - | - | 作者用户名 |
| `anonymous` | INTEGER | 0 | - | 是否匿名: 0=否, 1=是 |
| `reply_count` | INTEGER | 0 | - | 回复计数（v3 新增） |
| `image_path` | TEXT | NULL | - | 帖子附图本地路径（v3 新增） |

**索引**: `idx_posts_type_time` — `(type, time DESC)` 复合索引

#### 5.2.2 users 表（用户表）

| 字段 | 类型 | 默认值 | 约束 | 说明 |
|------|------|--------|------|------|
| `id` | INTEGER | 自增 | PRIMARY KEY | 用户 ID |
| `username` | TEXT | - | UNIQUE | 用户名 |
| `password` | TEXT | - | - | 密码（明文存储） |
| `avatar_path` | TEXT | NULL | - | 头像本地路径（v3 新增） |

#### 5.2.3 replies 表（回复表）

| 字段 | 类型 | 默认值 | 约束 | 说明 |
|------|------|--------|------|------|
| `id` | INTEGER | 自增 | PRIMARY KEY | 回复 ID |
| `post_id` | INTEGER | - | - | 关联的帖子 ID |
| `content` | TEXT | - | - | 回复内容 |
| `time` | INTEGER | - | - | 回复时间（毫秒时间戳） |
| `author` | TEXT | - | - | 回复者用户名 |
| `anonymous` | INTEGER | 0 | - | 是否匿名: 0=否, 1=是 |

**索引**: `idx_replies_post_id` — `(post_id)`

#### 5.2.4 favorites 表（收藏表，v3 新增）

| 字段 | 类型 | 默认值 | 约束 | 说明 |
|------|------|--------|------|------|
| `id` | INTEGER | 自增 | PRIMARY KEY | 收藏记录 ID |
| `user_id` | TEXT | - | NOT NULL | 收藏者用户名 |
| `post_id` | INTEGER | - | NOT NULL | 被收藏的帖子 ID |
| `time` | INTEGER | - | - | 收藏时间（毫秒时间戳） |

**约束**: `UNIQUE(user_id, post_id)` — 防止重复收藏  
**索引**: `idx_favorites_user` — `(user_id)`

#### 5.2.5 notifications 表（通知表，v3 新增）

| 字段 | 类型 | 默认值 | 约束 | 说明 |
|------|------|--------|------|------|
| `id` | INTEGER | 自增 | PRIMARY KEY | 通知 ID |
| `type` | INTEGER | - | - | 通知类型: 0=回复通知 |
| `target_user` | TEXT | - | NOT NULL | 接收通知的用户名 |
| `source_user` | TEXT | - | - | 触发通知的用户名 |
| `post_id` | INTEGER | - | - | 关联的帖子 ID |
| `content` | TEXT | - | - | 通知内容（截取前 30 字符） |
| `is_read` | INTEGER | 0 | - | 是否已读: 0=未读, 1=已读 |
| `time` | INTEGER | - | - | 通知时间（毫秒时间戳） |

**索引**: `idx_notif_target` — `(target_user, is_read)` 复合索引

### 5.3 数据库 API 方法清单

#### 帖子 CRUD

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `insertPost(Post)` | Post 对象 | long (新 ID) | 插入新帖子 |
| `updatePost(postId, title, content)` | 帖子 ID, 标题, 内容 | boolean | 更新帖子标题和内容 |
| `updatePostImage(postId, imagePath)` | 帖子 ID, 图片路径 | void | 更新帖子图片路径 |
| `deletePost(postId)` | 帖子 ID | boolean | 删除帖子（级联删除回复） |
| `getPostById(id)` | 帖子 ID | Post | 按 ID 查询单个帖子 |
| `getAllPosts()` | 无 | List\<Post\> | 查询所有帖子（时间倒序） |
| `getPostsByType(type)` | 帖子类型 | List\<Post\> | 按类型查询帖子 |
| `getPostsByTypePaged(type, lastPostId, pageSize)` | 类型, 游标 ID, 页大小 | List\<Post\> | 按类型分页查询 |
| `searchPosts(type, keyword)` | 类型, 关键词 | List\<Post\> | 按类型+关键词模糊搜索 |

#### 回复 CRUD

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `insertReply(Reply)` | Reply 对象 | long (新 ID) | 插入回复（事务内同步递增 reply_count） |
| `deleteReply(replyId)` | 回复 ID | boolean | 删除回复（事务内同步递减 reply_count） |
| `getRepliesByPostId(postId)` | 帖子 ID | List\<Reply\> | 查询帖子回复（时间正序） |
| `getReplyCount(postId)` | 帖子 ID | int | 查询帖子回复数 |

#### 收藏

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `addFavorite(userId, postId)` | 用户名, 帖子 ID | boolean | 添加收藏（INSERT OR IGNORE） |
| `removeFavorite(userId, postId)` | 用户名, 帖子 ID | boolean | 取消收藏 |
| `isFavorite(userId, postId)` | 用户名, 帖子 ID | boolean | 判断是否已收藏 |
| `getFavoritePosts(userId)` | 用户名 | List\<Post\> | 获取收藏帖子列表（JOIN 查询） |

#### 头像

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `updateAvatarPath(username, path)` | 用户名, 路径 | void | 保存头像路径 |
| `getAvatarPath(username)` | 用户名 | String | 获取头像路径 |

#### 通知

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `insertNotification(type, target, source, postId, content)` | 通知类型, 目标用户, 来源用户, 帖子 ID, 内容 | void | 创建通知 |
| `getUnreadNotificationCount(username)` | 用户名 | int | 获取未读通知数 |
| `getNotifications(username)` | 用户名 | List\<Object[]\> | 获取通知列表（时间倒序） |
| `markNotificationRead(id)` | 通知 ID | void | 标记单条已读 |
| `markAllNotificationsRead(username)` | 用户名 | void | 标记全部已读 |

#### 用户验证

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `validateUser(username, password)` | 用户名, 密码 | boolean | 验证用户名密码是否匹配 |

### 5.4 性能优化策略

1. **复合索引**: `idx_posts_type_time` 避免全表扫描，加速按类型+时间排序查询
2. **游标分页**: `getPostsByTypePaged()` 基于 ID 游标的分页，避免 `OFFSET` 性能退化
3. **计数冗余**: `reply_count` 字段维护在帖子表中，避免每次列表查询执行 `COUNT(*)`
4. **事务操作**: `insertReply()` 和 `deleteReply()` 使用事务保证数据一致性
5. **INSERT OR IGNORE**: 收藏操作使用 `INSERT OR IGNORE` 避免唯一约束冲突

---

## 6. 核心模块设计

### 6.1 认证系统

**组件**: `AuthManager` (`util/AuthManager.java`)

**存储机制**: SharedPreferences (`auth_prefs`)

| Key | 类型 | 说明 |
|-----|------|------|
| `logged_in` | boolean | 是否已登录 |
| `username` | String | 当前登录用户名 |

**API**:
- `login(username)` — 保存登录状态
- `logout()` — 清除登录状态
- `isLoggedIn()` — 检查登录状态
- `getUsername()` — 获取当前用户名

**登录流程**:
```
LoginActivity
  → 用户输入用户名和密码
  → DatabaseHelper.validateUser() 本地校验
  → 验证通过 → AuthManager.login(username)
  → 显示 Toast → 关闭登录页
```

**注册流程**:
```
RegisterActivity
  → 用户输入用户名、密码、确认密码
  → 前端校验: 字段非空 + 两次密码一致
  → INSERT OR IGNORE INTO users
  → 自动调用 AuthManager.login() 实现注册后自动登录
```

### 6.2 帖子系统

**帖子类型体系**:

| type 值 | 名称 | Fragment | 特点 |
|---------|------|----------|------|
| 0 | 公告 | `AnnounceFragment` | 正式公告，不支持匿名 |
| 1 | 动态 | `DynamicFragment` | 支持匿名发布和匿名回复 |
| 2 | 工具 | `ToolsFragment` | 支持实时搜索 |

**发帖流程** (`PublishPostDialog`):
```
点击 FAB → 检查登录状态 → 弹出 PublishPostDialog
  → 输入标题和内容
  → (动态页) 可选开启匿名开关
  → (可选) 从相册选择图片 → JPEG 85% 压缩 → 存储到 post_images/
  → 校验: 标题和内容不能为空
  → DatabaseHelper.insertPost() → 刷新列表
```

**帖子详情页** (`PostDetailActivity`):

采用 **单一 RecyclerView + 多 ViewType** 架构:

| ViewType | 常量 | 内容 |
|----------|------|------|
| 0 | `TYPE_HEADER` | 帖子信息头部 — 标题、内容、作者、时间、附图、编辑/删除/收藏按钮 |
| 1 | `TYPE_REPLY` | 回复条目 — 回复者信息、内容、时间、删除按钮 |

**帖子操作**:
- **编辑**: 仅作者本人可见，使用 AlertDialog + 自定义布局 (`dialog_edit_post.xml`)
- **删除**: 仅作者本人可见，二次确认弹窗，级联删除所有回复
- **回复**: 底部固定输入栏，支持 `autoFocusReply` 参数自动聚焦

### 6.3 回复系统

**发送回复**:
```
PostDetailActivity 底部输入框 → 输入内容 → sendReply()
  → 创建 Reply 对象（含匿名标记）
  → DatabaseHelper.insertReply() — 事务内:
      INSERT INTO replies
      UPDATE posts SET reply_count = reply_count + 1
  → 清空输入框 → 刷新回复列表 → 滚动到底部
  → (若回复他人帖子) DatabaseHelper.insertNotification()
```

**删除回复**:
```
点击删除按钮 → 二次确认
  → DatabaseHelper.deleteReply() — 事务内:
      查询 reply 所属 post_id
      DELETE FROM replies
      UPDATE posts SET reply_count = MAX(0, reply_count - 1)
```

### 6.4 收藏系统

**数据库约束**: `UNIQUE(user_id, post_id)` 防止重复收藏

**收藏操作** (`PostDetailActivity.toggleFavorite()`):
```
检查当前收藏状态 → DatabaseHelper.isFavorite()
  → 已收藏: DatabaseHelper.removeFavorite() → 刷新按钮状态
  → 未收藏: DatabaseHelper.addFavorite() → 刷新按钮状态
```

**收藏列表** (`FavoritesActivity`):
```sql
SELECT p.* FROM posts p
INNER JOIN favorites f ON p.id = f.post_id
WHERE f.user_id = ?
ORDER BY f.time DESC
```

### 6.5 通知系统

**触发条件**: 当用户回复他人帖子时自动创建通知

**通知数据**:
- `type = 0` — 回复通知
- `target_user` — 帖子作者（被通知者）
- `source_user` — 回复者（通知触发者）
- `content` — 回复内容前 30 字符

**通知功能**:
- 未读计数: `ProfileFragment` 中显示红点
- 标记已读: 点击通知自动标记
- 全部已读: 一键批量标记

---

## 7. UI 架构

### 7.1 布局文件 (19 个)

| 布局文件 | 用途 |
|----------|------|
| `activity_main.xml` | 主界面 — NavHostFragment + BottomNavigationView |
| `activity_login.xml` | 登录页 |
| `activity_register.xml` | 注册页 |
| `activity_post_detail.xml` | 帖子详情 — RecyclerView + 底部回复栏 |
| `activity_favorites.xml` | 收藏列表 |
| `activity_notifications.xml` | 通知列表 |
| `fragment_announce.xml` | 公告列表 |
| `fragment_dynamic.xml` | 动态列表 |
| `fragment_tools.xml` | 工具列表 |
| `fragment_profile.xml` | 个人中心 |
| `item_post.xml` | 帖子列表项 |
| `item_post_detail_header.xml` | 帖子详情头部 |
| `item_reply.xml` | 回复列表项 |
| `item_notification.xml` | 通知列表项 |
| `dialog_publish_post.xml` | 发帖弹窗 |
| `dialog_edit_post.xml` | 编辑帖子弹窗 |
| `dialog_reply.xml` | 回复弹窗 |
| `dialog_login.xml` | 登录弹窗 |
| `layout_bottom_nav.xml` | 胶囊形底部导航栏 |

### 7.2 Adapter 体系 (4 个)

| Adapter | 用途 | ViewType 数 |
|---------|------|------------|
| `PostAdapter` | 通用帖子列表项 | 1 |
| `PostDetailAdapter` | 帖子详情页多 ViewType | 2 (Header + Reply) |
| `ReplyAdapter` | 独立回复列表 | 1 |
| `NotificationAdapter` | 通知列表 | 1 |

### 7.3 自定义 Drawable 资源

| 资源 | 用途 |
|------|------|
| `bg_gradient_purple_pink.xml` | 全局紫粉渐变背景 (135度) |
| `bg_fab_glass.xml` | FAB 磨砂玻璃圆形按钮 |
| `bg_card_acrylic.xml` | 卡片亚克力半透明背景 |
| `bg_dialog_acrylic.xml` | 弹窗亚克力背景 |
| `bg_dialog_input.xml` | 输入框背景 |
| `bg_glass_button.xml` | 磨砂玻璃按钮 |
| `bg_nav_capsule.xml` | 胶囊形底部导航栏背景 |
| `bg_btn_publish_gradient.xml` | 发布按钮渐变背景 |
| `bg_image_preview.xml` | 图片预览容器背景 |
| `circle_bg.xml` | 圆形背景（头像等） |
| `circle_point.xml` | 未读红点 |
| `selector_bottom_nav_icon.xml` | 导航图标选中态 |
| `selector_bottom_nav_text.xml` | 导航文字选中态 |

### 7.4 动画资源 (8 个)

| 动画 | 用途 |
|------|------|
| `fab_scale_in.xml` | FAB 缩放进入 |
| `fade_in.xml` / `fade_out.xml` | 淡入淡出 |
| `slide_up.xml` / `slide_down.xml` | 弹窗上下滑入 |
| `slide_in_right.xml` / `slide_out_left.xml` | 页面右滑进入 |
| `list_item_animation.xml` | 列表项入场 |

### 7.5 页面过渡动画

| 场景 | 进入动画 | 退出动画 |
|------|----------|----------|
| 帖子详情、收藏、通知 | `slide_in_right` | `fade_out` |
| 弹窗 (发帖等) | `slide_up` | `slide_down` |
| 返回 | `fade_in` | `slide_down` |

---

## 8. 依赖库清单

### 8.1 生产依赖

```gradle
// AndroidX Core
implementation "androidx.core:core-ktx:1.12.0"
implementation "androidx.appcompat:appcompat:1.6.1"

// Material Design 3
implementation "com.google.android.material:material:1.9.0"

// Layout
implementation "androidx.constraintlayout:constraintlayout:2.1.4"

// Navigation Component
implementation "androidx.navigation:navigation-fragment:2.7.5"
implementation "androidx.navigation:navigation-ui:2.7.5"
```

### 8.2 测试依赖

```gradle
testImplementation "junit:junit:4.13.2"
androidTestImplementation "androidx.test.ext:junit:1.1.5"
androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
```

### 8.3 Kotlin 版本统一

```gradle
configurations.all {
    resolutionStrategy {
        force "org.jetbrains.kotlin:kotlin-stdlib:1.8.22"
        force "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.22"
        force "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22"
    }
}
```

### 8.4 依赖特点

- **极简依赖**: 仅依赖 AndroidX 核心库 + Material Design + Navigation
- **零第三方库**: 无网络库、图片加载库、异步库、依赖注入库、ORM 库
- **APK 体积小**: 无重量级框架，应用体积最小化

---

## 9. 数据库版本迁移历史

### v1 → v2

| 变更类型 | 内容 |
|----------|------|
| 新增表 | `replies` (回复表) |

### v2 → v3

| 变更类型 | 内容 |
|----------|------|
| 新增字段 | `posts.reply_count` — 帖子回复计数 |
| 新增字段 | `posts.image_path` — 帖子图片路径 |
| 新增字段 | `users.avatar_path` — 用户头像路径 |
| 新增表 | `favorites` (收藏表) |
| 新增表 | `notifications` (通知表) |
| 新增索引 | `idx_posts_type_time` — 帖子类型+时间复合索引 |
| 新增索引 | `idx_replies_post_id` — 回复帖子 ID 索引 |
| 新增索引 | `idx_favorites_user` — 收藏用户索引 |
| 新增索引 | `idx_notif_target` — 通知目标用户+已读状态复合索引 |
| 数据回填 | 更新现有帖子的 `reply_count` 字段（基于 replies 表 COUNT） |

---

## 10. 已知技术债务

### 10.1 安全性

| 问题 | 严重程度 | 说明 |
|------|----------|------|
| 密码明文存储 | 高 | `users.password` 字段以明文存储，应使用 BCrypt 等哈希算法 |
| 无 HTTPS 通信 | 中 | 当前无网络通信，若后续添加网络功能需注意 |

### 10.2 架构

| 问题 | 严重程度 | 说明 |
|------|----------|------|
| 无 ViewModel | 中 | Activity/Fragment 直接管理业务逻辑，不利于生命周期管理和测试 |
| 无 Repository 模式 | 中 | 数据访问层耦合在 UI 层中，不利于数据源切换和测试 |
| 数据库操作在主线程 | 中 | `SQLiteOpenHelper` 的读写操作均在主线程执行，可能造成 ANR |
| 无依赖注入 | 低 | 手动创建依赖，不利于测试和模块解耦 |

### 10.3 功能

| 问题 | 严重程度 | 说明 |
|------|----------|------|
| 无网络同步 | 中 | 数据仅本地存储，不支持跨设备同步和数据备份 |
| 无数据备份/恢复 | 中 | 用户卸载应用或更换设备后数据丢失 |
| 无推送通知 | 低 | 通知仅在应用内展示，无系统推送 |

### 10.4 代码质量

| 问题 | 严重程度 | 说明 |
|------|----------|------|
| 未使用的代码 | 低 | `LoginDialog` 和 `ReplyDialog` 已被替代但代码保留 |
| 通知返回类型 | 低 | `getNotifications()` 返回 `List<Object[]>` 而非类型安全的模型 |

---

*文档结束*
