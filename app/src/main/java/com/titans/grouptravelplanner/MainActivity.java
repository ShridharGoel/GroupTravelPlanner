package com.titans.grouptravelplanner;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcoscg.dialogsheet.DialogSheet;
import com.titans.grouptravelplanner.utils.Config;
import com.titans.grouptravelplanner.utils.UserHelper;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {

    private static final int POS_DASHBOARD = 0;
    private static final int POS_FORUM = 1;
    private static final int POS_SEND_MESSAGE = 2;
    private static final int POS_FRIENDS = 3;
    private static final int POS_ABOUT = 4;
    private static final int POS_LOGOUT = 6;
    public static String userId;
    private boolean mState=true;
    private boolean mStateForum=false;
    private SlidingRootNav slidingRootNav;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private UserHelper userHelper;
    public static CircleImageView imageView;

    public static TextView username;
    private AuthCredential credential;
    public static MainActivity activity;
    public static Fragment mCurrentFragment;
    public static FirebaseUser currentuser;
    public static Toolbar toolbar;
    DrawerAdapter adapter;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    public static void startActivity(Context context) {
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());


        setContentView(R.layout.activity_main);
        activity=this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Dashboard");
        try {
            getSupportActionBar().setTitle("Dashboard");
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }

        userHelper = new UserHelper(this);
        firestore = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();

        if (currentuser == null) {
            Login.startActivityy(this);
            finish();

        } else {

            mCurrentFragment = new HomeFragment();
            userId = currentuser.getUid();
            slidingRootNav = new SlidingRootNavBuilder(this)
                    .withToolbarMenuToggle(toolbar)
                    .withMenuOpened(false)
                    .withContentClickableWhenMenuOpened(false)
                    .withSavedState(savedInstanceState)
                    .withMenuLayout(R.layout.activity_main_drawer)
                    .inject();

            screenIcons = loadScreenIcons();
            screenTitles = loadScreenTitles();

            adapter = new DrawerAdapter(Arrays.asList(
                    createItemFor(POS_DASHBOARD).setChecked(true),
                    createItemFor (POS_FORUM),
                    createItemFor (POS_SEND_MESSAGE),
                    createItemFor(POS_FRIENDS),
                    createItemFor(POS_ABOUT),
                    createItemFor(POS_LOGOUT)));
            adapter.setListener(this);

            RecyclerView list = findViewById(R.id.list);
            list.setNestedScrollingEnabled(false);
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(adapter);

            adapter.setSelected(POS_DASHBOARD);
            setUserProfile();
        }
    }

    @NonNull
    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }
    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(Color.parseColor("#989898"))
                .withTextTint(Color.parseColor("#989898"))
                .withSelectedIconTint(color(R.color.colorAccentt))
                .withSelectedTextTint(color(R.color.colorAccentt));
    }
    private void setUserProfile() {

        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
        String imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

        if (!rs.isClosed()) {
            rs.close();
        }

        username = findViewById(R.id.username);
        imageView = findViewById(R.id.profile_image);
        username.setText(nam);
        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_profile_picture))
                .load(imag)
                .into(imageView);

    }
    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    @Override
    public void onItemSelected(int position) {

        Fragment selectedScreen;
        switch (position) {

            case POS_DASHBOARD:
                toolbar.setTitle("Dashboard");
                try {
                    getSupportActionBar().setTitle("Dashboard");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=true;
                mStateForum=false;
                showFragment(new HomeFragment());

                slidingRootNav.closeMenu(true);

                return;

            case POS_FORUM:
                toolbar.setTitle("Forum");
                try {
                    getSupportActionBar().setTitle("Forum");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=false;
                mStateForum=true;
//                selectedScreen = new Forum();
//                showFragment(selectedScreen);
                showFragment(new HomeFragment());

                slidingRootNav.closeMenu(true);

                return;

            case POS_SEND_MESSAGE:

                if(currentuser.isEmailVerified()) {
                    toolbar.setTitle("Flash Messages");
                    try {
                        getSupportActionBar().setTitle("Flash Messages");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
//                    selectedScreen = new FlashMessage();
//                    showFragment(selectedScreen);
                    showFragment(new HomeFragment());

                    slidingRootNav.closeMenu(true);
                }
                else{
                    showDialog();
                }

                return;

            case POS_FRIENDS:

                if(currentuser.isEmailVerified()) {
                    toolbar.setTitle("Manage Friends");
                    try {
                        getSupportActionBar().setTitle("Manage Friends");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
//                    selectedScreen = new FriendsFragment();
//                    showFragment(selectedScreen);
                    showFragment(new HomeFragment());

                    slidingRootNav.closeMenu(true);
                }
                else{
                    showDialog();
                }
                return;

            case POS_ABOUT:

                if(currentuser.isEmailVerified()) {

                    toolbar.setTitle("About");
                    try {
                        getSupportActionBar().setTitle("About");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
//                    selectedScreen = new About();
//                    showFragment(selectedScreen);
                    showFragment(new HomeFragment());

                    slidingRootNav.closeMenu(true);
                }
                else{
                    showDialog();
                }
                return;


            case POS_LOGOUT:

                if (currentuser != null) {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("Are you sure do you want to logout from this account?")
                            .positiveText("Yes")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    logout();
                                    dialog.dismiss();
                                }
                            }).negativeText("No")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();

                } else {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("A technical occurred while logging you out, Check your network connection and try again.")
                            .positiveText("Done")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();

                }

                return;

            default:

                showFragment(new HomeFragment());
        }

        slidingRootNav.closeMenu(true);

    }
    public void logout() {
        //performUploadTask();
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);

        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_ids", FieldValue.arrayRemove(pref.getString("regId","")));

        firestore.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                userHelper.deleteContact(1);
                mAuth.signOut();
                Login.startActivityy(MainActivity.this);
                mDialog.dismiss();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(MainActivity.this, "Error logging out", Toasty.LENGTH_SHORT,true).show();
                mDialog.dismiss();
                Log.e("Logout Error", e.getMessage());
            }
        });

    }
    public static void showFragment(Fragment fragment) {
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        mCurrentFragment=fragment;
    }
    public void showDialog() {

        new DialogSheet(this)
                .setTitle("Information")
                .setMessage("Email has not been verified, please verify and continue. If you have verified we recommend you to logout and login again")
                .setPositiveButton("Send again", v -> mAuth.getCurrentUser().sendEmailVerification()
                        .addOnSuccessListener(aVoid -> Toasty.success(MainActivity.this, "Verification email sent", Toasty.LENGTH_SHORT, true).show())
                        .addOnFailureListener(e -> Log.e("Error", e.getMessage())))
                .setNegativeButton("Ok", new DialogSheet.OnNegativeClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .setCancelable(true)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .show();


    }
    public void onViewProfileClicked(View view) {

        toolbar.setTitle("My Profile");
        try {
            getSupportActionBar().setTitle("My Profile");
        }catch (Exception e){
            Log.e("Error",e.getMessage());
        }
        this.invalidateOptionsMenu();
        mState=false;
        showFragment(new ProfileFragment());
        slidingRootNav.closeMenu(true);
    }
}