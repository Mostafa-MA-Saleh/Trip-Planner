package com.iti.tripplanner.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.iti.tripplanner.R;

public class SettingsActivity extends AppCompatActivity {

    static boolean mPremium = false;

    private RewardedVideoAd mAd;
    private ProgressDialog progressDialog;

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        progressDialog = new ProgressDialog(this);
        Button btnChangeLog = (Button) findViewById(R.id.btn_changelog);
        final SwitchCompat swtPremium = (SwitchCompat) findViewById(R.id.PremiumFeaturesSwitch);
        swtPremium.setChecked(mPremium);
        swtPremium.setEnabled(!mPremium);

        swtPremium.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    buttonView.setChecked(false);
                    new AlertDialog.Builder(SettingsActivity.this)
                            .setMessage("To enable premium features until the next app restart you'll have to watch a short video!")
                            .setTitle("Premium")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    initVideoAd(buttonView);
                                    progressDialog.setTitle("Loading Video..");
                                    progressDialog.setMessage("Please wait..");
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.show();
                                    buttonView.setOnCheckedChangeListener(null);
                                }
                            })
                            .show();
                }
            }
        });
        btnChangeLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AlertDialog aboutDialog = new AlertDialog.Builder(SettingsActivity.this)
                            .setMessage("Current Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + ".\n\n" + getString(R.string.changelog))
                            .setTitle(R.string.changelog_title)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("Ok", null)
                            .show();
                    doKeepDialog(aboutDialog);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void initVideoAd(final CompoundButton buttonView) {
        mAd = MobileAds.getRewardedVideoAdInstance(SettingsActivity.this);
        mAd.loadAd(getString(R.string.video_ad_unit_id), new AdRequest.Builder().build());
        mAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                progressDialog.dismiss();
                mAd.show();
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                mPremium = true;
                buttonView.setChecked(true);
                buttonView.setEnabled(false);
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                progressDialog.dismiss();
                new AlertDialog.Builder(SettingsActivity.this)
                        .setMessage("Sorry, but there isn't any available video right now. Please try again later.")
                        .setTitle("Sorry!")
                        .setPositiveButton("Ok", null)
                        .show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
