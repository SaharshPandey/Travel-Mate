package io.github.project_travel_mate.friend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.project_travel_mate.R;
import listeners.OnClickListener;
import objects.Friend;
import objects.User;


class MyFriendsAdapter extends RecyclerView.Adapter<MyFriendsAdapter.MyFriendsViewHolder> {

    private final Context mContext;
    private final List<Friend> mFriends;
    private LayoutInflater mInflater;
    private OnClickListener onClick;

   public MyFriendsAdapter(Context context, OnClickListener onClick, List<Friend> friends) {
        this.mContext = context;
        this.mFriends = friends;
        this.onClick = onClick;
        mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public MyFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.friend_listitem, parent, false);
        return new MyFriendsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyFriendsViewHolder holder, int position) {

        /*Picasso.with(mContext).load(mFriends.get(position).getImage()).placeholder(R.drawable.default_user_icon)
                .error(R.drawable.default_user_icon)
                .into(holder.friendImage);*/
        String name = mFriends.get(position).getName();
        String contact = mFriends.get(position).getContact();
        holder.friendDisplayName.setText(name);
        holder.friendContactInfo.setText(contact);

        holder.my_friends_linear_layout.setOnClickListener(v -> {
            /*Intent intent = FriendsProfileActivity.getStartIntent(mContext, mFriends.get(position).getId());
            mContext.startActivity(intent);*/
            onClick.onClick(v, contact);

        });

    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    class MyFriendsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.friend_display_name)
        TextView friendDisplayName;
        @BindView(R.id.friend_contact_info)
        TextView friendContactInfo;
        @BindView(R.id.my_friends_linear_layout)
        LinearLayout my_friends_linear_layout;

        MyFriendsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
