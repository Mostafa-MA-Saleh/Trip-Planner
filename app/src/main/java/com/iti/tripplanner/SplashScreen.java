package com.iti.tripplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.daimajia.androidanimations.library.Techniques;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

import io.fabric.sdk.android.Fabric;

public class SplashScreen extends AwesomeSplash {

    FirebaseRemoteConfig mFirebaseRemoteConfig;

    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            new AlertDialog.Builder(this)
                    .setMessage("Sorry this app cannot run on your device because Google Play Services is outdated.")
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
            return false;
        }
        if (status != ConnectionResult.SUCCESS) {
            new AlertDialog.Builder(this)
                    .setMessage("Sorry this app cannot run on your device because Google Play Services could not be found.")
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }

    @Override
    public void initSplash(ConfigSplash configSplash) {

        Fabric.with(this, new Crashlytics());

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaults(R.xml.default_config);

        mFirebaseRemoteConfig.fetch(2)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        }
                    }
                });

        if (mFirebaseRemoteConfig.getBoolean("AltTheme")) {
            setTheme(R.style.Girly_NoActionBar);
            configSplash.setBackgroundColor(R.color.darkest_pink);
        } else {
            configSplash.setBackgroundColor(R.color.colorPrimaryDark);
        }

        configSplash.setAnimCircularRevealDuration(500);
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM);

        configSplash.setLogoSplash(R.drawable.logo);
        configSplash.setAnimLogoSplashDuration(500);
        configSplash.setAnimLogoSplashTechnique(Techniques.RollIn);

        configSplash.setTitleSplash("Trip Planner");
        configSplash.setTitleTextColor(android.R.color.white);
        configSplash.setTitleTextSize(50f);
        configSplash.setAnimTitleDuration(2000);
        configSplash.setTitleFont("fonts/mvboli.ttf");
        configSplash.setAnimTitleTechnique(Techniques.FadeInLeft);

    }

    @Override
    public void animationsFinished() {
        if (isGooglePlayServicesAvailable()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}