package com.assettracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.assettracker.models.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddDriver extends AppCompatActivity {

    private EditText driverName,phone,driverAge;

    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;
    Driver driver;
    SharedPreferences mPrefs;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_driver);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        phone=(EditText)findViewById(R.id.driver_phone);
        driverName=(EditText)findViewById(R.id.driver_name);
        driverAge=(EditText)findViewById(R.id.driver_age);
        Button addDriver=(Button)findViewById(R.id.addDriver);
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        mDatabase=FirebaseDatabase.getInstance().getReference();
        driver=new Driver();
        addDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog=new ProgressDialog(AddDriver.this);
                progressDialog.setMessage("Processing");
                progressDialog.setCancelable(false);
                progressDialog.show();
                if(phone.getText().toString().isEmpty()){
                    progressDialog.dismiss();
                    phone.setError("Phone Number is Required");
                    phone.requestFocus();
                    return;
                }
                if(!(phone.getText().toString().length()==10)){
                    progressDialog.dismiss();
                    phone.setError("Invalid Phone Number");
                    phone.requestFocus();
                    return;
                }
                if(driverName.getText().toString().isEmpty()){
                    progressDialog.dismiss();
                    driverName.setError("Name is Required");
                    driverName.requestFocus();
                    return;
                }
                if(driverAge.getText().toString().isEmpty()){
                    progressDialog.dismiss();
                    driverAge.setError("Age is Required");
                    driverAge.requestFocus();
                    return;
                }
                mDatabase.child("Drivers").orderByChild("mobile").equalTo(phone.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            progressDialog.dismiss();
                            phone.setError("User Already Registered");
                            phone.requestFocus();
                            return;
                        }
                        else{
                            driver.setDriverAge(driverAge.getText().toString().trim());
                            driver.setDriverName(driverName.getText().toString().trim());
                            driver.setMobile(phone.getText().toString().trim());
                            driver.setOwnerUID(firebaseUser.getUid());
                            driver.setAssign(false);
                            driver.setOtp(0);
                            progressDialog.dismiss();
                            Intent i=new Intent(AddDriver.this,VerifyPhoneActivity.class);
                            i.putExtra("driver",driver);
                            i.putExtra("login","nologin");
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
