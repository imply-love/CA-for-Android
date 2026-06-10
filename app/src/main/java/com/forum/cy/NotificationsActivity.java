package com.forum.cy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.forum.cy.adapter.NotificationAdapter;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.util.AuthManager;
import java.util.List;

/**
 * 消息通知页面，展示当前用户的通知列表。
 */
public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private NotificationAdapter adapter;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.recycler_notifications);
        tvEmpty = findViewById(R.id.tv_empty);
        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnReadAll = findViewById(R.id.btn_read_all);

        dbHelper = new DatabaseHelper(this);
        authManager = new AuthManager(this);

        btnBack.setOnClickListener(v -> finish());

        btnReadAll.setOnClickListener(v -> {
            String userId = authManager.getUsername();
            if (userId != null) {
                dbHelper.markAllNotificationsRead(userId);
                loadNotifications();
                Toast.makeText(this, "已全部标记为已读", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        adapter.setOnItemClickListener((postId, notificationId) -> {
            // 标记为已读
            dbHelper.markNotificationRead(notificationId);
            // 跳转到帖子详情
            if (postId > 0) {
                Intent intent = new Intent(this, PostDetailActivity.class);
                intent.putExtra("postId", postId);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        loadNotifications();

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down);
    }

    private void loadNotifications() {
        String userId = authManager.getUsername();
        if (userId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        List<Object[]> notifications = dbHelper.getNotifications(userId);
        adapter.updateData(notifications);
        if (notifications.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
