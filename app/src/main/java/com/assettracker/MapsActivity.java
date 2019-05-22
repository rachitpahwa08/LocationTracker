package com.assettracker;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.assettracker.Interfaces.RetrofitInterface;
import com.assettracker.models.Result;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST = 1;
    private static String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private GoogleMap mMap;
    private Button stop,threedot,start,cancelOtp,submitOtp;
    private Snackbar mSnackbarPermissions;
    private Snackbar mSnackbarGps;
    private SharedPreferences mPrefs;
    EditText otp;
    LinearLayout otplayout;
    DatabaseReference mDatabase;
    LatLng destination;
    Polyline finalPolyline;
    ProgressDialog progressDialog;
    private static Retrofit.Builder builder1=new Retrofit.Builder().baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create());
    public static Retrofit retrofit1=builder1.build();
    RetrofitInterface retrofitInterface1=retrofit1.create(RetrofitInterface.class);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        stop=(Button)findViewById(R.id.stopLocation);
        threedot=(Button)findViewById(R.id.threedot);
        start=(Button)findViewById(R.id.navigation);
        otplayout=(LinearLayout)findViewById(R.id.otp_layout);
        submitOtp=(Button)findViewById(R.id.otp_submit);
        cancelOtp=(Button)findViewById(R.id.otp_cancel);
        otp=(EditText)findViewById(R.id.otp);
        mPrefs = getSharedPreferences("login", MODE_PRIVATE);
        mDatabase= FirebaseDatabase.getInstance().getReference();
        otplayout.setVisibility(View.GONE);
        progressDialog=new ProgressDialog(MapsActivity.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        checkLocationPermission();

        if (isServiceRunning(TrackerService.class)) {
            // If service already running, simply update UI.
            setTrackingStatus(R.string.tracking);
            start.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
        }
        getDestination();
        stop.setVisibility(View.GONE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

      //  setUpMapIfNeeded();
        cancelOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                otp.setText("");
                otp.setError(null);
                otplayout.setVisibility(View.GONE);
            }
        });
        submitOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                SharedPreferences mPrefs2=getSharedPreferences("login",MODE_PRIVATE);
                String vehicle=mPrefs2.getString("carNumber","");

                if(otp.getText().toString().isEmpty())
                {
                    progressDialog.dismiss();
                    otp.setError("This field cannot be empty");
                    otp.requestFocus();
                    return;
                }
                if(!(otp.getText().toString().length()==6))
                {
                    progressDialog.dismiss();
                    otp.setError("Invalid OTP");
                    otp.requestFocus();
                    return;
                }
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                mDatabase.child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("otp").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            Log.e("MapsActivity", "onDataChange: "+dataSnapshot.getValue().toString());
                            if(otp.getText().toString().equals(dataSnapshot.getValue().toString()))
                            {
                                stopLocationService();
                                otplayout.setVisibility(View.GONE);
                            }
                            else{
                                progressDialog.dismiss();
                                otp.setError("Wrong OTP");
                                otp.requestFocus();
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //confirmStop();
                otplayout.setVisibility(View.VISIBLE);
                /*Intent i=new Intent(MapsActivity.this,TrackerActivity.class);
                startActivity(i);
                finish();*/
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences mPrefs2=getSharedPreferences("login",MODE_PRIVATE);
                String vehicle=mPrefs2.getString("carNumber","");
                Random rnd = new Random();
                int n = 100000 + rnd.nextInt(900000);
                mDatabase.child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("otp").setValue(n).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            startLocationService();
                        }
                    }
                });

            }
        });
        threedot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });


    }

    private void getDestination() {
        SharedPreferences mPrefs2=getSharedPreferences("login",MODE_PRIVATE);
        String vehicle=mPrefs2.getString("carNumber","");
        Log.e("MapsActivity", "getDestination: "+vehicle);
        mDatabase.child("Vehicles").child(vehicle).child("destination").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    destination=new LatLng(dataSnapshot.child("lat").getValue(Double.class),dataSnapshot.child("long").getValue(Double.class));
                    Log.e("MapsActivity", "onDataChange: "+destination.toString() );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMenu() {
        PopupMenu popup = new PopupMenu(MapsActivity.this, threedot);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(MapsActivity.this,"You Clicked : " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
               if(menuItem.getTitle().equals("Logout"))
               {
                   new AlertDialog.Builder(MapsActivity.this)
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
                                   Intent i=new Intent(MapsActivity.this,MainActivity.class);
                                   startActivity(i);
                                   finish();
                               }

                           })
                           .setNegativeButton("No", null)
                           .show();
               }
                return true;
            }
        });
        popup.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
             //   mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("It's Me!"));
                if(destination!=null)
                {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                    getRouteMarker(destination,latLng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination,9));
                }
                else{
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                    mMap.animateCamera(cameraUpdate);
                }

            }
        });


        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    }
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkLocationPermission() {
        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int storagePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (locationPermission != PackageManager.PERMISSION_GRANTED
                || storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST);
        } else {
            loadMap();
            checkGpsEnabled();
        }
    }

    private void checkGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            reportGpsError();
        } else {
            resolveGpsError();

        }
    }

    private void startLocationService() {
        // Before we start the service, confirm that we have extra power usage privileges.
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        Intent intent = new Intent();
        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
        startService(new Intent(this, TrackerService.class));

    }

    private void stopLocationService() {
        SharedPreferences mPrefs2=getSharedPreferences("login",MODE_PRIVATE);
        String vehicle=mPrefs2.getString("carNumber","");
        start.setVisibility(View.VISIBLE);
        stop.setVisibility(View.GONE);
        stopService(new Intent(this, TrackerService.class));
        mMap.clear();
        mDatabase.child("Vehicles").child(vehicle).child("assign").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    mDatabase.child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("assign").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {   SharedPreferences.Editor editor = mPrefs.edit();
                                editor.clear();
                                editor.apply();
                                progressDialog.dismiss();
                                FirebaseAuth.getInstance().signOut();
                                Intent i=new Intent(MapsActivity.this,MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    });
                }
            }
        });

    }

    private void confirmStop() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.confirm_stop))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void loadMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private void reportGpsError() {

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.map1), getString(R.string
                        .gps_required), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id
                .snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();

    }

    private void resolveGpsError() {
        if (mSnackbarGps != null) {
            mSnackbarGps.dismiss();
            mSnackbarGps = null;
        }
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           setTrackingStatus(intent.getIntExtra(getString(R.string.status), 0));
        }
    };


    private void setTrackingStatus(int status) {
        boolean tracking = status == R.string.tracking;

        start.setVisibility((tracking ? View.INVISIBLE : View.VISIBLE));
        stop.setVisibility((tracking ? View.VISIBLE : View.INVISIBLE));
        Log.e("MapsActivity", "setTrackingStatus: "+tracking );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            // We request storage perms as well as location perms, but don't care
            // about the storage perms - it's just for debugging.
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        reportPermissionsError();
                    } else {
                        resolvePermissionsError();
                        checkGpsEnabled();
                    }
                }
            }
        }
    }

    private void reportPermissionsError() {
     /*   if (mSwitch != null) {
            mSwitch.setChecked(false);
        }*/
        Snackbar snackbar = Snackbar
                .make(
                        findViewById(R.id.rootView),
                        getString(R.string.location_permission_required),
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(android.provider.Settings
                                .ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(
                android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void resolvePermissionsError() {
        if (mSnackbarPermissions != null) {
            mSnackbarPermissions.dismiss();
            mSnackbarPermissions = null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(TrackerService.STATUS_INTENT));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
    private void getRouteMarker(LatLng pickUpLatLng, LatLng start) {

        Map<String, String> map = new HashMap<>();
        map.put("origin", String.valueOf(start.latitude) + "," + String.valueOf(start.longitude));
        map.put("destination", String.valueOf(pickUpLatLng.latitude) + "," + String.valueOf(pickUpLatLng.longitude));
        map.put("mode", "driving");
        map.put("key", "API Key");
        Call<Result> call1 = retrofitInterface1.getpoint(map);
        call1.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Log.e("MapsActivity", "onResponse: Mapfragment" + new Gson().toJson(response.body()));
                Result result = response.body();
                int size = result.getRoutes().get(0).getLegs().get(0).getSteps().size();
                String[] polyline = new String[size];
                for (int i = 0; i < size; i++) {
                    polyline[i] = result.getRoutes().get(0).getLegs().get(0).getSteps().get(i).getPolyline().getPoints();
                }
                for (int j = 0; j < size; j++) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.BLUE);
                    polylineOptions.width(20);
                    polylineOptions.addAll(PolyUtil.decode(polyline[j]));
                    finalPolyline=mMap.addPolyline(polylineOptions);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.e("MapsActivity", "onFailure:MapFragment " + t.getMessage());
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
