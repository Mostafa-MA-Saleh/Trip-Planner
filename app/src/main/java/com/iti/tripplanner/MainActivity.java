package com.iti.tripplanner;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static final int RQST_NEW_TRIP = 80;
    public static final int RQST_EDIT_TRIP = 60;
    public static final int RQST_VIEW_TRIP = 10;

    private AdView mAdView;

    private RecyclerAdapter mCurrentTripsAdapter;
    private RecyclerAdapter mPreviousTripsAdapter;
    private RecyclerView mTripsList;
    private FloatingActionButton mFloatingActionButton;

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseRemoteConfig.getInstance().getBoolean("AltTheme")) {
            setTheme(R.style.Girly_NoActionBar);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder()
                .addTestDevice("1798F5122D4DF503D56E37C2D7593933")
                .build());

        mTripsList = (RecyclerView) findViewById(R.id.TirpsList);
        mTripsList.setLayoutManager(new LinearLayoutManager(this));
        mTripsList.addItemDecoration(new DividerItemDecoration(mTripsList.getContext(), 1));
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), TripActivity.class), RQST_NEW_TRIP);
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_trips);
        ((NavigationMenuView) navigationView.getChildAt(0)).addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
        View navigationViewHeader = navigationView.getHeaderView(0);
        ((TextView) navigationViewHeader.findViewById(R.id.nav_header_name)).setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        ((TextView) navigationViewHeader.findViewById(R.id.nav_header_email_address)).setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        navigationView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                return true;
            }
        });
        navigationViewHeader.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                return true;
            }
        });
        navigationViewHeader.findViewById(R.id.SettingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawers();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        mCurrentTripsAdapter = new RecyclerAdapter(this);
        mPreviousTripsAdapter = new RecyclerAdapter(this);
        if (savedInstanceState == null) {
            final Dialog mProgressDialog = new Dialog(this);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setContentView(new ProgressBar(this));
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            DBAdapter.getDatabase()
                    .getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mCurrentTripsAdapter.clear();
                            mPreviousTripsAdapter.clear();
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                if (!postSnapshot.getKey().equals("Count")) {
                                    Trip trip = postSnapshot.getValue(Trip.class);
                                    if (trip.isDone())
                                        mPreviousTripsAdapter.add(trip, mPreviousTripsAdapter.getItemCount());
                                    else
                                        mCurrentTripsAdapter.add(trip, mCurrentTripsAdapter.getItemCount());
                                } else {
                                    DBAdapter.setLastID(postSnapshot.getValue(Integer.class) + 1);
                                }
                            }
                            mTripsList.setAdapter(mCurrentTripsAdapter);
                            mProgressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else {
            ArrayList<Trip> trips = savedInstanceState.getParcelableArrayList("CurrentTrips");
            for (Trip trip : trips) {
                mCurrentTripsAdapter.add(trip, mCurrentTripsAdapter.getItemCount());
            }
            trips = savedInstanceState.getParcelableArrayList("PreviousTrips");
            for (Trip trip : trips) {
                mPreviousTripsAdapter.add(trip, mPreviousTripsAdapter.getItemCount());
            }
            getSupportActionBar().setSubtitle(savedInstanceState.getString("Subtitle", ""));
            mTripsList.setAdapter(getSupportActionBar().getSubtitle().equals("Finished Trips") ? mPreviousTripsAdapter : mCurrentTripsAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFloatingActionButton.getLayoutParams();
            if (!SettingsActivity.mPremium) {
                mAdView.setVisibility(View.VISIBLE);
                params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mFloatingActionButton.setLayoutParams(params);
            } else {
                mAdView.setVisibility(View.GONE);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mFloatingActionButton.setLayoutParams(params);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("CurrentTrips", mCurrentTripsAdapter.getAllElements());
        outState.putParcelableArrayList("PreviousTrips", mPreviousTripsAdapter.getAllElements());
        if (getSupportActionBar().getSubtitle() != null)
            outState.putString("Subtitle", getSupportActionBar().getSubtitle().toString());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        mFloatingActionButton.setVisibility(View.VISIBLE);
        if (id == R.id.nav_sign_out) {
            FirebaseAuth.getInstance().signOut();
            for (Trip trip : mCurrentTripsAdapter.getAllElements())
                trip.cancelAlarm(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_trips) {
            mTripsList.setAdapter(mCurrentTripsAdapter);
            getSupportActionBar().setSubtitle("");
        } else if (id == R.id.nav_previous_trips) {
            mTripsList.setAdapter(mPreviousTripsAdapter);
            getSupportActionBar().setSubtitle("Finished Trips");
            mFloatingActionButton.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_stats) {
            startActivity(new Intent(this, TripsHistoryActivity.class));
        } else if (id == R.id.nav_about) {
            AlertDialog aboutDialog = null;
            try {
                aboutDialog = new AlertDialog.Builder(this)
                        .setMessage("Current Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + ".\n\n" + getString(R.string.changelog))
                        .setTitle(getString(R.string.about) + "!")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("Ok", null)
                        .show();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            doKeepDialog(aboutDialog);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Trip trip = data.getParcelableExtra("Trip");
            if (requestCode == RQST_NEW_TRIP) {
                mCurrentTripsAdapter.add(trip, mCurrentTripsAdapter.getItemCount());
            } else if (requestCode == RQST_EDIT_TRIP) {
                if (!trip.isDone())
                    mCurrentTripsAdapter.update(trip, data.getIntExtra("Position", -1));
                else
                    mPreviousTripsAdapter.update(trip, data.getIntExtra("Position", -1));
            } else if (requestCode == RQST_VIEW_TRIP) {
                if (trip.isDone()) {
                    mPreviousTripsAdapter.add(trip, mPreviousTripsAdapter.getItemCount());
                    mCurrentTripsAdapter.remove(data.getIntExtra("Position", -1));
                } else {
                    mCurrentTripsAdapter.add(trip, mCurrentTripsAdapter.getItemCount());
                    mPreviousTripsAdapter.remove(data.getIntExtra("Position", -1));
                }
            }
        }
    }
}
