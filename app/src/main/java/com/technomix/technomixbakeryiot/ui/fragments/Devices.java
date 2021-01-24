package com.technomix.technomixbakeryiot.ui.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.technomix.technomixbakeryiot.ui.models.DevicesViewModel;
import com.technomix.technomixbakeryiot.R;

public class Devices extends Fragment {

    private static DevicesViewModel mViewModel;
    private static RecyclerView mRecyclerView;
     public static DevicesItemListener devicesItemListener;
    private RecyclerView.LayoutManager mlayoutManager;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.devices_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init(){
        mViewModel = new ViewModelProvider(this).get(DevicesViewModel.class);
        mViewModel.init();
        devicesItemListener = new DevicesItemListener(getContext());
        mRecyclerView = getView().findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(false);
        mlayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mlayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mViewModel.getAdapter());
    }
    private static class DevicesItemListener implements View.OnClickListener{
        private final Context context;
        private DevicesItemListener(Context context) {
            this.context = context;
        }
        @Override
        public void onClick(View v) {
            int position =  mRecyclerView.getChildAdapterPosition(v);
            mViewModel.getAdapter().getClickedDevice(position);
        }
    }
}