package com.iti.tripplanner.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.iti.tripplanner.R;

import java.util.Arrays;
import java.util.List;

@SuppressLint("GoogleAppIndexingApiWarning")
public class LoginActivity extends AppCompatActivity {

    private static final int RQST_GOOGLE_SIGN_IN = 52204;
    private EditText mEmailText;
    private EditText mPasswordText;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Animation mShake;
    private CallbackManager mCallbackManager;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressDialog = new Dialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(new ProgressBar(this));
        //noinspection ConstantConditions
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        mShake = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
        mEmailText = (EditText) findViewById(R.id.txtemail);
        mPasswordText = (EditText) findViewById(R.id.txtpass);
        TextView btnRegister = (TextView) findViewById(R.id.btnRegister);
        TextView btnResetPassword = (TextView) findViewById(R.id.btnResetPassword);
        final Button SignInButton = (Button) findViewById(R.id.btnsignin);
        Button GoogleSignInButton = (Button) findViewById(R.id.btnsigningoogle);
        Button FacebookSignInButton = (Button) findViewById(R.id.btnsigninfacebook);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
        };

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset errors.
                mEmailText.setError(null);
                mPasswordText.setError(null);

                // Store values at the time of the login attempt.
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();

                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(password)) {
                    mPasswordText.setError(getString(R.string.error_field_required));
                    mPasswordText.startAnimation(mShake);
                    focusView = mPasswordText;
                    cancel = true;
                }

                if (TextUtils.isEmpty(email)) {
                    mEmailText.setError(getString(R.string.error_field_required));
                    mEmailText.startAnimation(mShake);
                    focusView = mEmailText;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                    return;
                }

                mProgressDialog.show();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                signInFailed(e.getLocalizedMessage());
                            }
                        });
            }
        });

        GoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(getString(R.string.web_client_id))
                        .build();

                GoogleApiClient GoogleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(GoogleApiClient);
                startActivityForResult(signInIntent, RQST_GOOGLE_SIGN_IN);
            }
        });

        FacebookSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                List<String> permissions = Arrays.asList("email", "public_profile");
                LoginManager loginManager = LoginManager.getInstance();
                loginManager.logInWithReadPermissions(LoginActivity.this, permissions);
                loginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                firebaseAuthWithFacebook(loginResult.getAccessToken());
                            }

                            @Override
                            public void onCancel() {
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                signInFailed(exception.getLocalizedMessage());
                            }
                        }
                );
            }
        });

        mPasswordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SignInButton.performClick();
                    return true;
                }
                return false;
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, PasswordResetActivity.class));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQST_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                signInFailed(result.getStatus().getStatusMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signInFailed(e.getLocalizedMessage());
                    }
                });
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signInFailed(e.getLocalizedMessage());
                    }
                });
    }

    private void signInFailed(String message) {
        if (message != null) {
            if (message.contains("email"))
                mEmailText.startAnimation(mShake);
            if (message.contains("pass"))
                mPasswordText.startAnimation(mShake);
        } else {
            message = "Unknown Error";
        }
        mProgressDialog.dismiss();
        Snackbar.make(findViewById(R.id.logo_text), message, Snackbar.LENGTH_LONG).show();
    }
}