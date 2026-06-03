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

    private static final String DATABASE_NAME = "campus_announcement.db";
    private static final int DATABASE_VERSION = 2;

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
                + COL_ANONYMOUS + " INTEGER DEFAULT 0"
                + ");";
        db.execSQL(CREATE_POST_TABLE);
        // 创建 users 表（仅用于本地登录/注册）
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USERNAME + " TEXT UNIQUE,"
                + COL_PASSWORD + " TEXT"
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
    }

    /** 插入新帖子 */
    public long insertPost(Post post) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO " + TABLE_POST + " (type, title, content, time, author, anonymous) VALUES (?,?,?,?,?,?)";
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

    /** 查询所有帖子，按时间倒序 */
    public List<Post> getAllPosts() {
        SQLiteDatabase db = getReadableDatabase();
        List<Post> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_POST + " ORDER BY " + COL_TIME + " DESC";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Post p = new Post();
                p.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                p.type = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TYPE));
                p.title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                p.content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT));
                p.time = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME));
                p.author = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR));
                p.anonymous = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANONYMOUS)) == 1;
                list.add(p);
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
            p = new Post();
            p.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            p.type = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TYPE));
            p.title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
            p.content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT));
            p.time = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME));
            p.author = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR));
            p.anonymous = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANONYMOUS)) == 1;
        }
        cursor.close();
        return p;
    }

    /** 插入回复 */
    public long insertReply(Reply reply) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_REPLY
                + " (" + COL_REPLY_POST_ID + "," + COL_REPLY_CONTENT + "," + COL_REPLY_TIME
                + "," + COL_REPLY_AUTHOR + "," + COL_REPLY_ANONYMOUS + ") VALUES (?,?,?,?,?)",
                new Object[]{reply.postId, reply.content, reply.time, reply.author, reply.anonymous ? 1 : 0});
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
        long id = -1;
        if (cursor.moveToFirst()) id = cursor.getLong(0);
        cursor.close();
        return id;
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
                Post p = new Post();
                p.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                p.type = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TYPE));
                p.title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                p.content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT));
                p.time = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME));
                p.author = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR));
                p.anonymous = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANONYMOUS)) == 1;
                list.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
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
                Post p = new Post();
                p.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                p.type = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TYPE));
                p.title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                p.content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT));
                p.time = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME));
                p.author = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR));
                p.anonymous = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANONYMOUS)) == 1;
                list.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
