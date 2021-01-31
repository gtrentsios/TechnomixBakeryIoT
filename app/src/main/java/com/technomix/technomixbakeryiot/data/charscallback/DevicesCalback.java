package com.technomix.technomixbakeryiot.data.charscallback;
import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
public interface DevicesCalback {

    void onTemperatureChanged(@NonNull final BluetoothDevice device, final float temp);

}
