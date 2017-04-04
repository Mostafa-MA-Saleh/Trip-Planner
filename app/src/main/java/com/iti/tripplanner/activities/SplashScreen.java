package com.iti.tripplanner.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.crashlytics.android.Crashlytics;
import com.daimajia.androidanimations.library.Techniques;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.iti.tripplanner.R;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

import io.fabric.sdk.android.Fabric;

public class SplashScreen extends AwesomeSplash {

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

        configSplash.setBackgroundColor(R.color.colorPrimaryDark);

        configSplash.setAnimCircularRevealDuration(500);
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM);

        configSplash.setLogoSplash(R.drawable.logo);
        configSplash.setAnimLogoSplashDuration(500);
        configSplash.setAnimLogoSplashTechnique(Techniques.FlipInX);

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