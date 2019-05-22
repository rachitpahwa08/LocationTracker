package com.assettracker;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.assettracker.models.Driver;

import java.util.List;

public class VehiclesAdapter extends RecyclerView.Adapter<VehiclesAdapter.ViewHolder> {

    private List<Driver> drivers;

    public VehiclesAdapter(List<Driver> drivers) {
        this.drivers = drivers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.vehicle_cards,viewGroup,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        viewHolder.registrationNumber.setText("Registration Number:"+drivers.get(i).getCarAssigned());
        viewHolder.driverName.setText("Driver Name:"+drivers.get(i).getDriverName());
        viewHolder.driverContact.setText("Contact No.:"+drivers.get(i).getMobile());
        viewHolder.stopOTP.setText("Stop OTP:"+drivers.get(i).getOtp());
        final int position=i;
        viewHolder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent =new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+drivers.get(position).getMobile()));
                view.getContext().startActivity(callIntent);
            }
        });
        viewHolder.track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Driver d=drivers.get(position);
                Intent i=new Intent(view.getContext(),OwnerActivity.class);
                Log.e("VehicleAdapter", "onClick: "+d.getCarAssigned());
                i.putExtra("driver",d);
                view.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView registrationNumber,driverName,driverContact,stopOTP;
        Button call,track;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            registrationNumber=(TextView)itemView.findViewById(R.id.car_rc);
            driverName=(TextView)itemView.findViewById(R.id.driver_name);
            driverContact=(TextView)itemView.findViewById(R.id.driver_mobile);
            call=(Button)itemView.findViewById(R.id.call_driver);
            track=(Button)itemView.findViewById(R.id.track_driver);
            stopOTP=(TextView)itemView.findViewById(R.id.stop_otp);
        }
    }
}
