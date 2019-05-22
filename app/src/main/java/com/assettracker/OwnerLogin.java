package com.assettracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class OwnerLogin extends AppCompatActivity {


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private TextView signUp;
    private View mProgressView;
    private View mLoginFormView;
    private FirebaseAuth mAuth;
    private SharedPreferences mPrefs;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_login);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        signUp=(TextView)findViewById(R.id.signup_text);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        Button mEmailSignInButton = (Button) findViewById(R.id.owner_login);
        signUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(OwnerLogin.this,OwnerSignup.class);
                startActivity(i);
            }
        });
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog=new ProgressDialog(OwnerLogin.this);
                progressDialog.setMessage("Signing In");
                progressDialog.setCancelable(false);
                progressDialog.show();
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

    }


    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            progressDialog.dismiss();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            // showProgress(true);

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("OwnerLogin", "createUserWithEmail:success");
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putString("loginvalue", "owner");
                        editor.putString("email",email);
                        editor.putString("password",password);
                        editor.apply();
                        progressDialog.dismiss();
                        Intent i = new Intent(OwnerLogin.this, HomeOwner.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("OwnerLogin", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(OwnerLogin.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthWeakPasswordException e) {
                            progressDialog.dismiss();
                            mPasswordView.setError("Weak Password");
                            mPasswordView.requestFocus();
                        } catch(FirebaseAuthInvalidCredentialsException e) {
                            progressDialog.dismiss();
                            if(e.getMessage().contains("The password is invalid"))
                            {
                                mPasswordView.setError("Incorrect Password");
                                mPasswordView.requestFocus();
                            }
                        }catch(FirebaseAuthUserCollisionException e) {
                            progressDialog.dismiss();
                            mEmailView.setError("User Already Registered");
                            Log.e("OwnerLogin", e.getMessage());
                            mEmailView.requestFocus();
                        } catch(Exception e) {
                            progressDialog.dismiss();
                            Log.e("OwnerLogin", e.getMessage());
                            if(e.getMessage().contains("There is no user record corresponding")) {
                                mEmailView.setError("User Not Registered");
                                mEmailView.requestFocus();
                                return;
                            }
                        }
                    }
                }
            });
      /*      mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("OwnerLogin", "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                SharedPreferences.Editor editor = mPrefs.edit();
                                editor.putString("loginvalue", "owner");
                                editor.apply();
                                Intent i = new Intent(OwnerLogin.this, HomeOwner.class);
                                startActivity(i);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("OwnerLogin", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(OwnerLogin.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });*/

            /*mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);*/
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}







