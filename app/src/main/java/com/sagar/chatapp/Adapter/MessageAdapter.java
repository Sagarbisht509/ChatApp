package com.sagar.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagar.chatapp.Models.Message;
import com.sagar.chatapp.R;
import com.sagar.chatapp.databinding.DeleteDialogBinding;
import com.sagar.chatapp.databinding.ItemReceiverBinding;
import com.sagar.chatapp.databinding.ItemSenderBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageAdapter extends RecyclerView.Adapter {

    final int sent = 1;
    final int receive = 2;

    String sender, receiver;

    FirebaseAuth auth;

    Context context;
    ArrayList<Message> messages;

    public MessageAdapter(String sender, String receiver, Context context, ArrayList<Message> messages) {
        this.sender = sender;
        this.receiver = receiver;
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        auth = FirebaseAuth.getInstance();
        if (auth.getUid().equals(message.getSenderId())) {
            return sent;
        } else {
            return receive;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;

            if (message.getMessage().equals("photo")) {
                viewHolder.senderBinding.sendImage.setVisibility(View.VISIBLE);
                viewHolder.senderBinding.sendMessage.setVisibility(View.GONE);

                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder)
                        .into(viewHolder.senderBinding.sendImage);
            } else {
                viewHolder.senderBinding.sendMessage.setText(message.getMessage());
            }

//            @SuppressLint("SimpleDateFormat")
//            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
//            viewHolder.senderBinding.sendTime.setText(dateFormat.format(new Date(message.getMessageTime())));

            viewHolder.itemView.setOnLongClickListener(v -> {
                View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                DeleteDialogBinding deleteDialogBinding = DeleteDialogBinding.bind(view);

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete message?")
                        .setView(deleteDialogBinding.getRoot())
                        .create();

                deleteDialogBinding.deleteForMe.setOnClickListener(v1 -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Chats")
                            .child(sender)
                            .child("Messages")
                            .child(message.getMessageId()).setValue(null);

                    dialog.dismiss();
                });

                deleteDialogBinding.cancel.setOnClickListener(v12 -> dialog.dismiss());

                deleteDialogBinding.deleteForEveryone.setOnClickListener(v13 -> {

                    if (message.getMessage().equals("photo")) {
                        viewHolder.senderBinding.sendMessage.setVisibility(View.VISIBLE);
                        viewHolder.senderBinding.sendImage.setVisibility(View.GONE);
                    }

                    FirebaseDatabase.getInstance().getReference()
                            .child("Chats")
                            .child(sender).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String messageId = snapshot.child("messageId").getValue(String.class);
                            if (message.getMessageId().equals(messageId)) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("lastMessage", "You delete this message");

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Chats")
                                        .child(sender).updateChildren(hashMap);

                                hashMap.put("lastMessage", "This message was deleted");

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Chats")
                                        .child(receiver).updateChildren(hashMap);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    message.setMessage("You deleted this message");

                    FirebaseDatabase.getInstance().getReference()
                            .child("Chats")
                            .child(sender)
                            .child("Messages")
                            .child(message.getMessageId()).setValue(message);

                    message.setMessage("This message was deleted");

                    FirebaseDatabase.getInstance().getReference()
                            .child("Chats")
                            .child(receiver)
                            .child("Messages")
                            .child(message.getMessageId()).setValue(message);

                    dialog.dismiss();
                });

                dialog.show();

                return false;
            });
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            if (message.getMessage().equals("photo")) {
                viewHolder.receiverBinding.receivedImage.setVisibility(View.VISIBLE);
                viewHolder.receiverBinding.receivedMessage.setVisibility(View.GONE);

                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder)
                        .into(viewHolder.receiverBinding.receivedImage);
            } else {
                viewHolder.receiverBinding.receivedMessage.setText(message.getMessage());
            }

//            @SuppressLint("SimpleDateFormat")
//            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
//            viewHolder.receiverBinding.receivedTime.setText(dateFormat.format(new Date(message.getMessageTime())));

            viewHolder.itemView.setOnLongClickListener(v -> {
                View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                DeleteDialogBinding deleteDialogBinding = DeleteDialogBinding.bind(view);

                deleteDialogBinding.deleteForEveryone.setVisibility(View.GONE);

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete message?")
                        .setView(deleteDialogBinding.getRoot())
                        .create();

                deleteDialogBinding.deleteForMe.setOnClickListener(v14 -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Chats")
                            .child(sender)
                            .child("Messages")
                            .child(message.getMessageId()).setValue(null).addOnSuccessListener(aVoid -> Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show());

                    dialog.dismiss();
                });

                deleteDialogBinding.cancel.setOnClickListener(v15 -> dialog.dismiss());

                dialog.show();

                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {

        ItemSenderBinding senderBinding;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderBinding = ItemSenderBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceiverBinding receiverBinding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverBinding = ItemReceiverBinding.bind(itemView);
        }
    }
}
