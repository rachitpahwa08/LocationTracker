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

import com.assettracker.models.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    private FirebaseAuth mAuth;
    private SharedPreferences mPrefs;
    // [END declare_auth]
    private DatabaseReference mDatabase;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText driverName,phone,driverAge;
    private Button signup;
    private View mProgressView;
    private View mLoginFormView;
    ProgressDialog progressDialog;
    Driver driver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        mDatabase= FirebaseDatabase.getInstance().getReference();
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        phone=(EditText)findViewById(R.id.mobile);
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        signup=(Button)findViewById(R.id.signup);
        driver=new Driver();
        signup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog=new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Processing");
                progressDialog.setCancelable(false);
                progressDialog.show();
                if(!(phone.getText().length()==10))
                {
                    progressDialog.dismiss();
                    phone.setError("Invalid Number");
                    phone.requestFocus();
                    return;
                }
                checkNumber();
/*                driver.setDriverAge(driverAge.getText().toString().trim());
                driver.setDriverName(driverName.getText().toString().trim());
                driver.setMobile(phone.getText().toString().trim());
                Log.e("LoginActivity","Driver"+driver.getDriverAge());
                Intent i=new Intent(LoginActivity.this,VerifyPhoneActivity.class);
                i.putExtra("driver",driver);
                i.putExtra("login","login");
                startActivity(i);*/
            }
        });
    }

    private void checkNumber()
    {
     mDatabase.child("Drivers").orderByChild("mobile").equalTo(phone.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if(dataSnapshot.exists()){
                 Log.e("LoginActivity",dataSnapshot.toString());
                 Log.e("LoginActivity",dataSnapshot.toString().substring(39,67));
                 if(dataSnapshot.child(dataSnapshot.toString().substring(39,67)).child("assign").getValue().equals(true))
                 {
                     progressDialog.dismiss();
                     driver.setMobile(phone.getText().toString().trim());
                     Intent i=new Intent(LoginActivity.this,VerifyPhoneActivity.class);
                     i.putExtra("driver",driver);
                     i.putExtra("login","login");
                     startActivity(i);
                 }
                 else{
                     progressDialog.dismiss();
                     phone.setError("Vehicle not assigned");
                     phone.requestFocus();
                     return;
                 }

         }
         else{
                 progressDialog.dismiss();
                 phone.setError("No Driver found");
                 phone.requestFocus();
                 return;
             }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });
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

