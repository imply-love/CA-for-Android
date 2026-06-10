package com.forum.cy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.util.AuthManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * "我的"页面，显示个人信息、头像和退出登录。
 */
public class ProfileFragment extends Fragment {
    private ImageView ivAvatar;
    private TextView tvUsername, tvAccountLabel;
    private Button btnLogout, btnGoLogin, btnFavorites, btnNotifications;
    private SwitchCompat switchDarkMode;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;

    private static final String PREF_THEME = "theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final ActivityResultLauncher<String> avatarPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) saveAvatar(uri);
            });

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
        btnFavorites = view.findViewById(R.id.btn_favorites);
        btnNotifications = view.findViewById(R.id.btn_notifications);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        authManager = new AuthManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        // 夜间模式开关
        SharedPreferences themePrefs = requireContext().getSharedPreferences(PREF_THEME, 0);
        boolean isDark = themePrefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDark);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themePrefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // 点击头像选择图片
        ivAvatar.setOnClickListener(v -> {
            if (!authManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            avatarPicker.launch("image/*");
        });

        btnLogout.setOnClickListener(v -> {
            authManager.logout();
            Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
            refreshUI();
        });

        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), LoginActivity.class));
        });

        btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), FavoritesActivity.class));
        });

        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NotificationsActivity.class));
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
            btnFavorites.setVisibility(View.VISIBLE);
            btnNotifications.setVisibility(View.VISIBLE);
            // 显示未读通知数
            int unread = dbHelper.getUnreadNotificationCount(username);
            if (unread > 0) {
                btnNotifications.setText("🔔 消息通知 (" + unread + ")");
            } else {
                btnNotifications.setText("🔔 消息通知");
            }
            loadAvatar();
        } else {
            tvUsername.setText("未登录");
            tvAccountLabel.setText("请先登录以使用完整功能");
            btnLogout.setVisibility(View.GONE);
            btnGoLogin.setVisibility(View.VISIBLE);
            btnFavorites.setVisibility(View.GONE);
            btnNotifications.setVisibility(View.GONE);
            ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    private void loadAvatar() {
        String username = authManager.getUsername();
        if (username == null) return;
        String path = dbHelper.getAvatarPath(username);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    ivAvatar.setImageBitmap(bitmap);
                    return;
                }
            }
        }
        ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
    }

    private void saveAvatar(Uri uri) {
        try {
            String username = authManager.getUsername();
            if (username == null) return;
            // 保存到应用私有目录
            File avatarDir = new File(requireContext().getExternalFilesDir(null), "avatars");
            if (!avatarDir.exists()) avatarDir.mkdirs();
            File avatarFile = new File(avatarDir, username + ".jpg");

            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            if (bitmap == null) return;

            // 压缩并保存
            FileOutputStream fos = new FileOutputStream(avatarFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            // 更新数据库
            dbHelper.updateAvatarPath(username, avatarFile.getAbsolutePath());
            ivAvatar.setImageBitmap(bitmap);
            Toast.makeText(getContext(), "头像已更新", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "头像保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}
