package io.github.project_travel_mate;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.juanlabrador.badgecounter.BadgeCounter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import butterknife.ButterKnife;
import database.DBManager;
import io.github.project_travel_mate.destinations.CityFragment;
import io.github.project_travel_mate.favourite.FavouriteCitiesFragment;
import io.github.project_travel_mate.friend.BottomDialogFragment;
import io.github.project_travel_mate.friend.FriendsProfileActivity;
import io.github.project_travel_mate.friend.MyFriendsFragment;
import io.github.project_travel_mate.login.LoginActivity;
import io.github.project_travel_mate.mytrips.MyTripInfoActivity;
import io.github.project_travel_mate.mytrips.MyTripsFragment;
import io.github.project_travel_mate.notifications.NotificationsActivity;
import io.github.project_travel_mate.travel.TravelFragment;
import io.github.project_travel_mate.utilities.AboutUsFragment;
import io.github.project_travel_mate.utilities.UtilitiesFragment;
import io.github.tonnyl.whatsnew.WhatsNew;
import io.github.tonnyl.whatsnew.item.WhatsNewItem;
import listeners.OnClickListener;
import objects.Trip;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.DailyQuotesManager;

import static utils.Constants.API_LINK_V2;
import static utils.Constants.AUTHORIZATION;
import static utils.Constants.SHARE_PROFILE_USER_ID_QUERY;
import static utils.Constants.SHARE_TRIP_TRIP_ID_QUERY;
import static utils.Constants.USER_DATE_JOINED;
import static utils.Constants.USER_EMAIL;
import static utils.Constants.USER_ID;
import static utils.Constants.USER_IMAGE;
import static utils.Constants.USER_NAME;
import static utils.Constants.USER_STATUS;
import static utils.Constants.USER_TOKEN;
import static utils.DateUtils.getDate;
import static utils.DateUtils.rfc3339ToMills;
import static utils.WhatsNewStrings.WHATS_NEW1_TEXT;
import static utils.WhatsNewStrings.WHATS_NEW1_TITLE;

