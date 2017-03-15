package com.iti.tripplanner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DialogActivity extends AppCompatActivity {

    private final static int ALARM_NOTIFICATION_ID = 8000;

    private Ringtone mAlarm;
    private Vibrator mVibrator;
    private MediaPlayer mediaPlayer;
    private NotificationManager mNotificationManager;
    private Trip mTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        int mTripID = getIntent().getIntExtra("Trip", -1);

        if (mTripID == -1) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Sorry, there was an error retrieving the trip info..")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            DBAdapter.getDatabase()
                    .getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(String.valueOf(mTripID))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mTrip = dataSnapshot.getValue(Trip.class);
                            if (android.os.Build.MANUFACTURER.toLowerCase().equals("huawei")) {
                                mediaPlayer = MediaPlayer.create(DialogActivity.this, R.raw.chime);
                                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
                            } else {
                                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                                mAlarm = RingtoneManager.getRingtone(getApplicationContext(), alert);
                                mAlarm.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
                            }

                            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                            setTitle(mTrip.getName());
                            ((TextView) findViewById(R.id.TextDetails)).setText("You have a trip to " + mTrip.getDestinationString() + "!");
                            TextView btnStart = (TextView) findViewById(R.id.StartButton);
                            TextView btnLater = (TextView) findViewById(R.id.LaterButton);
                            TextView btnCancel = (TextView) findViewById(R.id.CancelButton);

                            PendingIntent pendingIntent = PendingIntent.getActivity(DialogActivity.this, 15500, new Intent(DialogActivity.this, DialogActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

                            Notification alarmNotification =
                                    new NotificationCompat.Builder(DialogActivity.this)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentIntent(pendingIntent)
                                            .setContentTitle(getString(R.string.app_name))
                                            .setContentText(mTrip.getName())
                                            .setTicker(getString(R.string.app_name))
                                            .setAutoCancel(false)
                                            .setOngoing(true)
                                            .build();

                            mNotificationManager.notify(ALARM_NOTIFICATION_ID, alarmNotification);

                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onBackPressed();
                                }
                            });

                            btnLater.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + mTrip.getDestinationCoordinates());
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    PendingIntent pendingIntent = PendingIntent.getActivity(DialogActivity.this, mTrip.get_id(), mapIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    Notification alarmNotification =
                                            new NotificationCompat.Builder(DialogActivity.this)
                                                    .setSmallIcon(R.mipmap.ic_launcher)
                                                    .setContentIntent(pendingIntent)
                                                    .setContentTitle(getString(R.string.app_name))
                                                    .setAutoCancel(true)
                                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                                    .setTicker(getString(R.string.app_name))
                                                    .setContentText("Tap here to start navigation for " + mTrip.getName())
                                                    .build();

                                    mNotificationManager.notify(mTrip.get_id(), alarmNotification);

                                    new AlertDialog.Builder(DialogActivity.this)
                                            .setMessage("A notification has been added to your notification tray tap on it when you're ready to start your trip")
                                            .setTitle(mTrip.getName())
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    onBackPressed();
                                                }
                                            })
                                            .show();
                                }
                            });

                            btnStart.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + mTrip.getDestinationCoordinates());
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    startActivity(mapIntent);
                                    onBackPressed();
                                }
                            });

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mVibrator.hasVibrator()) {
                                        long[] pattern = {0, 200, 50};
                                        mVibrator.vibrate(pattern, 0);
                                    }
                                    if (android.os.Build.MANUFACTURER.toLowerCase().equals("huawei")) {
                                        if (mediaPlayer != null) {
                                            mediaPlayer.start();
                                        }
                                    } else {
                                        if (mAlarm != null) {
                                            mAlarm.play();
                                        }
                                    }
                                }
                            }, 500);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        if (android.os.Build.MANUFACTURER.toLowerCase().equals("huawei")) {
            mediaPlayer.stop();
            mediaPlayer.release();
        } else {
            mAlarm.stop();
        }
        mNotificationManager.cancel(ALARM_NOTIFICATION_ID);
        mVibrator.cancel();
        mTrip.setDone(true);
        DBAdapter dbAdapter = new DBAdapter(FirebaseAuth.getInstance().getCurrentUser().getUid());
        dbAdapter.updateTrip(mTrip.get_id(), mTrip);
        super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return MotionEvent.ACTION_OUTSIDE != event.getAction();
    }
}
