package com.assettracker;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.assettracker.models.Owner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeOwner extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView name,email,address,contact;
    private SharedPreferences mPrefs;
    DatabaseReference mDatabase;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_owner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name=(TextView)findViewById(R.id.ownerName);
        email=(TextView)findViewById(R.id.ownerEmail);
        address=(TextView)findViewById(R.id.ownerAddress);
        contact=(TextView)findViewById(R.id.ownerPhone);
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        progressDialog=new ProgressDialog(HomeOwner.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
        loadData();
    }

    private void loadData() {
        mDatabase.child("Owner").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Owner owner=dataSnapshot.getValue(Owner.class);
                Log.e("Home Owner", "onDataChange: "+owner.getName());
                name.setText("Name:"+owner.getName());
                address.setText("Address:"+owner.getAddress());
                contact.setText("Phone:"+owner.getPhone());
                email.setText("Email:"+owner.getEmail());
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_owner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_driver) {
            // Handle the camera action
            Intent intent=new Intent(HomeOwner.this,AddDriver.class);
            startActivity(intent);
        } else if (id == R.id.add_vehicle) {
            Intent intent=new Intent(HomeOwner.this,AddVehicle.class);
            startActivity(intent);
        } else if (id == R.id.track_vehicle) {
            Intent intent=new Intent(HomeOwner.this,VehicleTrackList.class);
            startActivity(intent);

        } else if(id == R.id.assign_driver){
            Intent intent=new Intent(HomeOwner.this,AssignDriver.class);
            startActivity(intent);

        }
        else if (id == R.id.logout_owner) {
            new AlertDialog.Builder(HomeOwner.this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to Logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.clear();
                            editor.apply();
                            FirebaseAuth.getInstance().signOut();
                            Intent i=new Intent(HomeOwner.this,MainActivity.class);
                            startActivity(i);
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
