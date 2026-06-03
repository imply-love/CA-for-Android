package com.forum.cy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.util.AuthManager;
import com.forum.cy.util.BlurUtil;

/**
 * 登录弹窗，使用亚克力（BlurView）背景。
 * 若登录成功，调用 AuthManager 保存登录状态。
 * 若用户点击 "注册" 按钮，跳转到 RegisterActivity。
 */
public class LoginDialog extends DialogFragment {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);
        // 应用亚克力效果
        BlurUtil.applyAcrylic(view);
        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        btnRegister = view.findViewById(R.id.btn_register);
        authManager = new AuthManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> {
            // 打开注册页面
            startActivity(new android.content.Intent(getContext(), RegisterActivity.class));
            dismiss();
        });
        builder.setView(view);
        // 让对话框的背景透明，以显示 BlurView 效果
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "请填写用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.validateUser(username, password)) {
            authManager.login(username);
            Toast.makeText(getContext(), "登录成功", Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            Toast.makeText(getContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}
