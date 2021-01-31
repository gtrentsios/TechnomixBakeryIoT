package com.technomix.technomixbakeryiot.data.charscallback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;
@SuppressWarnings("ConstantConditions")
public abstract class TemperatureDataCallback implements ProfileDataCallback, DevicesCalback {
    private  float mTemperature = 0;
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        final float temperature = data.getFloatValue(Data.FORMAT_FLOAT, 0);
        if (mTemperature !=temperature){
            mTemperature = temperature;
            onTemperatureChanged(device, mTemperature);
        }
    }
}
