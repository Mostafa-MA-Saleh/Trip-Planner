package com.iti.tripplanner.utilities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iti.tripplanner.models.Trip;

@SuppressWarnings("WeakerAccess")
public class DatabaseAdapter {

    private static int mLastID;
    private static DatabaseAdapter mInstance;

    private FirebaseDatabase mDatabase;
    private FirebaseUser currentUser;

    private DatabaseAdapter() {
    }

    public static DatabaseAdapter getInstance() {
        if (mInstance == null)
            mInstance = new DatabaseAdapter();
        return mInstance;
    }

    public FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public void insertTrip(final Trip trip) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            getDatabase()
                    .getReference(currentUser.getUid())
                    .child("Counter")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Integer id = dataSnapshot.getValue(Integer.class);
                            if (id == null) id = 0;
                            mLastID = ++id;
                            trip.set_id(id);
                            getDatabase().getReference(currentUser.getUid()).child("Count").setValue(id);
                            getDatabase().getReference(currentUser.getUid()).child(String.valueOf(id)).setValue(trip);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    public void deleteTrip(int _id) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        getDatabase()
                .getReference(currentUser.getUid())
                .child(String.valueOf(_id))
                .removeValue();
    }

    public void updateTrip(Trip trip) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        getDatabase()
                .getReference(currentUser.getUid())
                .child(String.valueOf(trip.get_id()))
                .setValue(trip);
    }

    public int getLastID() {
        return mLastID;
    }

    public static void setLastID(int LastID) {
        DatabaseAdapter.mLastID = LastID;
    }

}
