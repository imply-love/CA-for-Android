package com.forum.cy;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.forum.cy.data.DatabaseHelper;
import com.forum.cy.util.AuthManager;

/**
 * 注册页面，用户输入用户名、密码、确认密码。
 * 密码一致后写入本地 users 表并返回登录对话框。
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etConfirm;
    private Button btnRegister;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);
        btnRegister = findViewById(R.id.btn_register);
        dbHelper = new DatabaseHelper(this);
        authManager = new AuthManager(this);
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "请完整填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        // 简单插入用户（忽略冲突处理）
        dbHelper.getWritableDatabase().execSQL("INSERT OR IGNORE INTO users (username,password) VALUES (?,?)", new Object[]{username, password});
        // 注册成功后直接登录
        authManager.login(username);
        Toast.makeText(this, "注册成功并已登录", Toast.LENGTH_SHORT).show();
        finish(); // 关闭注册页，返回上一层（可能是登录弹窗）
    }
}
