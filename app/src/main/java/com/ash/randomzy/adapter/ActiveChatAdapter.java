package com.ash.randomzy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ash.randomzy.R;
import com.ash.randomzy.constants.SentBy;
import com.ash.randomzy.entity.ActiveChat;
import com.ash.randomzy.listener.OnItemClickListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ActiveChatAdapter extends RecyclerView.Adapter<ActiveChatAdapter.ActiveChatViewHolder> {

    List<ActiveChat> activeChatList;
    OnItemClickListener onItemClickListener;

    public ActiveChatAdapter(List<ActiveChat> activeChatList, OnItemClickListener onItemClickListener) {
        this.activeChatList = activeChatList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ActiveChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_chat_item, parent, false);
        return new ActiveChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveChatViewHolder holder, int position) {
        ActiveChat activeChat = activeChatList.get(position);
        holder.nameTxtView.setText(activeChat.getName());
        holder.lastMessageTxtView.setText(activeChat.getLastText());
        holder.lastMessageTimeTxtView.setText(activeChat.getLastTextTime()+"");
        holder.unreadCountTxtView.setText(""+1);
        if(activeChat.getSentBy() == SentBy.SENT_BY_ME)
            holder.unreadCountTxtView.setVisibility(View.GONE);
        else
            holder.tickImageView.setVisibility(View.GONE);
        holder.bind(activeChatList.get(position),onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return activeChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void addNewActiveChat(ActiveChat activeChat){
        activeChatList.add(activeChat);
        notifyDataSetChanged();
    }


    class ActiveChatViewHolder extends RecyclerView.ViewHolder {

        TextView nameTxtView;
        TextView lastMessageTxtView;
        TextView lastMessageTimeTxtView;
        TextView unreadCountTxtView;
        ImageView profileImageView;
        ImageView tickImageView;
        LinearLayout activeChatItem;

        ActiveChat activeChat;
        OnItemClickListener itemClickListener;

        public ActiveChatViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTxtView = itemView.findViewById(R.id.name_txt_view);
            lastMessageTxtView = itemView.findViewById(R.id.last_message_txt_view);
            lastMessageTimeTxtView = itemView.findViewById(R.id.last_message_time_txt_view);
            unreadCountTxtView = itemView.findViewById(R.id.unread_count_txt_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            tickImageView = itemView.findViewById(R.id.tick_image_view);
            activeChatItem = itemView.findViewById(R.id.active_chat_item);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(activeChat);
                }
            });
        }

        public void bind(ActiveChat activeChat, OnItemClickListener onItemClickListener){
            this.activeChat = activeChat;
            this.itemClickListener = onItemClickListener;
        }
    }
}
