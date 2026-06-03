package com.forum.cy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.model.Post;
import com.forum.cy.util.BlurUtil;

/**
 * 发布帖子弹窗（包含标题、内容、匿名开关）。
 * 使用亚克力（BlurView）背景，提交后写入 SQLite 并刷新对应列表。
 */
public class PublishPostDialog extends DialogFragment {
    private EditText etTitle, etContent;
    private Switch swAnonymous;
    private Button btnPublish, btnCancel;
    private DatabaseHelper dbHelper;
    private int postType; // 0=公告,1=动态,2=工具

    public PublishPostDialog(int postType) {
        this.postType = postType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_publish_post, null);
        // 应用亚克力模糊效果
        BlurUtil.applyAcrylic(view);
        etTitle = view.findViewById(R.id.et_title);
        etContent = view.findViewById(R.id.et_content);
        swAnonymous = view.findViewById(R.id.sw_anonymous);
        btnPublish = view.findViewById(R.id.btn_publish);
        btnCancel = view.findViewById(R.id.btn_cancel);
        dbHelper = new DatabaseHelper(requireContext());
        // 动态页才显示匿名开关
        swAnonymous.setVisibility(postType == 1 ? View.VISIBLE : View.GONE);
        btnPublish.setOnClickListener(v -> publishPost());
        btnCancel.setOnClickListener(v -> dismiss());
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    private void publishPost() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "标题和内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean anonymous = swAnonymous.isChecked();
        // 简单使用当前时间和默认作者（登录用户）
        String author = new com.forum.cy.util.AuthManager(requireContext()).getUsername();
        if (author == null) author = "匿名";
        Post post = new Post(postType, title, content, System.currentTimeMillis(), author, anonymous);
        long id = dbHelper.insertPost(post);
        if (id != -1) {
            Toast.makeText(getContext(), "发布成功", Toast.LENGTH_SHORT).show();
            // 通过接口回调刷新列表（Fragment 可自行在 onResume 中 reload）
            dismiss();
        } else {
            Toast.makeText(getContext(), "发布失败", Toast.LENGTH_SHORT).show();
        }
    }
}
