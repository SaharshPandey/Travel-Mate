package io.github.project_travel_mate.mytrips;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import database.DBManager;
import io.github.project_travel_mate.R;
import io.github.project_travel_mate.friend.BottomDialogFragment;
import io.github.project_travel_mate.friend.FriendAdapter;
import listeners.OnClickListener;
import objects.Friend;

import static android.view.View.GONE;


public class BottomFriendFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private final List<Friend> friends = new ArrayList<>();

    @BindView(R.id.friend_list_recyclerView)
        RecyclerView friendRecyclerView;
        Activity mActivity;
        private static OnClickListener onCLick;
        FriendAdapter mAdapter;


        static String contacts;
        public static BottomFriendFragment getInstance(String contact) {
            contacts = contact;
            return new BottomFriendFragment();
        }


        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.bottomsheet_friend_fragment, container, false);
            ButterKnife.bind(view);
            friendRecyclerView = view.findViewById(R.id.friend_list_recyclerView);
            LoadQuery();
            return view;
        }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    public void sendSMS(String number)
        {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
        }

        public void call(String number){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
            startActivity(intent);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.call_friend){
                call(contacts);
            }
            if(v.getId() == R.id.message_friend){
                sendSMS(contacts);
            }
        }

    private void LoadQuery() {
        DBManager dbManager = new DBManager(getContext());
        String[] projections = new String[]{"_id", "name", "contact"};
        String[] selectionArgs = new String[]{""};

        Cursor cursor = dbManager.Query(projections, "name like ?", selectionArgs, null);

        if (cursor.getCount() == 0) {
        } else {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String contact = cursor.getString(cursor.getColumnIndex("contact"));

                    friends.add(new Friend(id, name, contact));


                } while (cursor.moveToNext());
            }
            mAdapter = new FriendAdapter(mActivity.getApplicationContext(), onCLick, friends);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            friendRecyclerView.setLayoutManager(layoutManager);
            friendRecyclerView.setAdapter(mAdapter);
        }
    }
    }


