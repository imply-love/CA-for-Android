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
import com.forum.cy.model.Reply;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 帖子详情页统一适配器，包含帖子信息头部和回复列表。
 * 使用多 ViewType 实现，替代 ScrollView + RecyclerView 嵌套方案。
 */
public class PostDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_REPLY = 1;

    private Post post;
    private List<Reply> replies = new ArrayList<>();
    private String currentUsername;
    private boolean isFavorited;
    private OnDeletePostListener onDeletePostListener;
    private OnEditPostListener onEditPostListener;
    private OnDeleteReplyListener onDeleteReplyListener;
    private OnFavoriteListener onFavoriteListener;

    public interface OnDeletePostListener {
        void onDeleteClick();
    }

    public interface OnEditPostListener {
        void onEditClick();
    }

    public interface OnDeleteReplyListener {
        void onDeleteClick(Reply reply, int position);
    }

    public interface OnFavoriteListener {
        void onFavoriteClick();
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    public void setOnDeletePostListener(OnDeletePostListener listener) {
        this.onDeletePostListener = listener;
    }

    public void setOnEditPostListener(OnEditPostListener listener) {
        this.onEditPostListener = listener;
    }

    public void setOnDeleteReplyListener(OnDeleteReplyListener listener) {
        this.onDeleteReplyListener = listener;
    }

    public void setOnFavoriteListener(OnFavoriteListener listener) {
        this.onFavoriteListener = listener;
    }

    public void setFavoriteState(boolean favorited) {
        this.isFavorited = favorited;
        notifyItemChanged(0);
    }

    public void setPost(Post post) {
        this.post = post;
        notifyItemChanged(0);
    }

    public void updateReplies(List<Reply> newReplies) {
        int oldSize = this.replies.size();
        this.replies = newReplies;
        if (oldSize > 0) notifyItemRangeRemoved(1, oldSize);
        if (!newReplies.isEmpty()) notifyItemRangeInserted(1, newReplies.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_REPLY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_post_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.reply_item, parent, false);
            return new ReplyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            bindHeader((HeaderViewHolder) holder);
        } else if (holder instanceof ReplyViewHolder) {
            bindReply((ReplyViewHolder) holder, position - 1);
        }
    }

    private void bindHeader(HeaderViewHolder h) {
        if (post == null) return;
        h.tvTitle.setText(post.title);
        h.tvContent.setText(post.content);
        h.tvAuthor.setText(post.author);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        h.tvTime.setText(sdf.format(new Date(post.time)));

        // 回复区标题
        h.tvReplyHeader.setText("回复 (" + replies.size() + ")");

        // 编辑和删除按钮：仅作者可见
        String currentUser = currentUsername;
        boolean isOwner = currentUser != null && currentUser.equals(post.author);
        h.btnEdit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        h.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        h.btnEdit.setOnClickListener(v -> {
            if (onEditPostListener != null) onEditPostListener.onEditClick();
        });
        h.btnDelete.setOnClickListener(v -> {
            if (onDeletePostListener != null) onDeletePostListener.onDeleteClick();
        });

        // 收藏按钮
        h.btnFavorite.setText(isFavorited ? "★ 已收藏" : "☆ 收藏");
        h.btnFavorite.setTextColor(isFavorited ? 0xFFFFD700 : 0xFF7B68EE); // 金色 vs 紫色
        h.btnFavorite.setOnClickListener(v -> {
            if (onFavoriteListener != null) onFavoriteListener.onFavoriteClick();
        });

        // 帖子附图
        if (post.imagePath != null && !post.imagePath.isEmpty()) {
            File imgFile = new File(post.imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(post.imagePath);
                if (bitmap != null) {
                    h.ivPostImage.setImageBitmap(bitmap);
                    h.ivPostImage.setVisibility(View.VISIBLE);
                } else {
                    h.ivPostImage.setVisibility(View.GONE);
                }
            } else {
                h.ivPostImage.setVisibility(View.GONE);
            }
        } else {
            h.ivPostImage.setVisibility(View.GONE);
        }
    }

    private void bindReply(ReplyViewHolder h, int replyIndex) {
        Reply reply = replies.get(replyIndex);
        h.tvAuthor.setText(reply.anonymous ? "匿名用户" : reply.author);
        h.tvContent.setText(reply.content);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        h.tvTime.setText(sdf.format(new Date(reply.time)));

        boolean isOwner = currentUsername != null && currentUsername.equals(reply.author);
        h.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        h.btnDelete.setOnClickListener(v -> {
            if (onDeleteReplyListener != null) {
                onDeleteReplyListener.onDeleteClick(reply, replyIndex);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (post != null ? 1 : 0) + replies.size();
    }

    /** 帖子信息头部 ViewHolder */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor, tvTime;
        TextView tvReplyHeader;
        TextView btnEdit, btnDelete, btnFavorite;
        ImageView ivPostImage;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_detail_title);
            tvContent = itemView.findViewById(R.id.tv_detail_content);
            tvAuthor = itemView.findViewById(R.id.tv_detail_author);
            tvTime = itemView.findViewById(R.id.tv_detail_time);
            tvReplyHeader = itemView.findViewById(R.id.tv_reply_header);
            btnEdit = itemView.findViewById(R.id.btn_edit_post);
            btnDelete = itemView.findViewById(R.id.btn_delete_post);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            ivPostImage = itemView.findViewById(R.id.iv_post_image);
        }
    }

    /** 回复条目 ViewHolder */
    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvContent, tvTime, btnDelete;

        ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tv_reply_author);
            tvContent = itemView.findViewById(R.id.tv_reply_content);
            tvTime = itemView.findViewById(R.id.tv_reply_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_reply);
        }
    }
}
