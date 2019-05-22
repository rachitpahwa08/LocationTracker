package com.assettracker;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.assettracker.models.Driver;
import com.assettracker.models.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VehicleTrackList extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    List<Vehicle> vehicles;
    List<Driver> drivers;
    RecyclerView trackList;
    LinearLayoutManager linearLayoutManager;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_track_list);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        trackList=(RecyclerView)findViewById(R.id.recyclerViewTrackList);
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        vehicles=new ArrayList<>();
        drivers=new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(VehicleTrackList.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        trackList.setLayoutManager(linearLayoutManager);
        progressDialog=new ProgressDialog(VehicleTrackList.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
        getData();
    }

    private void getData() {
        mDatabase.child("Drivers").orderByChild("ownerUID").equalTo(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    {
                        String check=dataSnapshot1.child("assign").getValue().toString();
                        Log.e("VehicleTracklist", "onDataChange: check:"+check);
                        if(check.equals("true"))
                        {
                            Driver d=dataSnapshot1.getValue(Driver.class);
                            drivers.add(d);
                        }
                    }
                    VehiclesAdapter vehiclesAdapter=new VehiclesAdapter(drivers);
                    trackList.setAdapter(vehiclesAdapter);
                    progressDialog.dismiss();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getVehicleData() {
        mDatabase.child("Vehicles").orderByChild("ownerId").equalTo(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists())
               {
                   for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                   {
                       String check=dataSnapshot1.child("assign").getValue().toString();
                       Log.e("VehicleTracklist", "onDataChange: check:"+check);
                       if(check.equals("true"))
                       {
                           Vehicle v=dataSnapshot1.getValue(Vehicle.class);
                           vehicles.add(v);
                       }
                   }
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
