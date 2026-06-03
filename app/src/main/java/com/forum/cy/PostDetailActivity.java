package com.forum.cy;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.forum.cy.adapter.ReplyAdapter;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.model.Post;
import com.forum.cy.model.Reply;
import com.forum.cy.util.AuthManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 帖子详情页，底部固定回复输入栏，键盘弹出时自动上移。
 */
public class PostDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvContent, tvAuthor, tvTime, tvReplyHeader;
    private RecyclerView recyclerReplies;
    private EditText etReplyInput;
    private TextView btnSendReply;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;
    private ReplyAdapter replyAdapter;
    private long postId;
    private int postType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        tvTitle = findViewById(R.id.tv_detail_title);
        tvContent = findViewById(R.id.tv_detail_content);
        tvAuthor = findViewById(R.id.tv_detail_author);
        tvTime = findViewById(R.id.tv_detail_time);
        tvReplyHeader = findViewById(R.id.tv_reply_header);
        recyclerReplies = findViewById(R.id.recycler_replies);
        etReplyInput = findViewById(R.id.et_reply_input);
        btnSendReply = findViewById(R.id.btn_send_reply);

        dbHelper = new DatabaseHelper(this);
        authManager = new AuthManager(this);

        recyclerReplies.setLayoutManager(new LinearLayoutManager(this));
        replyAdapter = new ReplyAdapter(new ArrayList<>());
        recyclerReplies.setAdapter(replyAdapter);

        postId = getIntent().getLongExtra("postId", -1);
        boolean autoFocus = getIntent().getBooleanExtra("autoFocusReply", false);

        if (postId != -1) {
            loadPost(postId);
            loadReplies();
        }

        btnSendReply.setOnClickListener(v -> sendReply());

        // 从列表页点回复按钮进入时，自动聚焦输入框并弹出键盘
        if (autoFocus) {
            etReplyInput.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    private void loadPost(long postId) {
        Post post = dbHelper.getPostById(postId);
        if (post != null) {
            postType = post.type;
            tvTitle.setText(post.title);
            tvContent.setText(post.content);
            tvAuthor.setText(post.author);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            tvTime.setText(sdf.format(new Date(post.time)));
        }
    }

    private void loadReplies() {
        java.util.List<Reply> replies = dbHelper.getRepliesByPostId(postId);
        replyAdapter.updateData(replies);
        tvReplyHeader.setText("回复 (" + replies.size() + ")");
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
            recyclerReplies.scrollToPosition(replyAdapter.getItemCount() - 1);
        } else {
            Toast.makeText(this, "回复失败", Toast.LENGTH_SHORT).show();
        }
    }
}
