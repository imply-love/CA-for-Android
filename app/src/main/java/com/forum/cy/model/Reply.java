package com.forum.cy.model;

/**
 * 回复实体类，关联到某个帖子（postId）。
 */
public class Reply {
    public long id;
    public long postId;    // 关联的帖子 ID
    public String content; // 回复内容
    public long time;      // 时间戳（ms）
    public String author;  // 回复者用户名
    public boolean anonymous; // 是否匿名（仅动态页有效）

    public Reply() {}

    public Reply(long postId, String content, long time, String author, boolean anonymous) {
        this.postId = postId;
        this.content = content;
        this.time = time;
        this.author = author;
        this.anonymous = anonymous;
    }
}
