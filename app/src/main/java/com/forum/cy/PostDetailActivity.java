package com.forum.cy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.forum.cy.adapter.PostDetailAdapter;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.model.Post;
import com.forum.cy.model.Reply;
import com.forum.cy.util.AuthManager;
import java.util.ArrayList;

/**
 * 帖子详情页，底部固定回复输入栏，键盘弹出时自动上移。
 * 使用单一 RecyclerView + 头部 ViewType 替代 ScrollView 嵌套方案。
 */
public class PostDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerDetail;
    private EditText etReplyInput;
    private TextView btnSendReply;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;
    private PostDetailAdapter detailAdapter;
    private long postId;
    private int postType;
    private String postAuthor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        recyclerDetail = findViewById(R.id.recycler_detail);
        etReplyInput = findViewById(R.id.et_reply_input);
        btnSendReply = findViewById(R.id.btn_send_reply);

        dbHelper = new DatabaseHelper(this);
        authManager = new AuthManager(this);

        // 设置统一适配器
        detailAdapter = new PostDetailAdapter();
        detailAdapter.setCurrentUsername(authManager.getUsername());
        detailAdapter.setOnDeletePostListener(this::showDeletePostDialog);
        detailAdapter.setOnEditPostListener(this::showEditPostDialog);
        detailAdapter.setOnDeleteReplyListener((reply, position) -> showDeleteReplyDialog(reply));
        detailAdapter.setOnFavoriteListener(this::toggleFavorite);
        recyclerDetail.setLayoutManager(new LinearLayoutManager(this));
        recyclerDetail.setAdapter(detailAdapter);

        postId = getIntent().getLongExtra("postId", -1);
        boolean autoFocus = getIntent().getBooleanExtra("autoFocusReply", false);

        if (postId != -1) {
            loadPost(postId);
            loadReplies();
            updateFavoriteState();
        }

        btnSendReply.setOnClickListener(v -> sendReply());

        // 从列表页点回复按钮进入时，自动聚焦输入框并弹出键盘
        if (autoFocus) {
            etReplyInput.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        // 进入动画
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down);
    }

    private void loadPost(long postId) {
        Post post = dbHelper.getPostById(postId);
        if (post != null) {
            postType = post.type;
            postAuthor = post.author;
            detailAdapter.setPost(post);
        }
    }

    private void loadReplies() {
        java.util.List<Reply> replies = dbHelper.getRepliesByPostId(postId);
        detailAdapter.updateReplies(replies);
    }

    private void showDeletePostDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这条帖子吗？删除后将无法恢复，该帖子下的所有回复也会被删除。")
                .setPositiveButton("删除", (dialog, which) -> deletePost())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deletePost() {
        boolean success = dbHelper.deletePost(postId);
        if (success) {
            Toast.makeText(this, "帖子已删除", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditPostDialog() {
        Post post = dbHelper.getPostById(postId);
        if (post == null) return;

        // 使用 EditPostDialog（将在后续任务中创建，此处先用简单 AlertDialog 实现）
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_post, null);
        EditText etTitle = dialogView.findViewById(R.id.et_edit_title);
        EditText etContent = dialogView.findViewById(R.id.et_edit_content);
        etTitle.setText(post.title);
        etContent.setText(post.content);

        new AlertDialog.Builder(this)
                .setTitle("编辑帖子")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newContent = etContent.getText().toString().trim();
                    if (newTitle.isEmpty() || newContent.isEmpty()) {
                        Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean success = dbHelper.updatePost(postId, newTitle, newContent);
                    if (success) {
                        Toast.makeText(this, "编辑成功", Toast.LENGTH_SHORT).show();
                        loadPost(postId);
                    } else {
                        Toast.makeText(this, "编辑失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteReplyDialog(Reply reply) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这条回复吗？删除后将无法恢复。")
                .setPositiveButton("删除", (dialog, which) -> deleteReply(reply))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteReply(Reply reply) {
        boolean success = dbHelper.deleteReply(reply.id);
        if (success) {
            Toast.makeText(this, "回复已删除", Toast.LENGTH_SHORT).show();
            loadReplies();
            loadPost(postId); // 刷新头部回复计数
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteState() {
        String userId = authManager.getUsername();
        if (userId != null) {
            boolean fav = dbHelper.isFavorite(userId, postId);
            detailAdapter.setFavoriteState(fav);
        }
    }

    private void toggleFavorite() {
        String userId = authManager.getUsername();
        if (userId == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean currentlyFav = dbHelper.isFavorite(userId, postId);
        if (currentlyFav) {
            dbHelper.removeFavorite(userId, postId);
            Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addFavorite(userId, postId);
            Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
        }
        detailAdapter.setFavoriteState(!currentlyFav);
    }

    private void sendReply() {
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = etReplyInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "回复内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String author = authManager.getUsername();
        if (author == null) author = "匿名";

        boolean anonymous = (postType == 1); // 动态页回复默认匿名
        Reply reply = new Reply(postId, content, System.currentTimeMillis(), author, anonymous);
        long id = dbHelper.insertReply(reply);
        if (id != -1) {
            etReplyInput.setText("");
            Toast.makeText(this, "回复成功", Toast.LENGTH_SHORT).show();
            loadReplies();
            loadPost(postId); // 刷新头部回复计数
            recyclerDetail.scrollToPosition(detailAdapter.getItemCount() - 1);

            // 如果回复的是别人的帖子，创建通知
            if (postAuthor != null && !postAuthor.equals(author)) {
                String preview = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                dbHelper.insertNotification(0, postAuthor, author, postId,
                        "回复了你的帖子: " + preview);
            }
        } else {
            Toast.makeText(this, "回复失败", Toast.LENGTH_SHORT).show();
        }
    }
}
