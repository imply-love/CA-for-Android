package com.forum.cy.model;

/**
 * 帖子实体类，存储在本地 SQLite 数据库中。
 * type: 0=公告，1=动态，2=工具
 */
public class Post {
    public long id;
    public int type; // 0: 公告, 1: 动态, 2: 工具
    public String title;
    public String content;
    public long time; // 时间戳（ms）
    public String author;
    public boolean anonymous; // 是否匿名发表，仅对动态页有效
    public int replyCount; // 回复计数（由数据库维护）
    public String imagePath; // 帖子附图本地路径

    public Post() {}

    public Post(int type, String title, String content, long time, String author, boolean anonymous) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.time = time;
        this.author = author;
        this.anonymous = anonymous;
    }
}
