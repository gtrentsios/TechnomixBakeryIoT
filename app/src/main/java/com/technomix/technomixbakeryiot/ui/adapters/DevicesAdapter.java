package com.technomix.technomixbakeryiot.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.technomix.technomixbakeryiot.R;
import com.technomix.technomixbakeryiot.data.ConnectedDevices;
import com.technomix.technomixbakeryiot.ui.activities.MainActivity;
import com.technomix.technomixbakeryiot.ui.fragments.Devices;

import java.util.ArrayList;


public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder> {
    private final ArrayList<ConnectedDevices> mDevices = new ArrayList<ConnectedDevices>();


    public static class DevicesViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewAddress;
        ImageView imageViewIcon;
        public DevicesViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.lbl_device_name);
            this.textViewAddress = (TextView) itemView.findViewById(R.id.lbl_device_address);
            this.imageViewIcon = (ImageView) itemView.findViewById(R.id.img_connected_device);
        }
    }
    @Override
    public DevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.connected_device_item, parent, false);
        DevicesViewHolder devicesViewHolder = new DevicesViewHolder(view);
        view.setOnClickListener(Devices.devicesItemListener);
        return devicesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesViewHolder holder, int position) {

        TextView textViewName = holder.textViewName;
        TextView textViewVersion = holder.textViewAddress;
        ImageView imageView = holder.imageViewIcon;

        textViewName.setText(mDevices.get(position).getDeviceName());
        textViewVersion.setText(mDevices.get(position).getDeviceAddress());
        //imageView.setImageResource(mDevices.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }
    public void addDevice(ConnectedDevices device){
        mDevices.add(device);
    }
    public ConnectedDevices getClickedDevice(int position){
        return mDevices.get(position);
    }

}
