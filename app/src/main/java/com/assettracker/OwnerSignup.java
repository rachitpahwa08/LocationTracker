package com.assettracker;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.assettracker.models.Owner;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OwnerSignup extends AppCompatActivity {

    EditText name,email,address,phone,password,confirm_password;
    Button submit;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Owner owner;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_signup);
        name=(EditText)findViewById(R.id.owner_name);
        email=(EditText)findViewById(R.id.owner_email);
        address=(EditText)findViewById(R.id.owner_address);
        phone=(EditText)findViewById(R.id.owner_phone);
        password=(EditText)findViewById(R.id.owner_password);
        confirm_password=(EditText)findViewById(R.id.confirm_password);
        submit=(Button)findViewById(R.id.owner_signup);
        owner=new Owner();
        mAuth=FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance().getReference();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog=new ProgressDialog(OwnerSignup.this);
                progressDialog.setMessage("Signing Up");
                progressDialog.setCancelable(false);
                progressDialog.show();
                validateDetails();
            }
        });
    }

    private void validateDetails() {
        if(name.getText().toString().isEmpty())
        {
            name.setError("Name Cannot Be Empty");
            name.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(email.getText().toString().isEmpty())
        {
            email.setError("Email Cannot Be Empty");
            email.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(address.getText().toString().isEmpty())
        {
            address.setError("Address Cannot Be Empty");
            address.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(phone.getText().toString().isEmpty())
        {
            phone.setError("Phone Cannot Be Empty");
            phone.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(password.getText().toString().isEmpty())
        {
            password.setError("Password Cannot Be Empty");
            password.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(confirm_password.getText().toString().isEmpty())
        {
            confirm_password.setError("Confirm Password Cannot Be Empty");
            confirm_password.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())
        {
            email.setError("Invalid Email");
            email.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(!(phone.getText().toString().length()==10))
        {
            phone.setError("Invalid Phone");
            phone.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(!(password.getText().toString().equals(confirm_password.getText().toString())))
        {
            confirm_password.setError("Passwords do not matches");
            confirm_password.requestFocus();
            progressDialog.dismiss();
            return;
        }
        owner.setAddress(address.getText().toString());
        owner.setName(name.getText().toString());
        owner.setPhone(Long.valueOf(phone.getText().toString()));
        owner.setEmail(email.getText().toString());
       signUP(); 
    }

    private void signUP() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                .addOnCompleteListener(OwnerSignup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            mDatabase.child("Owner").child(task.getResult().getUser().getUid()).setValue(owner).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {   progressDialog.dismiss();
                                        new AlertDialog.Builder(OwnerSignup.this)
                                                .setTitle("Success")
                                                .setMessage("User created successfully,Please Login to Continue")
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent intent=new Intent(OwnerSignup.this,MainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        FirebaseAuth.getInstance().signOut();
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .show();
                                    }

                                }
                            });
                        }
                        else{
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                                progressDialog.dismiss();
                                password.setError("Weak Password");
                                password.requestFocus();
                            }catch(FirebaseAuthUserCollisionException e) {
                                progressDialog.dismiss();
                                email.setError("User Already Registered");
                                Log.e("OwnerLogin", e.getMessage());
                                email.requestFocus();
                            } catch(Exception e) {
                                progressDialog.dismiss();
                                Log.e("OwnerLogin", e.getMessage());

                            }
                        }
                    }
                });
    }
}
