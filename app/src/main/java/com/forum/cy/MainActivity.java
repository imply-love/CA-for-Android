package com.forum.cy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主入口 Activity，负责容纳 BottomNavigationView 与 NavHostFragment，
 * 切换四个业务 Fragment（公告、动态、工具、我的）。
 */
public class MainActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 通过 supportFragmentManager 获取 NavHostFragment（FragmentContainerView 需要此方式）
        NavHostFragment hostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = hostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    /**
     * 在进入帖子详情页时隐藏全局悬浮按钮，返回时恢复显示。
     * 这里提供给 Fragment 调用的公共方法。
     */
    public void setFabVisibility(boolean visible) {
        // 该实现留空，实际 UI 中可通过事件总线或共享 ViewModel 控制 FAB。
    }
}
