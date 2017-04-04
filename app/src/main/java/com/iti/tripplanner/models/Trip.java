package com.iti.tripplanner.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import com.google.android.gms.maps.model.LatLng;
import com.iti.tripplanner.activities.DialogActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Keep
public class Trip implements Parcelable {

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

    public Trip() {
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

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getStartString() {
        return mStartingString;
    }

    public void setStartString(String startString) {
        this.mStartingString = startString;
    }

    public String getDestinationString() {
        return mDestinationString;
    }

    public void setDestinationString(String destinationString) {
        this.mDestinationString = destinationString;
    }

    public String getStartCoordinates() {
        return mStartCoordinates.latitude + "," + mStartCoordinates.longitude;
    }

    public void setStartCoordinates(String mStartCoordinates) {
        String s[] = mStartCoordinates.split(",");
        double lat = Double.parseDouble(s[0]);
        double lng = Double.parseDouble(s[1]);
        this.mStartCoordinates = new LatLng(lat, lng);
    }

    public String getDestinationCoordinates() {
        return mDestinationCoordinates.latitude + "," + mDestinationCoordinates.longitude;
    }

    public void setDestinationCoordinates(String mDestinationCoordinates) {
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

    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, getTimeInMillis(), createPendingIntent(context));
        } else {
            am.set(AlarmManager.RTC_WAKEUP, getTimeInMillis(), createPendingIntent(context));
        }
    }

    public void cancelAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(createPendingIntent(context));
    }

    public long getTimeInMillis() {
        Date date = null;
        try {
            date = new SimpleDateFormat("EEEE, MMM dd, yyyy 'At' hh':'mm a", Locale.getDefault()).parse(mTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return date.getTime();
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        this.mTime = time;
    }

    public boolean isDone() {
        return mDone;
    }

    public void setDone(boolean done) {
        this.mDone = done;
    }

    public boolean isRoundTrip() {
        return mRoundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        this.mRoundTrip = roundTrip;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
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
}
