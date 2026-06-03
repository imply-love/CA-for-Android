package com.forum.cy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态列表（type = 1），支持回复。
 */
public class DynamicFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;

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
        List<Post> posts = dbHelper.getPostsByType(1);
        Map<Long, Integer> countMap = new HashMap<>();
        for (Post p : posts) countMap.put(p.id, dbHelper.getReplyCount(p.id));
        adapter.setReplyCountMap(countMap);
        adapter.updateData(posts);
    }
}
