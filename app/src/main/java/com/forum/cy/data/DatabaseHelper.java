package com.forum.cy.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.forum.cy.model.Post;
import com.forum.cy.model.Reply;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地 SQLite 数据库帮助类，负责帖子及用户的增删改查。
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE_USER = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password"; // 简单存明文或可哈希
    private static final String COL_AVATAR_PATH = "avatar_path"; // 头像本地路径

    private static final String DATABASE_NAME = "campus_announcement.db";
    private static final int DATABASE_VERSION = 3;

    // 表结构字段定义
    private static final String TABLE_POST = "posts";
    private static final String COL_ID = "id";
    private static final String COL_TYPE = "type"; // 0=公告,1=动态,2=工具
    private static final String COL_TITLE = "title";
    private static final String COL_CONTENT = "content";
    private static final String COL_TIME = "time"; // 毫秒时间戳
    private static final String COL_AUTHOR = "author";
    private static final String COL_ANONYMOUS = "anonymous"; // INTEGER 0/1

    // 回复表
    private static final String TABLE_REPLY = "replies";
    private static final String COL_REPLY_ID = "id";
    private static final String COL_REPLY_POST_ID = "post_id";
    private static final String COL_REPLY_CONTENT = "content";
    private static final String COL_REPLY_TIME = "time";
    private static final String COL_REPLY_AUTHOR = "author";
    private static final String COL_REPLY_ANONYMOUS = "anonymous";

    // 帖子表新增字段（v3）
    private static final String COL_REPLY_COUNT = "reply_count";
    private static final String COL_IMAGE_PATH = "image_path";

    // 收藏表
    private static final String TABLE_FAVORITE = "favorites";
    private static final String COL_FAV_ID = "id";
    private static final String COL_FAV_USER = "user_id";
    private static final String COL_FAV_POST_ID = "post_id";
    private static final String COL_FAV_TIME = "time";

    // 通知表
    private static final String TABLE_NOTIFICATION = "notifications";
    private static final String COL_NOTIF_ID = "id";
    private static final String COL_NOTIF_TYPE = "type"; // 0=回复通知
    private static final String COL_NOTIF_TARGET_USER = "target_user";
    private static final String COL_NOTIF_SOURCE_USER = "source_user";
    private static final String COL_NOTIF_POST_ID = "post_id";
    private static final String COL_NOTIF_CONTENT = "content";
    private static final String COL_NOTIF_IS_READ = "is_read";
    private static final String COL_NOTIF_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建 posts 表
        String CREATE_POST_TABLE = "CREATE TABLE " + TABLE_POST + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TYPE + " INTEGER,"
                + COL_TITLE + " TEXT,"
                + COL_CONTENT + " TEXT,"
                + COL_TIME + " INTEGER,"
                + COL_AUTHOR + " TEXT,"
                + COL_ANONYMOUS + " INTEGER DEFAULT 0,"
                + COL_REPLY_COUNT + " INTEGER DEFAULT 0,"
                + COL_IMAGE_PATH + " TEXT"
                + ");";
        db.execSQL(CREATE_POST_TABLE);
        // 创建 users 表（仅用于本地登录/注册）
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USERNAME + " TEXT UNIQUE,"
                + COL_PASSWORD + " TEXT,"
                + COL_AVATAR_PATH + " TEXT"
                + ");";
        db.execSQL(CREATE_USER_TABLE);
        // 创建 replies 表
        db.execSQL("CREATE TABLE " + TABLE_REPLY + " ("
                + COL_REPLY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_REPLY_POST_ID + " INTEGER,"
                + COL_REPLY_CONTENT + " TEXT,"
                + COL_REPLY_TIME + " INTEGER,"
                + COL_REPLY_AUTHOR + " TEXT,"
                + COL_REPLY_ANONYMOUS + " INTEGER DEFAULT 0"
                + ");");

        // 创建索引以优化查询性能
        db.execSQL("CREATE INDEX idx_posts_type_time ON " + TABLE_POST + " (" + COL_TYPE + ", " + COL_TIME + " DESC);");
        db.execSQL("CREATE INDEX idx_replies_post_id ON " + TABLE_REPLY + " (" + COL_REPLY_POST_ID + ");");

        // 创建收藏表
        db.execSQL("CREATE TABLE " + TABLE_FAVORITE + " ("
                + COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_FAV_USER + " TEXT NOT NULL,"
                + COL_FAV_POST_ID + " INTEGER NOT NULL,"
                + COL_FAV_TIME + " INTEGER,"
                + "UNIQUE(" + COL_FAV_USER + ", " + COL_FAV_POST_ID + ")"
                + ");");
        db.execSQL("CREATE INDEX idx_favorites_user ON " + TABLE_FAVORITE + " (" + COL_FAV_USER + ");");

        // 创建通知表
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATION + " ("
                + COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NOTIF_TYPE + " INTEGER,"
                + COL_NOTIF_TARGET_USER + " TEXT NOT NULL,"
                + COL_NOTIF_SOURCE_USER + " TEXT,"
                + COL_NOTIF_POST_ID + " INTEGER,"
                + COL_NOTIF_CONTENT + " TEXT,"
                + COL_NOTIF_IS_READ + " INTEGER DEFAULT 0,"
                + COL_NOTIF_TIME + " INTEGER"
                + ");");
        db.execSQL("CREATE INDEX idx_notif_target ON " + TABLE_NOTIFICATION + " (" + COL_NOTIF_TARGET_USER + ", " + COL_NOTIF_IS_READ + ");");

        // 初始化示例数据（可选）
        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // 插入几条公告、动态示例，供演示使用
        long now = System.currentTimeMillis();
        db.execSQL("INSERT INTO " + TABLE_POST + " (type, title, content, time, author, anonymous) VALUES (0,'欢迎使用','这是公告示例'," + now + ", '系统',0);");
        db.execSQL("INSERT INTO " + TABLE_POST + " (type, title, content, time, author, anonymous) VALUES (1,'校园活动','今天有学生社团展览'," + (now - 3600_000) + ", '张三',0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // v1 → v2: 新增 replies 表，保留已有数据
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REPLY + " ("
                    + COL_REPLY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_REPLY_POST_ID + " INTEGER,"
                    + COL_REPLY_CONTENT + " TEXT,"
                    + COL_REPLY_TIME + " INTEGER,"
                    + COL_REPLY_AUTHOR + " TEXT,"
                    + COL_REPLY_ANONYMOUS + " INTEGER DEFAULT 0"
                    + ");");
        }
        if (oldVersion < 3) {
            // v2 → v3: 新增 reply_count 字段 + 索引 + 同步回复计数
            db.execSQL("ALTER TABLE " + TABLE_POST + " ADD COLUMN " + COL_REPLY_COUNT + " INTEGER DEFAULT 0");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_posts_type_time ON " + TABLE_POST + " (" + COL_TYPE + ", " + COL_TIME + " DESC);");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_replies_post_id ON " + TABLE_REPLY + " (" + COL_REPLY_POST_ID + ");");
            // 回填现有帖子的回复计数
            db.execSQL("UPDATE " + TABLE_POST + " SET " + COL_REPLY_COUNT + " = "
                    + "(SELECT COUNT(*) FROM " + TABLE_REPLY + " WHERE " + TABLE_REPLY + "." + COL_REPLY_POST_ID + " = " + TABLE_POST + "." + COL_ID + ");");
            // 创建收藏表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITE + " ("
                    + COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_FAV_USER + " TEXT NOT NULL,"
                    + COL_FAV_POST_ID + " INTEGER NOT NULL,"
                    + COL_FAV_TIME + " INTEGER,"
                    + "UNIQUE(" + COL_FAV_USER + ", " + COL_FAV_POST_ID + ")"
                    + ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_favorites_user ON " + TABLE_FAVORITE + " (" + COL_FAV_USER + ");");
            // 新增头像路径字段
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COL_AVATAR_PATH + " TEXT");
            // 新增帖子图片路径字段
            db.execSQL("ALTER TABLE " + TABLE_POST + " ADD COLUMN " + COL_IMAGE_PATH + " TEXT");
            // 创建通知表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATION + " ("
                    + COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_NOTIF_TYPE + " INTEGER,"
                    + COL_NOTIF_TARGET_USER + " TEXT NOT NULL,"
                    + COL_NOTIF_SOURCE_USER + " TEXT,"
                    + COL_NOTIF_POST_ID + " INTEGER,"
                    + COL_NOTIF_CONTENT + " TEXT,"
                    + COL_NOTIF_IS_READ + " INTEGER DEFAULT 0,"
                    + COL_NOTIF_TIME + " INTEGER"
                    + ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_notif_target ON " + TABLE_NOTIFICATION
                    + " (" + COL_NOTIF_TARGET_USER + ", " + COL_NOTIF_IS_READ + ");");
        }
    }

    /** 插入新帖子 */
    public long insertPost(Post post) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO " + TABLE_POST + " (type, title, content, time, author, anonymous, reply_count) VALUES (?,?,?,?,?,?,0)";
        db.execSQL(sql, new Object[]{post.type, post.title, post.content, post.time, post.author, post.anonymous ? 1 : 0});
        // 获取刚插入的 id
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        return id;
    }

    /** 从 Cursor 当前行解析 Post 对象 */
    private Post cursorToPost(Cursor cursor) {
        Post p = new Post();
        p.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
        p.type = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TYPE));
        p.title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        p.content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT));
        p.time = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME));
        p.author = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR));
        p.anonymous = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANONYMOUS)) == 1;
        p.replyCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPLY_COUNT));
        int imgIdx = cursor.getColumnIndex(COL_IMAGE_PATH);
        p.imagePath = imgIdx >= 0 ? cursor.getString(imgIdx) : null;
        return p;
    }

    /** 更新帖子标题和内容 */
    public boolean updatePost(long postId, String newTitle, String newContent) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_POST + " SET " + COL_TITLE + "=?, " + COL_CONTENT + "=? WHERE " + COL_ID + "=?",
                new Object[]{newTitle, newContent, postId});
        // 检查是否实际更新了行
        Cursor cursor = db.rawQuery("SELECT changes()", null);
        int changes = 0;
        if (cursor.moveToFirst()) changes = cursor.getInt(0);
        cursor.close();
        return changes > 0;
    }

    /** 更新帖子图片路径 */
    public void updatePostImage(long postId, String imagePath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_POST + " SET " + COL_IMAGE_PATH + "=? WHERE " + COL_ID + "=?",
                new Object[]{imagePath, postId});
    }

    /** 查询所有帖子，按时间倒序 */
    public List<Post> getAllPosts() {
        SQLiteDatabase db = getReadableDatabase();
        List<Post> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_POST + " ORDER BY " + COL_TIME + " DESC";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPost(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /** 验证用户名和密码是否匹配 */
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + TABLE_USER + " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{username, password});
        boolean ok = false;
        if (cursor.moveToFirst()) {
            ok = cursor.getInt(0) > 0;
        }
        cursor.close();
        return ok;
    }

    public Post getPostById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_POST + " WHERE " + COL_ID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
        Post p = null;
        if (cursor.moveToFirst()) {
            p = cursorToPost(cursor);
        }
        cursor.close();
        return p;
    }

    /** 插入回复，并同步更新帖子回复计数 */
    public long insertReply(Reply reply) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO " + TABLE_REPLY
                    + " (" + COL_REPLY_POST_ID + "," + COL_REPLY_CONTENT + "," + COL_REPLY_TIME
                    + "," + COL_REPLY_AUTHOR + "," + COL_REPLY_ANONYMOUS + ") VALUES (?,?,?,?,?)",
                    new Object[]{reply.postId, reply.content, reply.time, reply.author, reply.anonymous ? 1 : 0});
            // 同步递增帖子回复计数
            db.execSQL("UPDATE " + TABLE_POST + " SET " + COL_REPLY_COUNT + " = " + COL_REPLY_COUNT + " + 1 WHERE " + COL_ID + " = ?",
                    new Object[]{reply.postId});
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            long id = -1;
            if (cursor.moveToFirst()) id = cursor.getLong(0);
            cursor.close();
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    /** 查询某帖子的所有回复，按时间正序 */
    public List<Reply> getRepliesByPostId(long postId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Reply> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REPLY
                + " WHERE " + COL_REPLY_POST_ID + "=? ORDER BY " + COL_REPLY_TIME + " ASC",
                new String[]{String.valueOf(postId)});
        if (cursor.moveToFirst()) {
            do {
                Reply r = new Reply();
                r.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REPLY_ID));
                r.postId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REPLY_POST_ID));
                r.content = cursor.getString(cursor.getColumnIndexOrThrow(COL_REPLY_CONTENT));
                r.time = cursor.getLong(cursor.getColumnIndexOrThrow(COL_REPLY_TIME));
                r.author = cursor.getString(cursor.getColumnIndexOrThrow(COL_REPLY_AUTHOR));
                r.anonymous = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPLY_ANONYMOUS)) == 1;
                list.add(r);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /** 查询某帖子的回复数量 */
    public int getReplyCount(long postId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REPLY
                + " WHERE " + COL_REPLY_POST_ID + "=?",
                new String[]{String.valueOf(postId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /** 按类型和关键词模糊搜索帖子（标题或内容） */
    public List<Post> searchPosts(int type, String keyword) {
        SQLiteDatabase db = getReadableDatabase();
        List<Post> list = new ArrayList<>();
        String pattern = "%" + keyword + "%";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POST
                + " WHERE " + COL_TYPE + "=? AND (" + COL_TITLE + " LIKE ? OR " + COL_CONTENT + " LIKE ?)"
                + " ORDER BY " + COL_TIME + " DESC",
                new String[]{String.valueOf(type), pattern, pattern});
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPost(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /** 删除帖子（同时删除该帖子的所有回复） */
    public boolean deletePost(long postId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 先删除该帖子的所有回复
            db.delete(TABLE_REPLY, COL_REPLY_POST_ID + "=?", new String[]{String.valueOf(postId)});
            // 再删除帖子本身
            int result = db.delete(TABLE_POST, COL_ID + "=?", new String[]{String.valueOf(postId)});
            db.setTransactionSuccessful();
            return result > 0;
        } finally {
            db.endTransaction();
        }
    }

    /** 删除单条回复，并同步更新帖子回复计数 */
    public boolean deleteReply(long replyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 先查询回复所属帖子 ID
            Cursor cursor = db.rawQuery("SELECT " + COL_REPLY_POST_ID + " FROM " + TABLE_REPLY + " WHERE " + COL_REPLY_ID + "=?",
                    new String[]{String.valueOf(replyId)});
            long postId = -1;
            if (cursor.moveToFirst()) postId = cursor.getLong(0);
            cursor.close();
            int result = db.delete(TABLE_REPLY, COL_REPLY_ID + "=?", new String[]{String.valueOf(replyId)});
            if (result > 0 && postId != -1) {
                // 同步递减帖子回复计数
                db.execSQL("UPDATE " + TABLE_POST + " SET " + COL_REPLY_COUNT + " = MAX(0, " + COL_REPLY_COUNT + " - 1) WHERE " + COL_ID + " = ?",
                        new Object[]{postId});
            }
            db.setTransactionSuccessful();
            return result > 0;
        } finally {
            db.endTransaction();
        }
    }

    /** 按类型查询帖子 */
    public List<Post> getPostsByType(int type) {
        SQLiteDatabase db = getReadableDatabase();
        List<Post> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POST
                + " WHERE " + COL_TYPE + "=? ORDER BY " + COL_TIME + " DESC",
                new String[]{String.valueOf(type)});
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPost(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /** 按类型分页查询帖子（基于 ID 游标分页） */
    public List<Post> getPostsByTypePaged(int type, long lastPostId, int pageSize) {
        SQLiteDatabase db = getReadableDatabase();
        List<Post> list = new ArrayList<>();
        String sql;
        String[] args;
        if (lastPostId <= 0) {
            // 首页
            sql = "SELECT * FROM " + TABLE_POST + " WHERE " + COL_TYPE + "=? ORDER BY " + COL_TIME + " DESC, " + COL_ID + " DESC LIMIT ?";
            args = new String[]{String.valueOf(type), String.valueOf(pageSize)};
        } else {
            // 基于最后一条帖子的 ID 进行游标分页
            sql = "SELECT * FROM " + TABLE_POST + " WHERE " + COL_TYPE + "=? AND " + COL_ID + "<? ORDER BY " + COL_TIME + " DESC, " + COL_ID + " DESC LIMIT ?";
            args = new String[]{String.valueOf(type), String.valueOf(lastPostId), String.valueOf(pageSize)};
        }
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPost(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // ==================== 收藏相关方法 ====================

    /** 添加收藏 */
    public boolean addFavorite(String userId, long postId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_FAVORITE
                    + " (" + COL_FAV_USER + ", " + COL_FAV_POST_ID + ", " + COL_FAV_TIME + ") VALUES (?,?,?)",
                    new Object[]{userId, postId, System.currentTimeMillis()});
            // 检查是否实际插入了行
            Cursor cursor = db.rawQuery("SELECT changes()", null);
            int changes = 0;
            if (cursor.moveToFirst()) changes = cursor.getInt(0);
            cursor.close();
            return changes > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** 取消收藏 */
    public boolean removeFavorite(String userId, long postId) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_FAVORITE,
                COL_FAV_USER + "=? AND " + COL_FAV_POST_ID + "=?",
                new String[]{userId, String.valueOf(postId)});
        return result > 0;
    }

    /** 判断是否已收藏 */
    public boolean isFavorite(String userId, long postId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FAVORITE
                        + " WHERE " + COL_FAV_USER + "=? AND " + COL_FAV_POST_ID + "=?",
                new String[]{userId, String.valueOf(postId)});
        boolean fav = false;
        if (cursor.moveToFirst()) fav = cursor.getInt(0) > 0;
        cursor.close();
        return fav;
    }

    /** 获取用户收藏的帖子列表（关联 posts 表） */
    public List<Post> getFavoritePosts(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Post> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT p.* FROM " + TABLE_POST + " p"
                        + " INNER JOIN " + TABLE_FAVORITE + " f ON p." + COL_ID + " = f." + COL_FAV_POST_ID
                        + " WHERE f." + COL_FAV_USER + "=?"
                        + " ORDER BY f." + COL_FAV_TIME + " DESC",
                new String[]{userId});
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPost(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // ==================== 头像相关方法 ====================

    /** 保存用户头像路径 */
    public void updateAvatarPath(String username, String avatarPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_USER + " SET " + COL_AVATAR_PATH + "=? WHERE " + COL_USERNAME + "=?",
                new Object[]{avatarPath, username});
    }

    /** 获取用户头像路径 */
    public String getAvatarPath(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_AVATAR_PATH + " FROM " + TABLE_USER + " WHERE " + COL_USERNAME + "=?",
                new String[]{username});
        String path = null;
        if (cursor.moveToFirst()) {
            path = cursor.getString(0);
        }
        cursor.close();
        return path;
    }

    // ==================== 通知相关方法 ====================

    /** 创建通知 */
    public void insertNotification(int type, String targetUser, String sourceUser, long postId, String content) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_NOTIFICATION
                        + " (" + COL_NOTIF_TYPE + ", " + COL_NOTIF_TARGET_USER + ", " + COL_NOTIF_SOURCE_USER
                        + ", " + COL_NOTIF_POST_ID + ", " + COL_NOTIF_CONTENT + ", " + COL_NOTIF_IS_READ + ", " + COL_NOTIF_TIME
                        + ") VALUES (?,?,?,?,?,0,?)",
                new Object[]{type, targetUser, sourceUser, postId, content, System.currentTimeMillis()});
    }

    /** 获取用户未读通知数量 */
    public int getUnreadNotificationCount(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOTIFICATION
                        + " WHERE " + COL_NOTIF_TARGET_USER + "=? AND " + COL_NOTIF_IS_READ + "=0",
                new String[]{username});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /** 获取用户所有通知（简单数组形式，返回 Object[] 列表） */
    public List<Object[]> getNotifications(String username) {
        SQLiteDatabase db = getReadableDatabase();
        List<Object[]> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATION
                        + " WHERE " + COL_NOTIF_TARGET_USER + "=? ORDER BY " + COL_NOTIF_TIME + " DESC",
                new String[]{username});
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_NOTIF_ID));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIF_TYPE));
                String source = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIF_SOURCE_USER));
                long postId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_NOTIF_POST_ID));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIF_CONTENT));
                boolean isRead = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIF_IS_READ)) == 1;
                long time = cursor.getLong(cursor.getColumnIndexOrThrow(COL_NOTIF_TIME));
                list.add(new Object[]{id, type, source, postId, content, isRead, time});
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /** 标记通知为已读 */
    public void markNotificationRead(long notificationId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NOTIFICATION + " SET " + COL_NOTIF_IS_READ + "=1 WHERE " + COL_NOTIF_ID + "=?",
                new Object[]{notificationId});
    }

    /** 标记用户所有通知为已读 */
    public void markAllNotificationsRead(String username) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NOTIFICATION + " SET " + COL_NOTIF_IS_READ + "=1 WHERE " + COL_NOTIF_TARGET_USER + "=?",
                new Object[]{username});
    }
}
