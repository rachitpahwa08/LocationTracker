package com.assettracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.assettracker.models.Driver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText verificationCode;
    private String mVerificationId;
    private Button verifyButton;
    private SharedPreferences mPrefs;
    private DatabaseReference mDatabase;
    private Driver driver;
    String status;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i=getIntent();
        driver=i.getParcelableExtra("driver");
        status=i.getStringExtra("login");
        Log.e("VerifyPhone", "onCreate: "+driver.getAssign());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        verificationCode=(EditText)findViewById(R.id.verification_code);
        verifyButton=(Button)findViewById(R.id.verify_button);
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        Log.e("VerifyPhoneActivity","Driver"+driver.getMobile().length());
        sendVerificationCode(driver.getMobile());
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog=new ProgressDialog(VerifyPhoneActivity.this);
                progressDialog.setMessage("Processing");
                progressDialog.setCancelable(false);
                progressDialog.show();
                if(verificationCode.getText().toString().isEmpty() ||verificationCode.getText().toString().length()<6 ){
                    progressDialog.dismiss();
                    verificationCode.setError("Enter Valid Code");
                    verificationCode.requestFocus();
                    return;
                }
                verifyVerificationCode(verificationCode.getText().toString());
            }
        });
    }

    private void sendVerificationCode(String mobile) {
        Log.e("VerifyPhoneActivity", "called mobile:"+mobile );
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                progressDialog=new ProgressDialog(VerifyPhoneActivity.this);
                progressDialog.setMessage("Processing");
                progressDialog.setCancelable(false);
                progressDialog.show();
                verificationCode.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
        }
    };


    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyPhoneActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            driver.setUid(task.getResult().getUser().getUid());
                            //verification successful we will start the profile activity
                            if(!status.equals("login")) {
                                mDatabase.child("Drivers").child(task.getResult().getUser().getUid()).setValue(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(VerifyPhoneActivity.this, "User Registered", Toast.LENGTH_LONG).show();
                                            Toast.makeText(VerifyPhoneActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                            String email=mPrefs.getString("email","");
                                            String password=mPrefs.getString("password","");
                                            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(VerifyPhoneActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if(task.isSuccessful())
                                                    {   progressDialog.dismiss();
                                                        Intent intent = new Intent(VerifyPhoneActivity.this, HomeOwner.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(VerifyPhoneActivity.this, "Error", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                                else {


                                mDatabase.child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("carAssigned").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists())
                                        {
                                            Log.e("VerifyPhoneActivity", "onDataChange: "+ dataSnapshot.getValue(String.class));
                                            SharedPreferences.Editor editor = mPrefs.edit();
                                            editor.putString("loginvalue", "driver");
                                            editor.putString("carNumber",dataSnapshot.getValue(String.class));
                                            editor.apply();
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(VerifyPhoneActivity.this, MapsActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                progressDialog.dismiss();
                                Intent intent = new Intent(VerifyPhoneActivity.this, MapsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Toast.makeText(VerifyPhoneActivity.this, message, Toast.LENGTH_LONG).show();

                        }
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
