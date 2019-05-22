package com.assettracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button driver,owner;
    private FirebaseAuth mAuth;

    private SharedPreferences mPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        driver=(Button)findViewById(R.id.driverSignin);
        owner=(Button)findViewById(R.id.ownerSignin);
        mAuth = FirebaseAuth.getInstance();
        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(MainActivity.this,LoginActivity.class);
                startActivity(i);
            }
        });
        owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent i=new Intent(MainActivity.this,OwnerLogin.class);
            startActivity(i);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        String loginvalue=mPrefs.getString("loginvalue","");
        Log.e("MainActivity", "onStart: "+loginvalue);
        if(currentUser!=null) {
            if (loginvalue.equals("driver")) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
                finish();
            }
            else if(loginvalue.equals("owner")){
                Intent i = new Intent(MainActivity.this, HomeOwner.class);
                startActivity(i);
                finish();
            }
        }

    }
}
