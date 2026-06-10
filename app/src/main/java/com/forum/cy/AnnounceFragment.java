package com.forum.cy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.forum.cy.adapter.PostAdapter;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.model.Post;
import com.forum.cy.util.AuthManager;
import java.util.ArrayList;
import java.util.List;

/**
 * 公告列表 Fragment（type = 0）。
 */
public class AnnounceFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(requireContext());
        authManager = new AuthManager(requireContext());

        adapter = new PostAdapter(new ArrayList<>(), post -> openDetail(post.id, false));
        adapter.setOnReplyClickListener(post -> openDetail(post.id, true));
        recyclerView.setAdapter(adapter);

        // 绑定 FAB 发布按钮
        View fabPublish = view.findViewById(R.id.fab_publish);
        if (fabPublish != null) {
            fabPublish.setOnClickListener(v -> {
                if (!authManager.isLoggedIn()) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    return;
                }
                PublishPostDialog dialog = new PublishPostDialog(0);
                dialog.show(getParentFragmentManager(), "publish_announce");
            });
            // FAB 缩放进入动画
            fabPublish.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_scale_in));
        }

        loadPosts();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPosts();
    }

    private void openDetail(long postId, boolean autoFocusReply) {
        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtra("postId", postId);
        intent.putExtra("autoFocusReply", autoFocusReply);
        startActivity(intent);
    }

    private void loadPosts() {
        List<Post> posts = dbHelper.getPostsByType(0);
        adapter.updateData(posts);
    }
}
