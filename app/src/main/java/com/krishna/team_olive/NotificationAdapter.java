package com.krishna.team_olive;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationsModel> mlist;
    private Context mcontext;
    private int Type = 0;

    public NotificationAdapter(List<NotificationsModel> mlist, Context mcontext) {
        this.mlist = mlist;
        this.mcontext = mcontext;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.notification_item,parent,false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        NotificationsModel notificationsModel = mlist.get(position);
        getuserinfo(holder.iv_notif_profile, holder.tv_notif_name, notificationsModel.getUser_id());

        //Like notif
        if(notificationsModel.getType() == 1){
            geticonimage(holder.iv_notif_arrow, notificationsModel.getPost_id());
            holder.tv_notif_detail.setText("liked your post");
        }
        //Request accepted notif
        if(notificationsModel.getType() == 2){
            holder.iv_notif_arrow.setImageResource(R.drawable.ic__arrow_right);
            holder.tv_notif_detail.setText("accepted your exchange request.");
            holder.iv_notif_arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Yha se ab map wle fragment m jana h !!!!
                }
            });
        }
        //
        if(notificationsModel.getType() == 3){

        }

    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_notif_name, tv_notif_detail;
        ImageView iv_notif_profile, iv_notif_arrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_notif_detail = itemView.findViewById(R.id.tv_info_notif);
            tv_notif_name = itemView.findViewById(R.id.tv_name_notif);
            iv_notif_arrow = itemView.findViewById(R.id.iv_notif_);
            iv_notif_profile = itemView.findViewById(R.id.iv_notif_profile);
        }
    }

    private void getuserinfo(final ImageView imageView, final TextView username, String publisherid){
        FirebaseDatabase.getInstance().getReference().child("users").child(publisherid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Users users = snapshot.getValue(Users.class);
                    username.setText(users.getName());
                    Picasso.get().load(users.getProfileimg()).placeholder(R.drawable.ic_launcher_background).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void geticonimage(final ImageView imageView, String postid){
        FirebaseDatabase.getInstance().getReference().child("allpostwithoutuser").child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AddedItemDescriptionModel addedItemDescriptionModel = snapshot.getValue(AddedItemDescriptionModel.class);
                Picasso.get().load(addedItemDescriptionModel.getImageurl()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}