package com.forum.cy.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.forum.cy.R;
import com.forum.cy.model.Post;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 帖子列表适配器。回复按钮点击跳转详情页并自动打开回复框。
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private OnItemClickListener listener;
    private OnReplyClickListener replyListener;

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    /** 回复按钮点击回调 */
    public interface OnReplyClickListener {
        void onReplyClick(Post post);
    }

    public PostAdapter(List<Post> posts, OnItemClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    public void setOnReplyClickListener(OnReplyClickListener replyListener) {
        this.replyListener = replyListener;
    }

    public void updateData(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.title.setText(post.title);
        holder.content.setText(post.content);
        holder.time.setText(formatTime(post.time));

        // 回复计数（直接使用 post 对象中的 replyCount，无需额外查询）
        if (post.replyCount > 0) {
            holder.replyCount.setText(post.replyCount + " 条回复");
            holder.replyCount.setVisibility(View.VISIBLE);
        } else {
            holder.replyCount.setVisibility(View.GONE);
        }

        // 点击卡片 → 进入详情页
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(post);
        });

        // 点击回复按钮 → 进入详情页并自动打开回复框
        holder.btnReply.setOnClickListener(v -> {
            if (replyListener != null) replyListener.onReplyClick(post);
        });

        // 帖子缩略图
        if (post.imagePath != null && !post.imagePath.isEmpty()) {
            File imgFile = new File(post.imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(post.imagePath);
                if (bitmap != null) {
                    holder.thumbnail.setImageBitmap(bitmap);
                    holder.thumbnail.setVisibility(View.VISIBLE);
                } else {
                    holder.thumbnail.setVisibility(View.GONE);
                }
            } else {
                holder.thumbnail.setVisibility(View.GONE);
            }
        } else {
            holder.thumbnail.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts == null ? 0 : posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, time, btnReply, replyCount;
        ImageView thumbnail;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            content = itemView.findViewById(R.id.tv_content);
            time = itemView.findViewById(R.id.tv_time);
            btnReply = itemView.findViewById(R.id.btn_reply);
            replyCount = itemView.findViewById(R.id.tv_reply_count);
            thumbnail = itemView.findViewById(R.id.iv_thumbnail);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
