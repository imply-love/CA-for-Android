package com.forum.cy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.forum.cy.util.AuthManager;

/**
 * "我的"页面，显示个人信息和退出登录。
 */
public class ProfileFragment extends Fragment {
    private ImageView ivAvatar;
    private TextView tvUsername, tvAccountLabel;
    private Button btnLogout, btnGoLogin;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvUsername = view.findViewById(R.id.tv_username);
        tvAccountLabel = view.findViewById(R.id.tv_account_label);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnGoLogin = view.findViewById(R.id.btn_go_login);
        authManager = new AuthManager(requireContext());

        btnLogout.setOnClickListener(v -> {
            authManager.logout();
            Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
            refreshUI();
        });

        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), LoginActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        if (authManager.isLoggedIn()) {
            String username = authManager.getUsername();
            tvUsername.setText(username != null ? username : "用户");
            tvAccountLabel.setText("本地账号");
            btnLogout.setVisibility(View.VISIBLE);
            btnGoLogin.setVisibility(View.GONE);
        } else {
            tvUsername.setText("未登录");
            tvAccountLabel.setText("请先登录以使用完整功能");
            btnLogout.setVisibility(View.GONE);
            btnGoLogin.setVisibility(View.VISIBLE);
        }
    }
}
