package com.iti.tripplanner.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.iti.tripplanner.R;

public class PasswordResetActivity extends AppCompatActivity {

    private EditText mEmailText;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        getWindow().setWindowAnimations(android.R.style.Animation_Toast);

        mEmailText = (EditText) findViewById(R.id.txtemail);

        mProgressDialog = new Dialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(new ProgressBar(this));
        //noinspection ConstantConditions
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        final Button passResetButton = (Button) findViewById(R.id.btnResetPass);
        passResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = mEmailText.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    mEmailText.setError(getString(R.string.error_field_required));
                } else if (!email.contains("@") || !email.contains(".")) {
                    mEmailText.setError(getString(R.string.error_invalid_email));
                } else {
                    mProgressDialog.show();
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mProgressDialog.dismiss();
                                    if (task.isSuccessful())
                                        new AlertDialog.Builder(PasswordResetActivity.this)
                                                .setTitle(getString(R.string.reset_password))
                                                .setMessage("An email has been sent to " + email + " with further instructions on how to reset your password.")
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                })
                                                .show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    new AlertDialog.Builder(PasswordResetActivity.this)
                                            .setTitle(getString(R.string.reset_password))
                                            .setMessage(e.getLocalizedMessage())
                                            .setPositiveButton("Ok", null)
                                            .show();
                                }
                            });
                }
            }
        });

        mEmailText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    passResetButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }
}