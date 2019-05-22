package com.assettracker;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.assettracker.models.Driver;
import com.assettracker.models.PlaceInfo;
import com.assettracker.models.Vehicle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.services.common.FirebaseInfo;

public class AssignDriver extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    Spinner driver,cars;
    Button assign;
    ArrayAdapter<String> adapter;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    List<String> driverList,carList;
    List<Driver> drivers;
    List<Vehicle> vehicle;
    ProgressDialog progressDialog;
    HashMap<String, Double> location;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private AutoCompleteTextView site_landmark;
    private PlaceInfo mPlace;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_driver);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        driver=(Spinner)findViewById(R.id.driver_spinner);
        cars=(Spinner)findViewById(R.id.car_spinner);
        assign=(Button)findViewById(R.id.assign_driver);
        site_landmark=(AutoCompleteTextView) findViewById(R.id.site_landmark);
        location=new HashMap<>();
        init();
        driverList=new ArrayList<>();
        carList=new ArrayList<>();
        drivers=new ArrayList<Driver>();
        vehicle=new ArrayList<>();
        progressDialog=new ProgressDialog(AssignDriver.this);
        progressDialog.setMessage("Processing");
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mAuth= FirebaseAuth.getInstance();
        getvalues();
        assign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Loading");
                progressDialog.setCancelable(false);
                progressDialog.show();
                if(driverList.get(0).equals("No Driver Found")||carList.get(0).equals("No Vehicle Found"))
                {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(AssignDriver.this)
                            .setTitle("Error")
                            .setMessage("No driver or vehicle is present to assign")
                            .setPositiveButton("Ok", null)
                            .show();
                }
                else{
                    mDatabase.child("Drivers").child(drivers.get(driver.getSelectedItemPosition()).getUid()).child("assign").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mDatabase.child("Drivers").child(drivers.get(driver.getSelectedItemPosition()).getUid()).child("carAssigned").setValue(vehicle.get(cars.getSelectedItemPosition()).getRegistrationNumber().toUpperCase()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            if(mPlace.getLatlng()!=null)
                                            {
                                                location.put("lat",mPlace.getLatlng().latitude);
                                                location.put("long",mPlace.getLatlng().longitude);
                                                Log.e("Add Site", "Lat Long "+mPlace.getLatlng().latitude+" "+mPlace.getLatlng().longitude);
                                            }
                                            else{
                                                site_landmark.setError("Please select landmark available from suggestions");
                                                site_landmark.requestFocus();
                                                return;
                                            }
                                            mDatabase.child("Vehicles").child(vehicle.get(cars.getSelectedItemPosition()).getRegistrationNumber().toUpperCase()).child("assign").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(AssignDriver.this,"Driver Assigned to vehicle",Toast.LENGTH_LONG).show();
                                                    mDatabase.child("Vehicles").child(vehicle.get(cars.getSelectedItemPosition()).getRegistrationNumber().toUpperCase()).child("destination").setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {
                                                                progressDialog.dismiss();
                                                                Intent i=new Intent(AssignDriver.this,HomeOwner.class);
                                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(i);
                                                                finish();
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("AssignDriver", "onConnectionFailed: "+connectionResult.getErrorMessage() );
    }

    private void getvalues() {
        mDatabase.child("Drivers").orderByChild("ownerUID").equalTo(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    {
                        String check=dataSnapshot1.child("assign").getValue().toString();
                        if(check.equals("false")) {
                            Log.e("Assign Driver", "onDataChange: " + dataSnapshot1.getValue().toString());
                            drivers.add(dataSnapshot1.getValue(Driver.class));
                        }
                    }
                    for(int i=0;i<drivers.size();i++)
                    {
                        driverList.add(drivers.get(i).getDriverName());
                    }
                    if(driverList.isEmpty())
                    {
                        driverList.add("No Driver Found");
                    }
                     adapter = new ArrayAdapter<String>(AssignDriver.this,
                            android.R.layout.simple_spinner_item, driverList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    driver.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.child("Vehicles").orderByChild("ownerId").equalTo(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                    {
                        String check=dataSnapshot1.child("assign").getValue().toString();
                        if(check.equals("false")){
                        Log.e("Assign Driver", "onDataChange: "+dataSnapshot1.getValue().toString()+dataSnapshot1.child("assign").getValue());
                        vehicle.add(dataSnapshot1.getValue(Vehicle.class));
                        }
                    }
                    for(int i=0;i<vehicle.size();i++)
                    {
                        carList.add(vehicle.get(i).getRegistrationNumber().toUpperCase());
                    }
                    if(carList.isEmpty())
                    {
                        carList.add("No Vehicle Found");
                    }
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(AssignDriver.this,
                            android.R.layout.simple_spinner_item, carList);
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cars.setAdapter(adapter1);
                    progressDialog.dismiss();
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

    private void geoLocate(){
        Log.d("AddSite", "geoLocate: geolocating");

        String searchString = site_landmark.getText().toString();

        Geocoder geocoder = new Geocoder(AssignDriver.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e("AddSite", "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d("AddSite", "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

        }
    }
    private void init(){
        Log.d("AddSite", "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        Log.e("AddSite", "api connected" );
        site_landmark.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        site_landmark.setAdapter(mPlaceAutocompleteAdapter);

        site_landmark.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });
        hideSoftKeyboard();}

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };



    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d("AddSite", "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d("AddSite", "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d("AddSite", "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d("AddSite", "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d("AddSite", "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d("AddSite", "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d("AddSite", "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d("AddSite", "onResult: website uri: " + place.getWebsiteUri());

                Log.d("AddSite", "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e("AddSite", "onResult: NullPointerException: " + e.getMessage() );
            }

           /* moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace.getName());*/

            places.release();
        }
    };
}
