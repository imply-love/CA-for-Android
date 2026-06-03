package com.forum.cy.util;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

/**
 * 工具类，快速在任意根视图上开启 Acrylic（半透明遮罩）效果。
 * 使用原生 View 实现，无需外部库。
 */
public class BlurUtil {
    private static final int OVERLAY_TAG_KEY = 0x7f0a0001; // 任意不冲突的 tag key

    /**
     * 在给定的根视图上叠加一层半透明遮罩，模拟亚克力背景。
     * @param root 根视图（通常是 dialog 的内容布局）
     */
    public static void applyAcrylic(View root) {
        if (!(root instanceof ViewGroup)) return;
        ViewGroup rootGroup = (ViewGroup) root;

        // 如果已经添加过遮罩则跳过
        View existing = rootGroup.findViewWithTag(OVERLAY_TAG_KEY);
        if (existing != null) return;

        // 创建半透明深色遮罩
        View overlay = new View(root.getContext());
        overlay.setTag(OVERLAY_TAG_KEY);
        overlay.setBackgroundColor(Color.parseColor("#80000000")); // 50% 黑色
        overlay.setClickable(true); // 拦截触摸事件
        rootGroup.addView(overlay, 0,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
