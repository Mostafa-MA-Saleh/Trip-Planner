package com.iti.tripplanner;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("ConstantConditions")
public class TripDetailsActivity extends AppCompatActivity {

    private TextView txtDuration;
    private ProgressBar progDuration;
    private Trip mTrip;
    private String duration;
    private boolean mChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        duration = "";
        mTrip = getIntent().getParcelableExtra("Trip");
        TextView txtStart = (TextView) findViewById(R.id.Start);
        TextView txtDestination = (TextView) findViewById(R.id.Destination);
        TextView txtTime = (TextView) findViewById(R.id.Time);
        txtDuration = (TextView) findViewById(R.id.Duration);
        TextView txtNotes = (TextView) findViewById(R.id.Notes);
        progDuration = (ProgressBar) findViewById(R.id.DurationProgress);

        setTitle(mTrip.getName());
        String startCoordinates = mTrip.getStartCoordinates();
        String destCoordinates = mTrip.getDestinationCoordinates();
        if (savedInstanceState != null && !savedInstanceState.getString("Duration", "").equals("")) {
            new DurationTask().onPostExecute(savedInstanceState.getString("Duration"));
        } else {
            new DurationTask().execute(startCoordinates, destCoordinates);
        }

        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

        SpannableString spannableString = new SpannableString("Start: " + mTrip.getStartString());
        spannableString.setSpan(boldSpan, 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtStart.setText(spannableString, TextView.BufferType.SPANNABLE);

        spannableString = new SpannableString("Destination: " + mTrip.getDestinationString());
        spannableString.setSpan(boldSpan, 0, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtDestination.setText(spannableString, TextView.BufferType.SPANNABLE);

        spannableString = new SpannableString("Time: " + mTrip.getTime());
        spannableString.setSpan(boldSpan, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtTime.setText(spannableString, TextView.BufferType.SPANNABLE);

        txtNotes.setText(mTrip.getNotes());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trip_details_menu, menu);
        SwitchCompat swtchDone = (SwitchCompat) menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchForActionBar);
        swtchDone.setChecked(mTrip.isDone());
        swtchDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mChanged = !(isChecked == mTrip.isDone());
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(duration))
            outState.putString("Duration", duration);
    }

    @Override
    public void onBackPressed() {
        if (mChanged) {
            mTrip.setDone(!mTrip.isDone());
            DBAdapter dbAdapter = new DBAdapter(FirebaseAuth.getInstance().getCurrentUser().getUid());
            dbAdapter.updateTrip(mTrip);

            mTrip.set_id(dbAdapter.getLastID());

            Intent data = new Intent()
                    .putExtra("Trip", mTrip)
                    .putExtra("Position", getIntent().getIntExtra("Position", -1));

            if (mTrip.getTimeInMillis() <= System.currentTimeMillis() || mTrip.isDone()) {
                mTrip.cancelAlarm(getApplicationContext());
            } else {
                mTrip.setAlarm(getApplicationContext());
            }
            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    private class DurationTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + params[0] + "&destination=" + params[1] + "&key=AIzaSyBdKV8BgBxsEiDjArDdRRPO4xXLFbcil3Y");
                HttpURLConnection HttpConn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(HttpConn.getInputStream(), "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONArray routesArray = jsonObject.getJSONArray("routes");
                JSONObject route = routesArray.getJSONObject(0);
                JSONArray legsArray = route.getJSONArray("legs");
                JSONObject legs = legsArray.getJSONObject(0);
                JSONObject duration = legs.getJSONObject("duration");
                return duration.getString("text");
            } catch (JSONException | IOException e) {
                return "Unable to retrieve duration";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            SpannableStringBuilder text = new SpannableStringBuilder()
                    .append("Duration: ")
                    .append(s);

            text.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            progDuration.setVisibility(View.INVISIBLE);
            txtDuration.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            txtDuration.setText(text, TextView.BufferType.SPANNABLE);
            duration = s;
        }
    }
}
