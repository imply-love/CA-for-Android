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
import com.forum.cy.model.Reply;
import com.forum.cy.util.AuthManager;
import com.forum.cy.util.BlurUtil;

/**
 * 回复弹窗，支持内容输入和可选匿名回复（仅动态页帖子）。
 * 通过 Arguments 传入 postId 和 postType。
 */
public class ReplyDialog extends DialogFragment {
    private static final String ARG_POST_ID = "postId";
    private static final String ARG_POST_TYPE = "postType";

    public static ReplyDialog newInstance(long postId, int postType) {
        ReplyDialog dialog = new ReplyDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_POST_ID, postId);
        args.putInt(ARG_POST_TYPE, postType);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reply, null);
        BlurUtil.applyAcrylic(view);

        EditText etContent = view.findViewById(R.id.et_reply_content);
        Switch swAnonymous = view.findViewById(R.id.sw_reply_anonymous);
        Button btnCancel = view.findViewById(R.id.btn_reply_cancel);
        Button btnSend = view.findViewById(R.id.btn_reply_send);

        long postId = getArguments() != null ? getArguments().getLong(ARG_POST_ID, -1) : -1;
        int postType = getArguments() != null ? getArguments().getInt(ARG_POST_TYPE, 0) : 0;

        // 仅动态页（type=1）显示匿名开关
        swAnonymous.setVisibility(postType == 1 ? View.VISIBLE : View.GONE);

        btnCancel.setOnClickListener(v -> dismiss());
        btnSend.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(getContext(), "回复内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            AuthManager auth = new AuthManager(requireContext());
            String author = auth.getUsername();
            if (author == null) author = "匿名";
            boolean anonymous = swAnonymous.isChecked();

            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            Reply reply = new Reply(postId, content, System.currentTimeMillis(), author, anonymous);
            long id = dbHelper.insertReply(reply);
            if (id != -1) {
                Toast.makeText(getContext(), "回复成功", Toast.LENGTH_SHORT).show();
                // 通知宿主 Activity/Fragment 刷新
                if (getActivity() instanceof OnReplySentListener) {
                    ((OnReplySentListener) getActivity()).onReplySent();
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "回复失败", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    /** 回复发送成功的回调接口，宿主 Activity 可实现此接口 */
    public interface OnReplySentListener {
        void onReplySent();
    }
}
