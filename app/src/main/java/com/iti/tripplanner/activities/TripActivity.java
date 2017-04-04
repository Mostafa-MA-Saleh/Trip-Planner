package com.iti.tripplanner.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.iti.tripplanner.R;
import com.iti.tripplanner.models.Trip;
import com.iti.tripplanner.utilities.DatabaseAdapter;
import com.simplicityapks.reminderdatepicker.lib.ReminderDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TripActivity extends AppCompatActivity implements TextWatcher {

    private final int START_REQUEST_CODE = 1;
    private final int DESTINATION_REQUEST_CODE = 2;

    private Trip trip;

    private boolean mChanged;

    private EditText txtTripName;
    private EditText txtTripStart;
    private LatLng coStart;
    private EditText txtTripDestination;
    private LatLng coDestination;
    private ReminderDatePicker rdpTripTime;
    private EditText txtTripNotes;
    private Switch swtchRoundTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_trip);
        txtTripName = (EditText) findViewById(R.id.TripName);
        txtTripStart = (EditText) findViewById(R.id.TripStart);
        txtTripDestination = (EditText) findViewById(R.id.TripDestinitaion);
        rdpTripTime = (ReminderDatePicker) findViewById(R.id.TripDateTime);
        txtTripNotes = (EditText) findViewById(R.id.TripNotes);
        swtchRoundTrip = (Switch) findViewById(R.id.RoundTripSwitch);

        txtTripName.addTextChangedListener(this);
        txtTripStart.addTextChangedListener(this);
        txtTripDestination.addTextChangedListener(this);
        txtTripNotes.addTextChangedListener(this);
        swtchRoundTrip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mChanged = true;
            }
        });

        rdpTripTime.setDateFormat(new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()));
        rdpTripTime.setTimeFormat(new SimpleDateFormat(" 'At' hh':'mm a", Locale.getDefault()));

        if ((trip = getIntent().getParcelableExtra("Trip")) != null) {
            txtTripName.setText(trip.getName());
            txtTripStart.setText(trip.getStartString());
            String[] coordinates = trip.getStartCoordinates().split(",");
            coStart = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            txtTripDestination.setText(trip.getDestinationString());
            coordinates = trip.getDestinationCoordinates().split(",");
            coDestination = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            Calendar tripDateTime = Calendar.getInstance();
            tripDateTime.setTimeInMillis(trip.getTimeInMillis());
            rdpTripTime.setSelectedDate(tripDateTime);
            txtTripNotes.setText(trip.getNotes());
            swtchRoundTrip.setChecked(trip.isRoundTrip());
        } else {
            trip = new Trip();
            rdpTripTime.setSelectedDate(Calendar.getInstance());
        }


        mChanged = false;

        txtTripStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PlaceTouch(event, v);
            }
        });

        txtTripDestination.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PlaceTouch(event, v);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            if (requestCode == START_REQUEST_CODE) {
                txtTripStart.setText(place.getAddress());
                coStart = place.getLatLng();
            } else if (requestCode == DESTINATION_REQUEST_CODE) {
                txtTripDestination.setText(place.getAddress());
                coDestination = place.getLatLng();
            }
        }
    }

    protected boolean PlaceTouch(MotionEvent event, View view) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            ((EditText) view).setError(null);
            try {
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                        .setBoundsBias(new LatLngBounds(new LatLng(21.747202, 37.325463), new LatLng(32.136654, 23.881997)))
                        .build(TripActivity.this);
                startActivityForResult(intent, view.getId() == R.id.TripStart ? START_REQUEST_CODE : DESTINATION_REQUEST_CODE);
            } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                Log.e("Error", "GooglePlayServicesNotAvailableException");
            }
        }
        return false;
    }

    protected void setTripData() {
        trip.setName(txtTripName.getText().toString());
        trip.setStartString(txtTripStart.getText().toString());
        if (coStart != null)
            trip.setStartCoordinates(coStart.latitude + "," + coStart.longitude);
        trip.setDestinationString(txtTripDestination.getText().toString());
        if (coDestination != null)
            trip.setDestinationCoordinates(coDestination.latitude + "," + coDestination.longitude);
        Date tripDate = rdpTripTime.getSelectedDate().getTime();
        trip.setTime(rdpTripTime.getCustomDateFormat().format(tripDate) + rdpTripTime.getTimeFormat().format(tripDate));
        trip.setNotes(txtTripNotes.getText().toString());
        trip.setRoundTrip(swtchRoundTrip.isChecked());
    }

    protected void saveTrip() {
        boolean cancel = false;

        if (TextUtils.isEmpty(txtTripName.getText().toString())) {
            txtTripName.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (TextUtils.isEmpty(txtTripStart.getText().toString())) {
            txtTripStart.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (TextUtils.isEmpty(txtTripDestination.getText().toString())) {
            txtTripDestination.setError(getString(R.string.error_field_required));
            cancel = true;
        }

        if (!cancel) {
            setTripData();
            if ((getIntent().getParcelableExtra("Trip")) == null) {
                DatabaseAdapter.getInstance().insertTrip(trip);
                trip.set_id(DatabaseAdapter.getInstance().getLastID());
            } else {
                DatabaseAdapter.getInstance().updateTrip(trip);
            }
            if (trip.getTimeInMillis() > System.currentTimeMillis() && !trip.isDone())
                trip.setAlarm(getApplicationContext());
            Intent data = new Intent();
            data.putExtra("Trip", trip);
            data.putExtra("Position", getIntent().getIntExtra("Position", -1));
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveTrip();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        setTripData();
        if (mChanged) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Trip Changed!")
                    .setMessage("Would you like to save the changes you have made?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveTrip();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TripActivity.super.onBackPressed();
                        }
                    }).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mChanged = true;
    }
}
