# 畅言论坛待办事项实施计划

基于 README.md 中的 8 项待办事项，结合现有代码架构，制定以下分阶段实施计划。

---

## 阶段一：基础功能完善（低复杂度、高价值）

### 1. 添加帖子编辑和删除功能
**现状：** 删除功能已在 `PostDetailActivity` 中实现（仅作者可见），但编辑功能缺失。
**涉及文件：**
- `DatabaseHelper.java` — 新增 `updatePost()` 方法
- `PostDetailActivity.java` — 添加编辑按钮和编辑逻辑
- 新建 `EditPostDialog.java` — 编辑帖子弹窗（复用 `PublishPostDialog` 的样式）
- `activity_post_detail.xml` — 编辑按钮布局

**实施步骤：**
1. 在 `DatabaseHelper` 中新增 `updatePost(long id, String title, String content)` 方法
2. 创建 `EditPostDialog`，预填充现有标题和内容
3. 在 `PostDetailActivity` 的帖子信息卡片中添加编辑按钮（仅作者可见）
4. 编辑完成后刷新帖子详情页面

---

### 2. 添加帖子收藏功能
**现状：** 无收藏相关功能。
**涉及文件：**
- `DatabaseHelper.java` — 新增 `favorites` 表和相关 CRUD 方法
- `PostAdapter.java` — 帖子列表项添加收藏图标
- `PostDetailActivity.java` — 帖子详情页添加收藏按钮
- `post_item.xml` — 收藏图标布局
- `activity_post_detail.xml` — 收藏按钮布局

**数据库变更（版本 3）：**
```sql
CREATE TABLE favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    post_id INTEGER NOT NULL,
    time INTEGER,
    UNIQUE(user_id, post_id)
);
```

**实施步骤：**
1. `DatabaseHelper` 升级数据库到版本 3，新增 `favorites` 表
2. 实现 `addFavorite()`, `removeFavorite()`, `isFavorite()`, `getFavoritesByUser()` 方法
3. 在 `PostAdapter` 和 `PostDetailActivity` 中添加收藏/取消收藏交互
4. 在 `ProfileFragment` 中添加"我的收藏"入口，展示收藏列表

---

## 阶段二：用户体验提升（中等复杂度）

### 3. 实现用户头像上传
**现状：** `ProfileFragment` 中有头像占位符，但无法上传自定义头像。
**涉及文件：**
- `ProfileFragment.java` — 头像选择和显示逻辑
- `DatabaseHelper.java` — `users` 表新增 `avatar_path` 字段
- `AuthManager.java` — 持久化头像路径
- `fragment_profile.xml` — 头像点击交互
- `PostAdapter.java` / `ReplyAdapter.java` — 显示用户头像

**实施步骤：**
1. 使用 `ActivityResultContracts.GetContent` 或 `TakePicture` 获取图片
2. 将图片复制到应用私有目录 (`getExternalFilesDir("avatars")`)
3. `users` 表新增 `avatar_path` 字段（数据库版本 3 或 4）
4. 在帖子列表和回复列表中显示用户头像
5. 添加默认头像 drawable 资源

---

### 4. 添加图片上传功能
**现状：** 帖子仅支持纯文本，不支持图片。
**涉及文件：**
- `PublishPostDialog.java` / `EditPostDialog.java` — 添加图片选择
- `PostDetailActivity.java` — 显示帖子图片
- `DatabaseHelper.java` — `posts` 表新增 `image_path` 字段
- `PostAdapter.java` — 帖子列表显示缩略图
- `activity_post_detail.xml` / `post_item.xml` — 图片展示布局

**数据库变更：**
- `posts` 表新增 `image_path TEXT` 字段

**实施步骤：**
1. 使用 `ActivityResultContracts.GetContent("image/*")` 选择图片
2. 图片压缩后保存到应用私有目录 (`getExternalFilesDir("images")`)
3. 帖子详情页使用 `ImageView` 展示图片
4. 帖子列表中显示缩略图预览
5. 复用头像上传的图片处理工具类

---

### 5. 优化 UI 动画效果
**现状：** 页面切换和交互缺少动画。
**涉及文件：**
- `anim/` 目录 — 新建动画资源文件
- `PostDetailActivity.java` — 页面转场动画
- 各 `Fragment` — 列表加载动画
- `PostAdapter.java` — 条目动画

**实施步骤：**
1. 添加 Activity 转场动画（`overridePendingTransition` 或 `ActivityOptions`）
2. `RecyclerView` 添加 `ItemAnimator`（默认支持，可自定义）
3. FAB 添加缩放进入动画
4. 页面元素添加渐入动画（`fade_in.xml`, `slide_up.xml`）
5. 底部导航切换添加 Fragment 过渡动画

