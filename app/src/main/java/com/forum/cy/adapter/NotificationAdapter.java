package com.forum.cy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.forum.cy.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 通知列表适配器。
 * 每条通知格式: Object[]{id, type, sourceUser, postId, content, isRead, time}
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {
    private List<Object[]> notifications = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(long postId, long notificationId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Object[]> newData) {
        this.notifications = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        Object[] notif = notifications.get(position);
        long id = (long) notif[0];
        String source = (String) notif[2];
        long postId = (long) notif[3];
        String content = (String) notif[4];
        boolean isRead = (boolean) notif[5];
        long time = (long) notif[6];

        holder.tvSource.setText(source != null ? source : "系统");
        holder.tvContent.setText(content);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(time)));
        holder.viewUnreadDot.setVisibility(isRead ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(postId, id);
        });
    }

    @Override
    public int getItemCount() {
        return notifications == null ? 0 : notifications.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView tvSource, tvContent, tvTime;
        View viewUnreadDot;

        NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSource = itemView.findViewById(R.id.tv_notif_source);
            tvContent = itemView.findViewById(R.id.tv_notif_content);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            viewUnreadDot = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}
