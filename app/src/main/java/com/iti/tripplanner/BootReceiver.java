package com.iti.tripplanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

@SuppressWarnings("ConstantConditions")
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            DBAdapter.getDatabase()
                    .getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                                if (!postSnapshot.getKey().equals("Count")) {
                                    Trip trip = postSnapshot.getValue(Trip.class);
                                    if (trip.getTimeInMillis() > System.currentTimeMillis() && !trip.isDone())
                                        trip.setAlarm(context);
                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
    }
}
