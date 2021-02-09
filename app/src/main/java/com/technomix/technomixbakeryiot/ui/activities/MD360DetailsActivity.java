/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.technomix.technomixbakeryiot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.technomix.technomixbakeryiot.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import com.technomix.technomixbakeryiot.ui.adapters.DiscoveredBluetoothDevice;
import com.technomix.technomixbakeryiot.ui.models.DeviceViewModel;

@SuppressWarnings("ConstantConditions")
public class MD360DetailsActivity extends AppCompatActivity {
	public static final String EXTRA_DEVICE = "no.nordicsemi.android.blinky.EXTRA_DEVICE";

	private DeviceViewModel viewModel;

	@BindView(R.id.device_item_model_name)  TextView temperature;
	@BindView(R.id.device_item_temperature) TextView modelName;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md360);
		ButterKnife.bind(this);

		final Intent intent = getIntent();
		final DiscoveredBluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
		final String deviceName = device.getName();
		final String deviceAddress = device.getAddress();

		final MaterialToolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle(deviceName != null ? deviceName : getString(R.string.unknown_device));
		toolbar.setSubtitle(deviceAddress);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Configure the view model.
		viewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
		viewModel.connect(device);

		// Set up views.
		final TextView ledState = findViewById(R.id.device_item_temperature);

		final TextView connectionState = findViewById(R.id.device_item_img_connected_status);
		final View content = findViewById(R.id.device_item_cardView);
		final View notSupported = findViewById(R.id.not_supported);


		viewModel.getConnectionState().observe(this, state -> {
			switch (state.getState()) {
				case CONNECTING:

					notSupported.setVisibility(View.GONE);
					connectionState.setText(R.string.state_connecting);
					break;
				case INITIALIZING:
					connectionState.setText(R.string.state_initializing);
					break;
				case READY:

					content.setVisibility(View.VISIBLE);
					onConnectionStateChanged(true);
					break;
				case DISCONNECTED:
					if (state instanceof ConnectionState.Disconnected) {
						final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) state;
						if (stateWithReason.isNotSupported()) {

							notSupported.setVisibility(View.VISIBLE);
						}
					}
					// fallthrough
				case DISCONNECTING:
					onConnectionStateChanged(false);
					break;
			}
		});
		viewModel.getTemperature().observe(this, temperature -> {
			ledState.setText(temperature.toString());
		});
		viewModel.getModelName().observe(this,
				modelName -> this.modelName.setText(modelName.toString()));
	}

	@OnClick(R.id.action_clear_cache)
	public void onTryAgainClicked() {
		viewModel.reconnect();
	}

	private void onConnectionStateChanged(final boolean connected) {
		temperature.setEnabled(connected);
		if (!connected) {
			temperature.setText("0");
			modelName.setText(R.string.button_unknown);
		}
	}
}
