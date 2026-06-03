package com.forum.cy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
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
 * 工具页（type = 2），支持实时搜索工具。
 */
public class ToolsFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tools, container, false);
        recyclerView = view.findViewById(R.id.recycler_tools);
        tvEmpty = view.findViewById(R.id.tv_empty);
        EditText etSearch = view.findViewById(R.id.et_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(requireContext());
        authManager = new AuthManager(requireContext());

        adapter = new PostAdapter(new ArrayList<>(), post -> openDetail(post.id, false));
        adapter.setOnReplyClickListener(post -> openDetail(post.id, true));
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { doSearch(s.toString().trim()); }
        });

        loadAll();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EditText et = getView() != null ? getView().findViewById(R.id.et_search) : null;
        if (et != null) doSearch(et.getText().toString().trim());
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

    private void loadAll() { showResults(dbHelper.getPostsByType(2)); }

    private void doSearch(String keyword) {
        showResults(keyword.isEmpty() ? dbHelper.getPostsByType(2) : dbHelper.searchPosts(2, keyword));
    }

    private void showResults(List<Post> posts) {
        Map<Long, Integer> countMap = new HashMap<>();
        for (Post p : posts) countMap.put(p.id, dbHelper.getReplyCount(p.id));
        adapter.setReplyCountMap(countMap);
        adapter.updateData(posts);
        if (posts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
