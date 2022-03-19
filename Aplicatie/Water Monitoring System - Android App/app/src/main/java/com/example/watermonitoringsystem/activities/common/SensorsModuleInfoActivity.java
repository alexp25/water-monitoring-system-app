package com.example.watermonitoringsystem.activities.common;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.supplier.AddCoordinateToExistingSensor;
import com.example.watermonitoringsystem.adapters.SensorsAdapter;
import com.example.watermonitoringsystem.api.ApiManager;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.models.app.ChannelsData;
import com.example.watermonitoringsystem.models.MqttDataFormat;
import com.example.watermonitoringsystem.models.sqldb.CurrentSensorData;
import com.example.watermonitoringsystem.mqtt.SensorDataListener;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Channels data activity, updated in real time - common for supplier and customer
 *
 * @author Ioan-Alexandru Chirita
 */
public class SensorsModuleInfoActivity extends AppCompatActivity {

    public final static String CUSTOMER_CODE = "SensorsModuleInfoActivity-customer_code_response";
    public final static String SENSOR_ID = "SensorsModuleInfoActivity-sensor_id_response";
    private String userType;
    private ArrayList<ChannelsData> sensorChannelsDataList;
    private SensorsAdapter sensorAdapter;
    public static SensorDataListener sensorsDataFromMQTT;

    private int sensorId;
    private String customerCode;

