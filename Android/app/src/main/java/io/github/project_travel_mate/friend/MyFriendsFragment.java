package io.github.project_travel_mate.friend;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import database.DBManager;
import io.github.project_travel_mate.MainActivity;
import io.github.project_travel_mate.R;
import listeners.OnClickListener;
import objects.Friend;
import objects.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.view.View.GONE;
import static utils.Constants.API_LINK_V2;
import static utils.Constants.USER_TOKEN;

public class MyFriendsFragment extends Fragment implements View.OnClickListener {

    private final List<User> mFriends = new ArrayList<>();
    private final List<Friend> friends = new ArrayList<>();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.add_friend)
    FloatingActionButton floatingActionButton;

    @BindView(R.id.animation_view)
    LottieAnimationView animationView;

    private String mToken;
    private Handler mHandler;
    private Activity mActivity;
    private MyFriendsAdapter mAdapter;
    private static OnClickListener onCLick;

    public MyFriendsFragment() {
        // Required empty public constructor
    }


    public static MyFriendsFragment newInstance(OnClickListener onclick) {
        onCLick = onclick;
        return new MyFriendsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_friends, container, false);
        ButterKnife.bind(this, view);
        floatingActionButton.setOnClickListener(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mToken = sharedPreferences.getString(USER_TOKEN, null);
        mHandler = new Handler(Looper.getMainLooper());
        LoadQuery();
       // myFriends();

        ((MainActivity)getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {

                if(friends!=null){
                    friends.clear();
                    LoadQuery();
                }
                // Refresh Your Fragment
            }
        });

        return view;

    }

   /* private void myFriends() {

        String uri = API_LINK_V2 + "trip-friends-all";

        Log.v("EXECUTING", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        final Request request = new Request.Builder()
                .header("Authorization", "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.e("Request Failed", "Message : " + e.getMessage());
                mHandler.post(() -> networkError());
            }

            @Override
            public void onResponse(Call call, final Response response) {

                mHandler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONArray arr;
                        try {
                            final String res = response.body().string();
                            Log.v("Response", res);
                            arr = new JSONArray(res);

                            if (arr.length() <= 1) {
                                noResults();
                                return;
                            }

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject object = arr.getJSONObject(i);
                                String userName = object.getString("username");
                                String firstName = object.getString("first_name");
                                String lastName = object.getString("last_name");
                                int id = object.getInt("id");
                                String imageURL = object.getString("image");
                                String dateJoined = object.getString("date_joined");
                                String status = object.getString("status");
                                mFriends.add(new User(userName, firstName, lastName, id, imageURL, dateJoined, status));
                                animationView.setVisibility(GONE);
                                //mAdapter = new MyFriendsAdapter(mActivity.getApplicationContext(), mFriends);
                                recyclerView.setAdapter(mAdapter);
                            }
                        } catch (JSONException | IOException | NullPointerException e) {
                            e.printStackTrace();
                            Log.e("ERROR", "Message : " + e.getMessage());
                            networkError();
                        }
                    } else {
                        networkError();
                    }
                });
            }
        });
    }
*/

    /**
     * Plays the network lost animation in the view
     */
    private void networkError() {
        animationView.setAnimation(R.raw.network_lost);
        animationView.playAnimation();
    }

    /**
     * Plays the no results animation in the view
     */
    private void noResults() {
        Toast.makeText(mActivity, R.string.no_friends_message, Toast.LENGTH_SHORT).show();
        animationView.setAnimation(R.raw.empty_list);
        animationView.playAnimation();

    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.mActivity = (Activity) activity;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_friend) {
            if (onCLick != null) {
                onCLick.onClick(v,"");
            }
        }
    }

    private void LoadQuery() {
        DBManager dbManager = new DBManager(getContext());
        String[] projections = new String[]{"_id", "name", "contact"};
        String[] selectionArgs = new String[]{""};

        Cursor cursor = dbManager.Query(projections, "name like ?", selectionArgs, null);

        if (cursor.getCount() == 0) {
            noResults();
        } else {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String contact = cursor.getString(cursor.getColumnIndex("contact"));

                    friends.add(new Friend(id, name, contact));


                } while (cursor.moveToNext());
            }
            animationView.setVisibility(GONE);
            mAdapter = new MyFriendsAdapter(getContext(), onCLick, friends);
            recyclerView.setAdapter(mAdapter);
        }
    }
}
