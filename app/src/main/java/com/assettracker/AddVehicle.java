package com.assettracker;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.assettracker.models.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddVehicle extends AppCompatActivity {

    private EditText rc,carModel;
    private Button add;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rc=(EditText)findViewById(R.id.Rc);
        carModel=(EditText)findViewById(R.id.car_model);
        add=(Button)findViewById(R.id.add_car);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog=new ProgressDialog(AddVehicle.this);
                progressDialog.setMessage("Processing");
                progressDialog.setCancelable(false);
                progressDialog.show();
                checkValidation();
            }
        });
        Log.e("AddVehicle", "onCreate: "+mAuth.getCurrentUser().getUid());
    }

    private void checkValidation() {
        if(rc.getText().toString().isEmpty()){
            progressDialog.dismiss();
            rc.setError("Registration Number is Required");
            rc.requestFocus();
            return;
        }
        if(rc.getText().toString().length()!=10){
            progressDialog.dismiss();
            rc.setError("Invalid Registration Number");
            rc.requestFocus();
            return;
        }
        if(carModel.getText().toString().isEmpty()){
            progressDialog.dismiss();
            carModel.setError("Registration Number is Required");
            carModel.requestFocus();
            return;
        }
        mDatabase.child("Vehicles").child(rc.getText().toString().toUpperCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {  Log.e("Addvehicle", "onDataChange: "+dataSnapshot.getValue().toString() );
                    progressDialog.dismiss();
                    rc.setError("Car is Already Registered");
                    rc.requestFocus();
                    return;
                }
                else{
                    addCar();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addCar() {

        Vehicle vehicle=new Vehicle(rc.getText().toString().toUpperCase(),carModel.getText().toString(),false,mAuth.getCurrentUser().getUid());
        mDatabase.child("Vehicles").child(rc.getText().toString().toUpperCase()).setValue(vehicle).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                    Toast.makeText(AddVehicle.this,"Vehicle added Successfully",Toast.LENGTH_LONG).show();
                    Intent i=new Intent(AddVehicle.this,HomeOwner.class);
                    startActivity(i);
                    finish();
                }
                else{
                    new AlertDialog.Builder(AddVehicle.this)
                            .setTitle("Error")
                            .setMessage("Some Error Occurred,Try after sometime")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    progressDialog.dismiss();
                                    Intent i=new Intent(AddVehicle.this,MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }

                            })
                            .show();
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
