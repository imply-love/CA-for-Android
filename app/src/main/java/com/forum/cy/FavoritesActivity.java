package com.forum.cy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.forum.cy.adapter.PostAdapter;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.model.Post;
import com.forum.cy.util.AuthManager;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的收藏页面，展示当前用户收藏的帖子列表。
 */
public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private PostAdapter adapter;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView = findViewById(R.id.recycler_favorites);
        tvEmpty = findViewById(R.id.tv_empty);
        TextView btnBack = findViewById(R.id.btn_back);

        dbHelper = new DatabaseHelper(this);
        authManager = new AuthManager(this);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(new ArrayList<>(), post -> openDetail(post.id, false));
        adapter.setOnReplyClickListener(post -> openDetail(post.id, true));
        recyclerView.setAdapter(adapter);

        loadFavorites();

        // 进入动画
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void openDetail(long postId, boolean autoFocusReply) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId", postId);
        intent.putExtra("autoFocusReply", autoFocusReply);
        startActivity(intent);
    }

    private void loadFavorites() {
        String userId = authManager.getUsername();
        if (userId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        List<Post> posts = dbHelper.getFavoritePosts(userId);
        adapter.updateData(posts);
        if (posts.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
