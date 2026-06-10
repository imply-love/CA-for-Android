package com.forum.cy;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.model.Post;
import com.forum.cy.util.AuthManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 发布帖子弹窗（毛玻璃卡片风格，匹配全局 UI）。
 */
public class PublishPostDialog extends DialogFragment {
    private EditText etTitle, etContent;
    private SwitchCompat swAnonymous;
    private LinearLayout anonymousRow;
    private TextView btnPublish, btnCancel, btnSelectImage, btnRemoveImage;
    private ImageView ivPreview;
    private DatabaseHelper dbHelper;
    private int postType;
    private Uri selectedImageUri;

    private ActivityResultLauncher<String> imagePicker;

    public PublishPostDialog(int postType) {
        this.postType = postType;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                showPreview(uri);
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_publish_post, null);
        dialog.setContentView(view);

        // 全屏透明窗口，卡片居中显示
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
            window.setDimAmount(0.5f);
        }

        // 绑定视图
        etTitle = view.findViewById(R.id.et_title);
        etContent = view.findViewById(R.id.et_content);
        anonymousRow = view.findViewById(R.id.anonymous_row);
        swAnonymous = view.findViewById(R.id.sw_anonymous);
        btnPublish = view.findViewById(R.id.btn_publish);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnRemoveImage = view.findViewById(R.id.btn_remove_image);
        ivPreview = view.findViewById(R.id.iv_preview);
        dbHelper = new DatabaseHelper(requireContext());

        // 动态页显示匿名开关
        if (postType == 1) {
            anonymousRow.setVisibility(View.VISIBLE);
        }

        // 点击卡片外部关闭
        FrameLayout rootLayout = (FrameLayout) view;
        rootLayout.setOnClickListener(v -> dismiss());
        View card = view.findViewById(R.id.card_container);
        card.setOnClickListener(v -> { /* 拦截，不关闭 */ });

        // 按钮事件
        btnPublish.setOnClickListener(v -> publishPost());
        btnCancel.setOnClickListener(v -> dismiss());
        btnSelectImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            ivPreview.setVisibility(View.GONE);
            btnRemoveImage.setVisibility(View.GONE);
        });

        // 进入动画
        window.getAttributes().windowAnimations = R.style.DialogSlideAnim;

        return dialog;
    }

    private void showPreview(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            if (bitmap != null) {
                ivPreview.setImageBitmap(bitmap);
                ivPreview.setVisibility(View.VISIBLE);
                btnRemoveImage.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    private void publishPost() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(getContext(), "标题和内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean anonymous = swAnonymous.isChecked();
        String author = new AuthManager(requireContext()).getUsername();
        if (author == null) author = "匿名";
        Post post = new Post(postType, title, content, System.currentTimeMillis(), author, anonymous);
        long id = dbHelper.insertPost(post);
        if (id != -1) {
            if (selectedImageUri != null) {
                String imagePath = saveImage(selectedImageUri, id);
                if (imagePath != null) {
                    dbHelper.updatePostImage(id, imagePath);
                }
            }
            Toast.makeText(getContext(), "发布成功", Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            Toast.makeText(getContext(), "发布失败", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImage(Uri uri, long postId) {
        try {
            File imageDir = new File(requireContext().getExternalFilesDir(null), "post_images");
            if (!imageDir.exists()) imageDir.mkdirs();
            File imageFile = new File(imageDir, "post_" + postId + ".jpg");

            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            if (bitmap == null) return null;

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}
