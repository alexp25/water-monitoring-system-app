package com.example.watermonitoringsystem.activities.supplier;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.api.ApiManager;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.SensorsPerCustomersData;
import com.example.watermonitoringsystem.models.sqldb.CoordinateDataReturn;
import com.example.watermonitoringsystem.models.sqldb.CoordinatesData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredRawElementsData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredElementData;
import com.example.watermonitoringsystem.mqtt.MqttConstants;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Add coordinate and optional a customer code to an existing sensor
 *
 * @author Ioan-Alexandru Chirita
 */
public class AddCoordinateToExistingSensor extends AppCompatActivity {

    private AutoCompleteTextView sensorACIdTextView;
    private TextView latitudeEditTxtView;
    private TextView longitudeEditTxtView;
    private TextView customerCodeEditTxtView;
    private int selectedSensorId;
    ArrayAdapter<Integer> sensorIdAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_coordinate_to_existing_sensor_activity);

        sensorACIdTextView = findViewById(R.id.sensors_no_customer_acTextView);
        latitudeEditTxtView = findViewById(R.id.latitude_edit_text);
        latitudeEditTxtView.setEnabled(false);
        longitudeEditTxtView = findViewById(R.id.longitude_edit_text);
        longitudeEditTxtView.setEnabled(false);
        customerCodeEditTxtView = findViewById(R.id.customer_code_edit_text);
        Button sensorForCustomerAddBtn = findViewById(R.id.sensor_for_customer_add_btn);
        Button sensorForCustomerCloseBtn = findViewById(R.id.sensor_for_customer_close_btn);

        // Get data send between activities
        Bundle b = getIntent().getExtras();
        double latitude = b.getDouble(getString(R.string.latitude_field));
        double longitude = b.getDouble(getString(R.string.longitude_field));
        latitudeEditTxtView.setText(String.valueOf(latitude));
        longitudeEditTxtView.setText(String.valueOf(longitude));

        // Add data to spinner
        buildSensorIdACTextView();

        // Action when click on Add button => Change sensors coordinate (or add new one) + add customer code to it (optional)
        sensorForCustomerAddBtn.setOnClickListener(v -> addCoordinatesAndCustomerCodeToDb());

        // Action when click on Close button => Return to Google Map
        sensorForCustomerCloseBtn.setOnClickListener(v -> goBackToSupplierSensorsMap());
    }

    @Override
    public void onBackPressed() {
        goBackToSupplierSensorsMap();
    }

    /**
     * Get all sensors registered into MySQL database in order to build the sensors spinner
     */
    private void buildSensorIdACTextView() {

        Callback<RegisteredRawElementsData> callback = new Callback<RegisteredRawElementsData>() {
            @Override
            public void onResponse(@NonNull Call<RegisteredRawElementsData> call, Response<RegisteredRawElementsData> response) {
                if (response.body() == null || response.body().getData() == null || response.body().getData().isEmpty()) {
                    goBackToSupplierSensorsMap();
                } else {
                    ArrayList<Integer> sensorIdList = new ArrayList<>();
                    for (RegisteredElementData registeredSensorsData : response.body().getData()) {
                        sensorIdList.add(registeredSensorsData.getSensorId());

                        sensorIdAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, sensorIdList.toArray(new Integer[0]));
                        sensorACIdTextView.setAdapter(sensorIdAdapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisteredRawElementsData> call, Throwable t) {
                Log.e("API", "ERROR on calling ApiManager.getRegisteredSensors(). Error Message: " + t.getMessage());
            }
        };

        ApiManager.getRegisteredSensors(MqttConstants.SENSOR_NODE_TYPE, callback);
    }

    /**
     * Add for selected sensor the new coordinated into MySQL database and, optional, the customerCode into Firebase database (if it is not "-")
     */
    private void addCoordinatesAndCustomerCodeToDb() {
        CoordinatesData coordinatesData = new CoordinatesData();
        coordinatesData.setSensorId(selectedSensorId);
        coordinatesData.setLat(Double.parseDouble(latitudeEditTxtView.getText().toString()));
        coordinatesData.setLng(Double.parseDouble(longitudeEditTxtView.getText().toString()));
        customerCodeEditTxtView.getText().toString();

        Callback<CoordinateDataReturn> callback = new Callback<CoordinateDataReturn>() {
            @Override
            public void onResponse(@NonNull Call<CoordinateDataReturn> call, @NonNull Response<CoordinateDataReturn> response) {
                DatabaseReference sensorsEndpoint = Database.getSensorsEndpoint();
                SensorsPerCustomersData sensorsPerCustomersData = new SensorsPerCustomersData();
                sensorsPerCustomersData.setSensorId(selectedSensorId);
                if (customerCodeEditTxtView.getText() == null || customerCodeEditTxtView.getText().toString().isEmpty()) {
                    sensorsPerCustomersData.setCustomerCode("-");
                } else {
                    sensorsPerCustomersData.setCustomerCode(customerCodeEditTxtView.getText().toString());
                }
                sensorsEndpoint.child(String.valueOf(sensorsPerCustomersData.getSensorId())).setValue(sensorsPerCustomersData, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        Log.e(getString(R.string.title_activity_add_new_coordinates_for_existing_sensor), databaseError.getMessage());
                    }
                });
                sensorsEndpoint.push();
                goBackToSupplierSensorsMap();
            }

            @Override
            public void onFailure(@NonNull Call<CoordinateDataReturn> call, Throwable t) {
                Log.e("API", "ERROR on calling ApiManager.addCoordinatesToSensor(). Error Message: " + t.getMessage());
            }
        };
        ApiManager.addNewCoordinatesToSensor(coordinatesData, callback);
    }

    /**
     * Go back to Google Map
     */
    private void goBackToSupplierSensorsMap() {
        startActivity(new Intent(this, SupplierSensorsMapActivity.class));
        finish();
    }
}