package com.forum.cy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.forum.cy.R;
import com.forum.cy.model.Reply;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 回复列表适配器。
 */
public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {
    private List<Reply> replies;

    public ReplyAdapter(List<Reply> replies) {
        this.replies = replies;
    }

    public void updateData(List<Reply> newReplies) {
        this.replies = newReplies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reply_item, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Reply reply = replies.get(position);
        holder.author.setText(reply.anonymous ? "匿名用户" : reply.author);
        holder.content.setText(reply.content);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.time.setText(sdf.format(new Date(reply.time)));
    }

    @Override
    public int getItemCount() {
        return replies == null ? 0 : replies.size();
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView author, content, time;
        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.tv_reply_author);
            content = itemView.findViewById(R.id.tv_reply_content);
            time = itemView.findViewById(R.id.tv_reply_time);
        }
    }
}