/**
 * Launcher Activity; Handles fragment changes;
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String KEY_SELECTED_NAV_MENU = "KEY_SELECTED_NAV_MENU";

    private SharedPreferences mSharedPreferences;
    private String mToken;
    private DrawerLayout mDrawer;
    private Handler mHandler;
    private NavigationView mNavigationView;
    private int mPreviousMenuItemId;
    private final int RESULT_PICK_CONTACT = 100;
    private static final String travelShortcut = "io.github.project_travel_mate.TravelShortcut";
    private static final String myTripsShortcut = "io.github.project_travel_mate.MyTripsShortcut";
    private static final String utilitiesShortcut = "io.github.project_travel_mate.UtilitiesShortcut";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPreferences.getString(USER_TOKEN, null);
        mHandler = new Handler(Looper.getMainLooper());

        DailyQuotesManager.checkDailyQuote(this);

        // To show what's new in our application
        WhatsNew whatsNew = WhatsNew.newInstance(
                new WhatsNewItem(WHATS_NEW1_TITLE, WHATS_NEW1_TEXT));
        whatsNew.setButtonBackground(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        whatsNew.setButtonTextColor(ContextCompat.getColor(this, R.color.white));
        whatsNew.presentAutomatically(this);

        // To check for shared profile intents
        String action = getIntent().getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            showProfileOrTrip(getIntent().getDataString());
        }

        //Initially city fragment
        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SELECTED_NAV_MENU)) {
            mPreviousMenuItemId = savedInstanceState.getInt(KEY_SELECTED_NAV_MENU);
            fragment = getFragmentByNavMenuItemId(mPreviousMenuItemId);
            defaultSelectedNavMenu(mPreviousMenuItemId);
        } else {
            fragment = CityFragment.newInstance();
            defaultSelectedNavMenu(R.id.nav_city);
            mPreviousMenuItemId = R.id.nav_city;
        }

        fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();

        // Get runtime permissions for Android M
        getRuntimePermissions();

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                mDrawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        String emailId = mSharedPreferences.getString(USER_EMAIL, getString(R.string.app_name));
        fillNavigationView(emailId, null);

        getProfileInfo();
        if (getIntent() != null && getIntent().getAction() != null) {
            switch (getIntent().getAction()) {
                case travelShortcut:
                    fragment = TravelFragment.newInstance();
                    fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();
                    break;
                case myTripsShortcut:
                    fragment = MyTripsFragment.newInstance();
                    fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();
                    break;
                case utilitiesShortcut:
                    fragment = UtilitiesFragment.newInstance();
                    fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();
                    break;
            }
        }
    }


    void defaultSelectedNavMenu(int resId) {
        mNavigationView = findViewById(R.id.nav_view);
        Menu menu = mNavigationView.getMenu();
        MenuItem menuItem = menu.findItem(resId);
        menuItem.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Change fragment on selecting naviagtion drawer item
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == mPreviousMenuItemId) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        int id = item.getItemId();
        mPreviousMenuItemId = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = getFragmentByNavMenuItemId(id);

        if (fragment != null) {
            fragmentManager.beginTransaction().replace(R.id.inc, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Fragment getFragmentByNavMenuItemId(int id) {

        Fragment fragment = null;

        switch (id) {
            case R.id.nav_home:
                fragment = HomeFragment.newInstance();
                break;

            case R.id.nav_travel:
                fragment = TravelFragment.newInstance();
                break;

            case R.id.nav_mytrips:
                fragment = MyTripsFragment.newInstance();
                break;

            case R.id.nav_city:
                fragment = CityFragment.newInstance();
                break;

            case R.id.nav_utility:
                fragment = UtilitiesFragment.newInstance();
                break;

            case R.id.nav_favourite:
                fragment = FavouriteCitiesFragment.newInstance();
                break;

            case R.id.nav_about_us:
                fragment = AboutUsFragment.newInstance();
                break;

            case R.id.nav_signout: {

                //set AlertDialog before signout
                ContextThemeWrapper crt = new ContextThemeWrapper(this, R.style.AlertDialog);
                AlertDialog.Builder builder = new AlertDialog.Builder(crt);
                builder.setMessage(R.string.signout_message)
                        .setPositiveButton(R.string.positive_button,
                                (dialog, which) -> {
                                    mSharedPreferences
                                            .edit()
                                            .putString(USER_TOKEN, null)
                                            .apply();
                                    Intent i = LoginActivity.getStartIntent(MainActivity.this);
                                    startActivity(i);
                                    finish();
                                })
                        .setNegativeButton(android.R.string.cancel,
                                (dialog, which) -> {

                                });
                builder.create().show();
                break;
            }

            case R.id.nav_myfriends:
                fragment = MyFriendsFragment.newInstance(this);
                break;
            case R.id.nav_settings:
                fragment = SettingsFragment.newInstance();
                break;
        }
        return fragment;
    }

    private void getRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.VIBRATE,
                }, 0);
            }
        }
    }

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }

    private void fillNavigationView(String emailId, String imageURL) {

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get reference to the navigation view header and email textview
        View navigationHeader = navigationView.getHeaderView(0);
        TextView emailTextView = navigationHeader.findViewById(R.id.email);
        emailTextView.setText(emailId);

        ImageView imageView = navigationHeader.findViewById(R.id.image);
        Picasso.with(MainActivity.this).load(imageURL).placeholder(R.drawable.icon_profile)
                .error(R.drawable.icon_profile).into(imageView);
        imageView.setOnClickListener(v -> startActivity(ProfileActivity.getStartIntent(MainActivity.this)));
    }

    private void getProfileInfo() {

        // to fetch user details
        String uri = API_LINK_V2 + "get-user";
        Log.v(TAG, "url=" + uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
                .header(AUTHORIZATION, "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "request Failed, message = " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = Objects.requireNonNull(response.body()).string();

                mHandler.post(() -> {
                    try {
                        JSONObject object = new JSONObject(res);
                        String userName = object.getString("username");
                        String firstName = object.getString("first_name");
                        String lastName = object.getString("last_name");
                        String status = object.getString("status");
                        int id = object.getInt("id");
                        String imageURL = object.getString("image");
                        String dateJoined = object.getString("date_joined");
                        Long dateTime = rfc3339ToMills(dateJoined);
                        String date = getDate(dateTime);

                        String fullName = firstName + " " + lastName;
                        mSharedPreferences.edit().putString(USER_NAME, fullName).apply();
                        mSharedPreferences.edit().putString(USER_EMAIL, userName).apply();
                        mSharedPreferences.edit().putString(USER_DATE_JOINED, date).apply();
                        mSharedPreferences.edit().putString(USER_IMAGE, imageURL).apply();
                        mSharedPreferences.edit().putString(USER_STATUS, status).apply();
                        mSharedPreferences.edit().putString(USER_ID, String.valueOf(id)).apply();
                        fillNavigationView(fullName, imageURL);

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing user JSON, " + e.getMessage());
                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillNavigationView(mSharedPreferences.getString(USER_NAME, getString(R.string.app_name)),
                mSharedPreferences.getString(USER_IMAGE, null));
        invalidateOptionsMenu();
    }

    void showProfileOrTrip(String data) {
        Uri uri = Uri.parse(data);
        String userId = uri.getQueryParameter(SHARE_PROFILE_USER_ID_QUERY);
        String myId = mSharedPreferences.getString(USER_ID, null);
        Log.v("user id", userId + " " + myId);
        if (userId != null) {
            int id = Integer.parseInt(userId);
            if (!userId.equals(myId)) {
                Intent intent = FriendsProfileActivity.getStartIntent(MainActivity.this, id);
                startActivity(intent);
            } else {
                Intent intent = ProfileActivity.getStartIntent(MainActivity.this);
                startActivity(intent);
            }
        } else {
            String tripID = uri.getQueryParameter(SHARE_TRIP_TRIP_ID_QUERY);
            if (tripID != null) {
                Log.v("trip id", tripID + " ");
                Trip trip = new Trip();
                trip.setId(tripID);
                Intent intent = MyTripInfoActivity.getStartIntent(MainActivity.this, trip, false);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_menu, menu);
        updateNotificationsCount(menu);
        return true;
    }

    public void updateNotificationsCount(Menu menu) {
        String uri;
        uri = API_LINK_V2 + "number-of-unread-notifications";
        Log.v(TAG, "url = " + uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
                .header(AUTHORIZATION, "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request Failed, message =" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = Objects.requireNonNull(response.body()).string();

                mHandler.post(() -> {
                    try {
                        JSONObject object = new JSONObject(res);
                        int mNotificationCount = object.getInt("number_of_unread_notifications");
                        if (mNotificationCount > 0) {
                            BadgeCounter.update(MainActivity.this,
                                    menu.findItem(R.id.action_notification),
                                    R.drawable.ic_notifications_white,
                                    BadgeCounter.BadgeColor.RED,
                                    mNotificationCount);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing notifications json, " + e.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notification:
                Intent intent = NotificationsActivity.getStartIntent(MainActivity.this);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_NAV_MENU, mPreviousMenuItemId);
    }

    public NavigationView getNavigationView() {
        return mNavigationView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    Cursor cursor = null;
                    try {
                        String phoneNo = null;
                        String name = null;
                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        name = cursor.getString(nameIndex);
                        phoneNo = cursor.getString(phoneIndex);

                        addFriends(name, phoneNo);


                        //Toast.makeText(this,name+" "+phoneNo,Toast.LENGTH_LONG).show();
                        //textView2.setText(phoneNo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }


    @Override
    public void onClick(View view, String contacts) {
        if (view.getId() == R.id.add_friend) {

            //Use Intent.ACTION_PICK in your Activity
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
        }

        if(view.getId() == R.id.my_friends_linear_layout){
            BottomDialogFragment bottomSheetDialog = BottomDialogFragment.getInstance(contacts);
            bottomSheetDialog.show(getSupportFragmentManager(), "Custom Bottom Sheet");
        }
    }

    public void addFriends(String name, String contact){
        DBManager dbManager = new DBManager(this);
        ContentValues values = new ContentValues();

        values.put("name",name+"");
        values.put("contact",contact+"");
        long ID = dbManager.Insert(values);
        if(ID>0){
            Toast.makeText(this, "Your friend is added...", Toast.LENGTH_SHORT).show();
            if(getFragmentRefreshListener()!=null){
                getFragmentRefreshListener().onRefresh();
            }
        }
        else{
            Toast.makeText(this, "Unable to add!!!", Toast.LENGTH_SHORT).show();
        }

    }

    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    private FragmentRefreshListener fragmentRefreshListener;

    public interface FragmentRefreshListener{
        void onRefresh();
    }
}