    private ListView sensorsListView;
    private EditText customerCodeValueET;
    private TextView sensorIdValueTV;
    private ImageButton confirmCCButton;
    private ImageButton cancelCCButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userType = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_USER_TYPE, SensorsModuleInfoActivity.this);

        setContentView(R.layout.sensors_module_info_activity);

        // Toolbar
        AppBarLayout appBarLayout = findViewById(R.id.sensor_info_app_bar_layout);
        Toolbar mainToolbar;
        Toolbar supplierToolbar = appBarLayout.findViewById(R.id.toolbar_supplier);
        Toolbar customerToolbar = appBarLayout.findViewById(R.id.toolbar_customer);


        // Update Toolbar and NavigationView regarding to the user type = SUPPLIER / CUSTOMER
        if (userType.equals(Constants.SUPPLIER)) {
            // Remove toolbar for customer and set toolbar for supplier
            appBarLayout.removeView(customerToolbar);
            mainToolbar = supplierToolbar;
        } else {
            // Remove toolbar for supplier and set toolbar for customer
            appBarLayout.removeView(supplierToolbar);
            mainToolbar = customerToolbar;
        }

        // Toolbar
        setSupportActionBar(mainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        mainToolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (userType.equals(Constants.SUPPLIER)) {
            // Get notifications number
            ImageView redSquare = findViewById(R.id.red_square);
            TextView notificationNumber = findViewById(R.id.notifications_number);
            Utils.getNotificationsNumber(redSquare, notificationNumber);

            // Notification Bell
            ImageView notificationsBellImg = findViewById(R.id.notificationBellImg);
            notificationsBellImg.setOnClickListener(Utils::openNotificationsPopUp);
        }

        // Get data send between activities
        Bundle b = getIntent().getExtras();
        sensorId = Integer.parseInt(b.getString(getString(R.string.sensor_id_field)));
        customerCode = b.getString(getString(R.string.customer_code_field));

        confirmCCButton = findViewById(R.id.btnConfirmCustomerCode);
        confirmCCButton.setOnClickListener(x -> {
            Toast.makeText(this, "Customer code changed from " + customerCode + " to " + customerCodeValueET.getText(), Toast.LENGTH_SHORT).show();
            customerCode = AddCoordinateToExistingSensor.saveCustomerCodeToFirebase(sensorId, customerCodeValueET.getText().toString());
            confirmCCButton.setVisibility(View.INVISIBLE);
            cancelCCButton.setVisibility(View.INVISIBLE);
        });
        cancelCCButton = findViewById(R.id.btnCancelCustomerCode);
        cancelCCButton.setOnClickListener(x -> customerCodeValueET.setText(customerCode));

        sensorsListView = findViewById(R.id.sensors_module_list_view);
        sensorIdValueTV = findViewById(R.id.sensorIdValue);
        customerCodeValueET = findViewById(R.id.customerCodeValue);
        customerCodeValueET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals(customerCode)){
                    confirmCCButton.setVisibility(View.INVISIBLE);
                    cancelCCButton.setVisibility(View.INVISIBLE);
                } else {
                    confirmCCButton.setVisibility(View.VISIBLE);
                    cancelCCButton.setVisibility(View.VISIBLE);
                }

            }
        });
        if (userType.equals(Constants.CUSTOMER)) {
            customerCodeValueET.setFocusable(false);
            customerCodeValueET.setEnabled(false);
        }

        sensorChannelsDataList = new ArrayList<>();

        // Get sensor's channels information from MySQL database
        getSensorChannelsDataFromDatabase();

        // Action on click on a channel => Open channel history graphic activity
        sensorsListView.setOnItemClickListener((parent, view, position, id) -> openSensorChannelInfo(position));

        // Update sensors data via MQTT subscriber
        sensorsDataFromMQTT.setSensorObserverInterface(data -> {
            Log.d("SensorsModuleInfoActivity", data);
            updateSensorDataViaMQTT(data);
        });
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra(CUSTOMER_CODE, customerCode);
        data.putExtra(SENSOR_ID, sensorId);

        setResult(Activity.RESULT_OK, data);
        finish();
    }

    /**
     * Get channels data from MySQL database
     */
    private void getSensorChannelsDataFromDatabase() {

        final Callback<CurrentSensorData> callback = new Callback<CurrentSensorData>() {
            @Override
            public void onResponse(@NonNull Call<CurrentSensorData> call, Response<CurrentSensorData> response) {
                assert response.body() != null;
                if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                    List<String> sensorDataList = response.body().getData();
                    for (String sensorData : sensorDataList) {
                        MqttDataFormat mqttDataFormat = new MqttDataFormat(sensorData);
                        if (mqttDataFormat.getSensorId() == sensorId) {
                            sensorChannelsDataList = new ArrayList<>();
                            for (int channelId = 0; channelId < mqttDataFormat.getChannelValues().size(); channelId++) {
                                ChannelsData channelsData = new ChannelsData();
                                channelsData.setChannelId(channelId);
                                channelsData.setWaterFlowValue(mqttDataFormat.getChannelValues().get(channelId));
                                sensorChannelsDataList.add(channelsData);
                            }

                            customerCodeValueET.setText(customerCode);
                            customerCodeValueET.setVisibility(View.VISIBLE);

                            sensorIdValueTV.setText(String.valueOf(sensorId));
                            sensorIdValueTV.setVisibility(View.VISIBLE);

                            // Sensors list adapter
                            sensorAdapter = new SensorsAdapter(sensorChannelsDataList, getApplicationContext());
                            sensorsListView.setAdapter(sensorAdapter);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CurrentSensorData> call, Throwable t) {
                Log.e("API", "ERROR on calling ApiManager.getSensorChannelsDataBySensorId(). Error Message: " + t.getMessage());
            }
        };

        ApiManager.getSensorChannelsDataBySensorId(sensorId, callback);
    }

    /**
     * Update in real time the channels data for the current sensor
     */
    private void updateSensorDataViaMQTT(String payload) {
        MqttDataFormat mqttDataFormat = new MqttDataFormat(payload);

        // The received channels information are not for current moduleId
        if (mqttDataFormat.getSensorId() != sensorId) {
            return;
        }

        // Clean channels flow data and add the new one
        sensorChannelsDataList.clear();
        for (int channelId = 0; channelId < mqttDataFormat.getChannelValues().size(); channelId++) {
            ChannelsData channelsData = new ChannelsData();
            channelsData.setChannelId(channelId);
            channelsData.setWaterFlowValue(mqttDataFormat.getChannelValues().get(channelId));
            sensorChannelsDataList.add(channelsData);
        }

        Log.d("updateSensorDataViaMQTT", sensorChannelsDataList.toString());
        Runnable run = () -> sensorAdapter.notifyDataSetChanged();
        runOnUiThread(run);
    }

    /**
     * Open graphic activity for a specific channel
     */
    private void openSensorChannelInfo(int position) {
        Intent intent = new Intent(SensorsModuleInfoActivity.this, SensorsChannelInfoActivity.class);
        Bundle b = new Bundle();
        b.putString(getString(R.string.sensor_id_field), String.valueOf(sensorId));
        b.putString(getString(R.string.channel_id_field), String.valueOf(sensorChannelsDataList.get(position).getChannelId()));
        b.putString(getString(R.string.customer_code_field), customerCode);
        intent.putExtras(b);
        startActivity(intent);
    }
}
