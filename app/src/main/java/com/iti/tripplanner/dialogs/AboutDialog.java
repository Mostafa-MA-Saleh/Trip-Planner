package com.iti.tripplanner.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.iti.tripplanner.R;

public class AboutDialog extends Dialog {

    public AboutDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_about);
        TextView txtAppVersion = (TextView) findViewById(R.id.txt_app_version);
        setTitle("About");
        txtAppVersion.setText(R.string.app_version);
    }
}