---

## 阶段三：系统功能扩展（较高复杂度）

### 6. 添加消息通知系统
**现状：** 无通知功能，用户无法得知回复或系统消息。
**涉及文件：**
- `DatabaseHelper.java` — 新建 `notifications` 表
- 新建 `NotificationFragment.java` — 通知列表页面
- `PostDetailActivity.java` — 发送回复时创建通知
- `nav_graph.xml` — 添加通知页面导航
- `MainActivity.java` — 通知角标

**数据库变更（版本 4 或 5）：**
```sql
CREATE TABLE notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type INTEGER,         -- 0=回复通知, 1=系统通知
    target_user TEXT,     -- 接收者
    source_user TEXT,     -- 发送者
    post_id INTEGER,      -- 关联帖子
    content TEXT,         -- 通知内容
    is_read INTEGER DEFAULT 0,
    time INTEGER
);
```

**实施步骤：**
1. 新建 `notifications` 表
2. 在回复发送时自动创建通知记录
3. 创建 `NotificationFragment` 展示通知列表
4. 在底部导航或顶部添加通知入口和未读角标
5. 点击通知跳转到对应帖子详情

---

### 7. 实现夜间模式
**现状：** 仅支持浅色模式，但已使用 Material3 DayNight 主题。
**涉及文件：**
- `values-night/` — 新建夜间模式资源
- `values/colors.xml` — 定义可主题化的颜色
- `ProfileFragment.java` — 添加夜间模式切换开关
- `fragment_profile.xml` — 切换按钮布局
- 所有布局 XML — 使用 `?attr/colorXxx` 替代硬编码颜色

**实施步骤：**
1. 创建 `values-night/colors.xml`，定义夜间配色方案
2. 创建 `values-night/themes.xml` 覆盖夜间主题
3. 在布局中将硬编码颜色替换为主题属性引用
4. 在 `ProfileFragment` 中添加模式切换开关
5. 使用 `AppCompatDelegate.setDefaultNightMode()` 持久化模式选择
6. 渐变背景和毛玻璃效果适配夜间模式

---

## 阶段四：性能优化（贯穿全程）

### 8. 优化数据库查询性能
**现状：** 存在多个性能隐患：ScrollView+RecyclerView 反模式、无索引、全表扫描。
**涉及文件：**
- `DatabaseHelper.java` — 添加索引、优化查询
- `PostDetailActivity.java` — 移除 ScrollView，使用纯 RecyclerView
- 各 Fragment — 引入分页加载
- `activity_post_detail.xml` — 重构布局

**实施步骤：**
1. 为 `posts.type`, `posts.time`, `replies.post_id` 添加数据库索引
2. 重构 `PostDetailActivity`：移除 ScrollView，RecyclerView 使用 `HeaderView` 模式
3. 实现分页加载（`LIMIT/OFFSET` 或基于 ID 的游标分页）
4. 优化回复计数查询：在 `posts` 表新增 `reply_count` 冗余字段，维护一致性
5. 考虑引入 `CursorLoader` 或简单的异步查询避免主线程数据库操作

---

## 实施优先级建议

| 优先级 | 任务 | 理由 |
|--------|------|------|
| P0 | 数据库查询优化 (#8) | 基础性能问题，影响所有功能 |
| P1 | 帖子编辑删除 (#1) | 删除已实现，编辑是基本需求 |
| P1 | 帖子收藏 (#2) | 用户高频需求，实现简单 |
| P2 | 头像上传 (#3) | 提升用户身份感 |
| P2 | 夜间模式 (#6) | 用户体验提升，Material3 已支持 |
| P2 | UI 动画 (#5) | 视觉体验提升 |
| P3 | 图片上传 (#4) | 功能丰富度提升 |
| P3 | 消息通知 (#6) | 复杂度最高，需要新增页面和表 |

---

## 注意事项

1. **数据库版本管理：** 每次 schema 变更都需要升级版本号并在 `onUpgrade()` 中处理迁移
2. **代码复用：** 三个列表 Fragment 代码高度重复，建议在实施过程中抽取 `BasePostListFragment`
3. **发布功能缺失：** 当前 `PublishPostDialog` 未被任何地方调用，FAB 按钮未绑定点击事件，建议一并修复
4. **已废弃代码清理：** `LoginDialog` 和 `ReplyDialog` 未被使用，可考虑清理或重新启用
