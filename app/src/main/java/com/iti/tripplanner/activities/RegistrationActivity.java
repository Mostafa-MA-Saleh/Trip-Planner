package com.iti.tripplanner.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.iti.tripplanner.R;

public class RegistrationActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private FirebaseAuth mAuth;

    private EditText mFullNameText;
    private EditText mEmailText;
    private EditText mPasswordText;
    private EditText mPasswordConfirmText;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getWindow().setWindowAnimations(android.R.style.Animation_Toast);

        mProgressDialog = new Dialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(new ProgressBar(this));
        //noinspection ConstantConditions
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        mFullNameText = (EditText) findViewById(R.id.txtfullname);
        mEmailText = (EditText) findViewById(R.id.txtemail);
        mPasswordText = (EditText) findViewById(R.id.txtpass);
        mPasswordConfirmText = (EditText) findViewById(R.id.txtpass_confirm);

        mFullNameText.setOnFocusChangeListener(this);
        mEmailText.setOnFocusChangeListener(this);
        mPasswordText.setOnFocusChangeListener(this);
        mPasswordConfirmText.setOnFocusChangeListener(this);

        Button mRegisterButton = (Button) findViewById(R.id.btnregister);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String fullName = mFullNameText.getText().toString();
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();
                String passwordConfirm = mPasswordConfirmText.getText().toString();

                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(passwordConfirm)) {
                    mPasswordConfirmText.setError(getString(R.string.error_field_required));
                    focusView = mPasswordConfirmText;
                    cancel = true;
                }

                if (TextUtils.isEmpty(password)) {
                    mPasswordText.setError(getString(R.string.error_field_required));
                    focusView = mPasswordText;
                    cancel = true;
                }

                if (!cancel && !password.equals(passwordConfirm)) {
                    mPasswordConfirmText.setError(getString(R.string.error_password_match));
                    focusView = mPasswordConfirmText;
                    cancel = true;
                }

                if (TextUtils.isEmpty(email)) {
                    mEmailText.setError(getString(R.string.error_field_required));
                    focusView = mEmailText;
                    cancel = true;
                } else if (!email.contains("@") || !email.contains(".")) {
                    mEmailText.setError(getString(R.string.error_invalid_email));
                    focusView = mEmailText;
                    cancel = true;
                }

                if (TextUtils.isEmpty(fullName)) {
                    mFullNameText.setError(getString(R.string.error_field_required));
                    focusView = mFullNameText;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    mProgressDialog.show();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    mProgressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        if (user != null) {
                                            user.updateProfile(new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(fullName).build());
                                            new AlertDialog.Builder(RegistrationActivity.this)
                                                    .setTitle("Registration Successful!")
                                                    .setMessage("Thank you for registering...")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    })
                                                    .show();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(findViewById(R.id.logo_text), e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        onFocusChange(mFullNameText, true);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.MainScrollRegistration);
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, scrollView.getHeight());
            }
        }, 500);
    }
}