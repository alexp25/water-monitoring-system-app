package com.example.watermonitoringsystem.activities.supplier;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.common.AboutAppActivity;
import com.example.watermonitoringsystem.activities.common.AppSupportActivity;
import com.example.watermonitoringsystem.activities.common.SensorsModuleInfoActivity;
import com.example.watermonitoringsystem.api.ApiManager;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.app.SensorData;
import com.example.watermonitoringsystem.models.firebasedb.SensorsPerCustomersData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredRawElementsData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredElementData;
import com.example.watermonitoringsystem.mqtt.MqttConstants;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Sensors Map for supplier
 *
 * @author Ioan-Alexandru Chirita
 */
public class SupplierSensorsMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private ArrayList<SensorData> sensorDataListForMap;
    private ArrayList<Marker> mMarkerArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.supplier_sensors_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_supplier);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);

        // Drawer Layout
        DrawerLayout drawer = findViewById(R.id.drawer_layout_supplier_sensors);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view_supplier);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_sensors);
        View headerLayout = navigationView.getHeaderView(0);

        TextView txtName = headerLayout.findViewById(R.id.user_nav_header);
        TextView txtEmail = headerLayout.findViewById(R.id.email_nav_header);
        CircleImageView imgProfile = headerLayout.findViewById(R.id.profile_picture_nav_header);

        String email = Utils.getValueFromSharedPreferences(Constants.keyEmail, SupplierSensorsMapActivity.this);
        Utils.getSupplierProfileFromDatabase(email, txtName, txtEmail, imgProfile);

        // Get notifications number
        ImageView redSquare = findViewById(R.id.red_square);
        TextView notificationNumber = findViewById(R.id.notifications_number);
        Utils.getNotificationsNumber(redSquare, notificationNumber);

        // Notification Bell
        ImageView notificationsBellImg = findViewById(R.id.notificationBellImg);
        notificationsBellImg.setOnClickListener(Utils::openNotificationsPopUp);

        // Create Google Map
        mMarkerArray = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(SupplierSensorsMapActivity.this);
    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        // Build sensors data list from MySQL database and Firebase Realtime Database
        getModulesDataFromDatabases(googleMap);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_electrovalve) {
            startActivity(new Intent(this, SupplierElectrovalveActivity.class));
            finish();
        } else if (id == R.id.nav_water_pump) {
            startActivity(new Intent(this, SupplierWaterPumpActivity.class));
            finish();
        } else if (id == R.id.nav_app_support) {
            startActivity(new Intent(this, AppSupportActivity.class));
            finish();
        } else if (id == R.id.nav_about_app) {
            startActivity(new Intent(this, AboutAppActivity.class));
            finish();
        } else if (id == R.id.nav_sign_out) {
            Toast.makeText(getApplicationContext(), R.string.logout_successfully, Toast.LENGTH_SHORT).show();
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_supplier_sensors);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Get sensors' data from MySQL database
     */
    private void getModulesDataFromDatabases(@NotNull GoogleMap googleMap) {
        sensorDataListForMap = new ArrayList<>();

        final Callback<RegisteredRawElementsData> callback = new Callback<RegisteredRawElementsData>() {
            @Override
            public void onResponse(@NonNull Call<RegisteredRawElementsData> call, Response<RegisteredRawElementsData> response) {
                assert response.body() != null;
                Log.d("API", "Response Code for calling APIManager.getRegisteredSensors(): " + response.code() + "; Body: " + response.body().toString());

                List<RegisteredElementData> registeredSensorsData = response.body().getData();
                Log.d("API-Data", registeredSensorsData.toString());

                // Build list of sensors from MySQL database
                buildSensorsList(registeredSensorsData);

                // Add customerCode from Firebase and and GPS coordinate from MySQL database
                Database.getSensorsEndpoint().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(getApplicationContext(), R.string.no_sensor_data_into_database, Toast.LENGTH_SHORT).show();
                        } else {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                SensorsPerCustomersData sensorsPerCustomersDataFromDb = postSnapshot.getValue(SensorsPerCustomersData.class);
                                assert sensorsPerCustomersDataFromDb != null;
                                for (SensorData sensorData : sensorDataListForMap) {
                                    assert sensorData != null;
                                    if (sensorData.getSensorId() == sensorsPerCustomersDataFromDb.getSensorId()) {
                                        sensorData.setCustomerCode(sensorsPerCustomersDataFromDb.getCustomerCode());
                                    }
                                }
                            }
                            buildGoogleMapsWithMarkers(googleMap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase DB", "Database.getSensorsEndpoint(). Error: " + error);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<RegisteredRawElementsData> call, Throwable t) {
                Log.e("API", "ERROR on calling ApiManager.getRegisteredSensors(). Error Message: " + t.getMessage());
            }
        };

        ApiManager.getRegisteredSensors(MqttConstants.SENSOR_NODE_TYPE, callback);
    }

    /**
     * Build sensors list for Google map
     */
    private void buildSensorsList(List<RegisteredElementData> registeredSensorsData) {
        for (RegisteredElementData sensorDataFromMySqlDb : registeredSensorsData) {
            if (sensorDataFromMySqlDb.getSensorTypeCode() == MqttConstants.SENSOR_NODE_TYPE) {
                if (sensorDataFromMySqlDb.getSensorId() != -1 && sensorDataFromMySqlDb.getLat() != null && sensorDataFromMySqlDb.getLng() != null) {
                    SensorData sensorData = new SensorData();
                    sensorData.setSensorId(sensorDataFromMySqlDb.getSensorId());
                    sensorData.setLatitude(sensorDataFromMySqlDb.getLat());
                    sensorData.setLongitude(sensorDataFromMySqlDb.getLng());
                    sensorDataListForMap.add(sensorData);
                }
            }
        }
    }

    /**
     * Build Google map
     */
    private void buildGoogleMapsWithMarkers(@NonNull GoogleMap googleMap) {
        double latitudeAverage = 0.0;
        double longitudeAverage = 0.0;

        for (SensorData sensor : sensorDataListForMap) {
            LatLng place = new LatLng(sensor.getLatitude(), sensor.getLongitude());
            latitudeAverage += sensor.getLatitude();
            longitudeAverage += sensor.getLongitude();
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(place)
                    .title("SensorId: " + sensor.getSensorId() + "; CustomerCode: " + sensor.getCustomerCode())
            );
            mMarkerArray.add(marker);
        }

        latitudeAverage /= sensorDataListForMap.size();
        longitudeAverage /= sensorDataListForMap.size();

        LatLng currentPosition = new LatLng(latitudeAverage, longitudeAverage);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 6.5f));

        // Action on long click on a marker => open sensor module's channels
        googleMap.setOnMapLongClickListener(latLng -> {
            for (Marker marker : mMarkerArray) {
                if (Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.01 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.01) {
                    String[] markerSplitParts = Utils.getModuleIdAndCustomerCodeFromMarkerTitle(Objects.requireNonNull(marker.getTitle()));
                    openSensorModuleInfo(markerSplitParts[0], markerSplitParts[1]);
                    break;
                }
            }
        });

        // Action on click on a marker => view centered on that marker and show the customerId and sensorId
        googleMap.setOnMarkerClickListener(marker -> {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            return false;
        });

        // Action on click on map, not on a specific marker => open activity to configure sensors (add at selected coordinate a new sensor + optional the customerCode for it)
        googleMap.setOnMapClickListener(latLng -> new AlertDialog.Builder(SupplierSensorsMapActivity.this).setTitle("Add new location")
                .setMessage("Are you sure you want to add a new location and a customer code for an existing sensor?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Bundle b = new Bundle();

                    DecimalFormat numberFormat = new DecimalFormat("#.000000");
                    b.putDouble(getString(R.string.latitude_field), Double.parseDouble(numberFormat.format(latLng.latitude)));
                    b.putDouble(getString(R.string.longitude_field), Double.parseDouble(numberFormat.format(latLng.longitude)));
                    Intent intent = new Intent(SupplierSensorsMapActivity.this, AddCoordinateToExistingSensor.class);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show());
    }

    /**
     * Open sensor's channel activity
     */
    private void openSensorModuleInfo(String moduleId, String customerCode) {
        Intent intent = new Intent(SupplierSensorsMapActivity.this, SensorsModuleInfoActivity.class);
        Bundle b = new Bundle();
        b.putString(getString(R.string.sensor_id_field), moduleId);
        b.putString(getString(R.string.customer_code_field), customerCode);
        intent.putExtras(b);
        finish();
        startActivity(intent);
    }
}
