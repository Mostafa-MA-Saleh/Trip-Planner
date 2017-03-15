package com.iti.tripplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Keep
@IgnoreExtraProperties
class Trip implements Parcelable {

    public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel source) {
            return new Trip(source);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    private int _id;
    private String mName;
    private String mStartingString;
    private String mDestinationString;
    private LatLng mStartCoordinates;
    private LatLng mDestinationCoordinates;
    private String mTime;
    private boolean mDone;
    private boolean mRoundTrip;
    private String mNotes;

    Trip() {
        mName = "";
        mStartingString = "";
        mDestinationString = "";
        mTime = "";
        mDone = false;
        mRoundTrip = false;
        mNotes = "";
    }

    private Trip(Parcel in) {
        _id = in.readInt();
        mName = in.readString();
        mStartingString = in.readString();
        mDestinationString = in.readString();
        mStartCoordinates = in.readParcelable(LatLng.class.getClassLoader());
        mDestinationCoordinates = in.readParcelable(LatLng.class.getClassLoader());
        mTime = in.readString();
        mDone = in.readByte() != 0;
        mRoundTrip = in.readByte() != 0;
        mNotes = in.readString();
    }

    int get_id() {
        return _id;
    }

    void set_id(int _id) {
        this._id = _id;
    }

    String getName() {
        return mName;
    }

    void setName(String name) {
        mName = name;
    }

    String getStartString() {
        return mStartingString;
    }

    void setStartString(String startString) {
        this.mStartingString = startString;
    }

    String getDestinationString() {
        return mDestinationString;
    }

    void setDestinationString(String destinationString) {
        this.mDestinationString = destinationString;
    }

    String getStartCoordinates() {
        return mStartCoordinates.latitude + "," + mStartCoordinates.longitude;
    }

    void setStartCoordinates(String mStartCoordinates) {
        String s[] = mStartCoordinates.split(",");
        double lat = Double.parseDouble(s[0]);
        double lng = Double.parseDouble(s[1]);
        this.mStartCoordinates = new LatLng(lat, lng);
    }

    String getDestinationCoordinates() {
        return mDestinationCoordinates.latitude + "," + mDestinationCoordinates.longitude;
    }

    void setDestinationCoordinates(String mDestinationCoordinates) {
        String s[] = mDestinationCoordinates.split(",");
        double lat = Double.parseDouble(s[0]);
        double lng = Double.parseDouble(s[1]);
        this.mDestinationCoordinates = new LatLng(lat, lng);
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("Trip", this.get_id());
        return PendingIntent.getActivity(context, get_id(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, getTimeInMillis(), createPendingIntent(context));
    }

    void cancelAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(createPendingIntent(context));
    }

    long getTimeInMillis() {
        Date date = null;
        try {
            date = new SimpleDateFormat("EEEE, MMM dd, yyyy 'At' hh':'mm a", Locale.getDefault()).parse(mTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    String getTime() {
        return mTime;
    }

    void setTime(String time) {
        this.mTime = time;
    }

    boolean isDone() {
        return mDone;
    }

    void setDone(boolean done) {
        this.mDone = done;
    }

    boolean isRoundTrip() {
        return mRoundTrip;
    }

    void setRoundTrip(boolean roundTrip) {
        this.mRoundTrip = roundTrip;
    }

    String getNotes() {
        return mNotes;
    }

    void setNotes(String notes) {
        this.mNotes = notes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(mName);
        dest.writeString(mStartingString);
        dest.writeString(mDestinationString);
        dest.writeParcelable(mStartCoordinates, flags);
        dest.writeParcelable(mDestinationCoordinates, flags);
        dest.writeString(mTime);
        dest.writeByte(mDone ? (byte) 1 : (byte) 0);
        dest.writeByte(mRoundTrip ? (byte) 1 : (byte) 0);
        dest.writeString(mNotes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trip trip = (Trip) o;

        if (mDone != trip.mDone) return false;
        if (mRoundTrip != trip.mRoundTrip) return false;
        if (mName != null ? !mName.equals(trip.mName) : trip.mName != null) return false;
        if (mStartingString != null ? !mStartingString.equals(trip.mStartingString) : trip.mStartingString != null)
            return false;
        if (mDestinationString != null ? !mDestinationString.equals(trip.mDestinationString) : trip.mDestinationString != null)
            return false;
        if (mStartCoordinates != null ? !mStartCoordinates.equals(trip.mStartCoordinates) : trip.mStartCoordinates != null)
            return false;
        if (mDestinationCoordinates != null ? !mDestinationCoordinates.equals(trip.mDestinationCoordinates) : trip.mDestinationCoordinates != null)
            return false;
        if (mTime != null ? !mTime.equals(trip.mTime) : trip.mTime != null) return false;
        return mNotes != null ? mNotes.equals(trip.mNotes) : trip.mNotes == null;

    }
}
