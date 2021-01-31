package com.technomix.technomixbakeryiot.ui.activities;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.technomix.technomixbakeryiot.R;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MainActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback {
    private static final int BLUETOOTH = 1;
    private static final int BLUETOOTH_ADMIN = 2;
    private static final int ACCESS_LOCATION = 3;
    private static final int BACKGROUND_LOCATION = 4;
    private static NavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.MainView_nav_host_fragment);
        mNavController = navHostFragment.getNavController();


    }
    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermissions() &&  checkBlueToothPermissions()) {
            mNavController.navigate(R.id.action_mainFragment_to_scannerActivity);
        } else {
            final Button buttonRequestBluetoothAccess = findViewById(R.id.btnRequestBloutoothAccess);
            buttonRequestBluetoothAccess.setVisibility(checkBlueToothPermissions()?View.GONE:View.VISIBLE);
            final Button buttonRequestLocationAccess = findViewById(R.id.btnRequestLocation);
            buttonRequestLocationAccess.setVisibility(checkLocationPermissions()?View.GONE:View.VISIBLE);
        }
    }
    public void onClickRequestLocation(View view) {
        requestAccessLocationPermission();

    }
    public void onChickRequestBluetoothAccess(View view){
        requestBlueToothPermission();
    }
    /*
        Permissions methods
     */
    boolean checkLocationPermissions() {
        int mLocationPermission;
        int mBackgroundLocationPermission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            mLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mBackgroundLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            mBackgroundLocationPermission = PERMISSION_GRANTED;
        }

        if (mLocationPermission == PERMISSION_GRANTED && mBackgroundLocationPermission == PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    boolean checkBlueToothPermissions(){
        int mBluetoothPermission;
        int mBluetoothAdminPermission;
        mBluetoothPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        mBluetoothAdminPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
        if(mBluetoothPermission == PERMISSION_GRANTED && mBluetoothAdminPermission == PERMISSION_GRANTED){
            return  true;
        } else {
            return  false;
        }
    }
    void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //Android 10 and higher
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION);
            }
        }
    }

    void requestAccessLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //Android 10 and higher
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
            } else {
                requestBackgroundLocationPermission();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_LOCATION);

            } else {
                requestBackgroundLocationPermission();
            }
        }
    }

    void requestBlueToothPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, BLUETOOTH);
        } else {
            requestBlueToothAdminPermission();
        }
    }

    void requestBlueToothAdminPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADMIN}, BLUETOOTH_ADMIN);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        boolean permissionsGranted = true;
        int sMessageText = 0;
        if (grantResults[0] == PERMISSION_GRANTED) {
            switch (requestCode) {
                case ACCESS_LOCATION: {
                    requestBackgroundLocationPermission();
                }
                case BACKGROUND_LOCATION: {
                    sMessageText = R.string.permissionGrantedLocation;
                    Toast.makeText(this, sMessageText, Toast.LENGTH_SHORT).show();
                    checkAndNavigate();
                }
                case BLUETOOTH: {
                    requestBlueToothAdminPermission();
                }
                case BLUETOOTH_ADMIN: {
                    sMessageText = R.string.permissionGrantedLocation;
                    Toast.makeText(this, sMessageText, Toast.LENGTH_SHORT).show();
                    checkAndNavigate();
                }
            }

        }
    }
    void checkAndNavigate(){
        if (checkLocationPermissions() &&  checkBlueToothPermissions()) {
            mNavController.navigate(R.id.action_mainFragment_to_scannerActivity);
        }
    }
}