package com.iti.tripplanner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

class DBAdapter {

    private static FirebaseDatabase mDatabase;
    private static int mLastID;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mCounterRef;

    DBAdapter(String uid) {
        mDatabase = getDatabase();
        mDatabaseRef = mDatabase.getReference(uid);
        mCounterRef = mDatabaseRef.child("Count").getRef();
    }

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    void insertTrip(final Trip trip) {
        mCounterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer id = dataSnapshot.getValue(Integer.class);
                if (id == null) id = 0;
                mLastID = ++id;
                trip.set_id(id);
                mCounterRef.setValue(id);
                mDatabaseRef.child(String.valueOf(id)).setValue(trip);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void deleteTrip(int _id) {
        mDatabaseRef.child(String.valueOf(_id)).removeValue();
    }

    void updateTrip(Trip trip) {
        mDatabaseRef.child(String.valueOf(trip.get_id())).setValue(trip);
    }

    int getLastID() {
        return mLastID;
    }

    static void setLastID(int LastID) {
        DBAdapter.mLastID = LastID;
    }

}
