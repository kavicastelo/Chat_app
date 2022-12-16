package com.example.chat.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.databinding.ItemContainerRecievedMessageBinding;
import com.example.chat.databinding.ItemContainerSentMessageBinding;
import com.example.chat.models.chatMessage;

import java.util.List;

public class chatAdapters extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<chatMessage> chtMsg;
    private Bitmap receiverProfile;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfile(Bitmap bitmap){
        receiverProfile = bitmap;
    }

    public chatAdapters(List<chatMessage> chtMsg, Bitmap receiverProfile, String senderId) {
        this.chtMsg = chtMsg;
        this.receiverProfile = receiverProfile;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new sentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
        else{
            return new receivedMessageViewHolder(
                    ItemContainerRecievedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((sentMessageViewHolder) holder).setData(chtMsg.get(position));
        }
        else{
            ((receivedMessageViewHolder) holder).setData(chtMsg.get(position),receiverProfile);
        }
    }

    @Override
    public int getItemCount() {
        return chtMsg.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chtMsg.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    public class sentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        sentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(chatMessage chtMsg){
            binding.txtMessage.setText(chtMsg.message);
            binding.txtDateTime.setText(chtMsg.dateTime);
        }
    }

    static class receivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerRecievedMessageBinding binding;

        receivedMessageViewHolder(ItemContainerRecievedMessageBinding itemContainerRecievedMessageBinding){
            super(itemContainerRecievedMessageBinding.getRoot());
            binding = itemContainerRecievedMessageBinding;
        }

        void setData(chatMessage chtMsg, Bitmap receiverProfile){
            binding.txtMessage.setText(chtMsg.message);
            binding.txtDateTime.setText(chtMsg.dateTime);
            if(receiverProfile != null){
                binding.imageProfile.setImageBitmap(receiverProfile);
            }
        }
    }
}
