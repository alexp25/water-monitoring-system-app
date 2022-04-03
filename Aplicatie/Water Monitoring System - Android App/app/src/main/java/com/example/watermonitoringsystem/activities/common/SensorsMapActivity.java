package com.example.watermonitoringsystem.activities.common;

import static com.example.watermonitoringsystem.authentication.LogoutHelper.logoutFromActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.customer.CustomerComplaintsActivity;
import com.example.watermonitoringsystem.activities.customer.CustomerDashboardActivity;
import com.example.watermonitoringsystem.activities.customer.CustomerPersonalProfileActivity;
import com.example.watermonitoringsystem.activities.supplier.AddCoordinateToExistingSensor;
import com.example.watermonitoringsystem.activities.supplier.SupplierElectrovalveActivity;
import com.example.watermonitoringsystem.activities.supplier.SupplierWaterPumpActivity;
import com.example.watermonitoringsystem.api.ApiManager;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.MqttDataFormat;
import com.example.watermonitoringsystem.models.app.SensorData;
import com.example.watermonitoringsystem.models.firebasedb.SensorsPerCustomersData;
import com.example.watermonitoringsystem.models.map.SensorMarker;
import com.example.watermonitoringsystem.models.sqldb.AllSensorsRealTimeDataResponse;
import com.example.watermonitoringsystem.models.sqldb.RegisteredRawElementsData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredElementData;
import com.example.watermonitoringsystem.mqtt.MqttConstants;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public class SensorsMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String TAG = SensorsMapActivity.class.getSimpleName();
    private static final String MAP_CAMERA_POS_KEY = "com.example.watermonitoringsystem.activities.common.SupplierSensorsMapActivity.map_camera_pos";
    private static final String CAMERA_LAT_KEY = "camera_lat_key";
    private static final String CAMERA_LON_KEY = "camera_lon_key";
    private static final String CAMERA_ZOOM_KEY = "camera_zoom_key";

    private String userType;
    private String customerCode;
    private boolean addSensorMode;
    ActivityResultLauncher<Intent> launchSensorModuleInfo;
    private ArrayList<SensorData> sensorDataListForMap;

    // Map
    private GoogleMap googleMap;
    private ArrayList<SensorMarker> mMarkerArray;
    private CustomClusterManager<SensorMarker> mClusterManager;

    // FABs
    private FloatingActionButton toggleViewFab;
    private FloatingActionButton resetFab;
    private FloatingActionButton addSensorFab;
    private boolean viewFlag;
    private TextView addSensorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.supplier_sensors_activity);

        userType = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_USER_TYPE, this);
        customerCode = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_CUSTOMER_CODE, this);

        launchSensorModuleInfo = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data == null)
                            return;
                        String customer_code = data.getStringExtra(SensorsModuleInfoActivity.CUSTOMER_CODE);
                        int sensor_id = data.getIntExtra(SensorsModuleInfoActivity.SENSOR_ID, -1);
                        if(customer_code == null || sensor_id == -1)
                            return;
                        for (SensorData sensor : sensorDataListForMap) {
                            if(sensor.getSensorId() != sensor_id)
                                continue;
                            if(sensor.getCustomerCode().equals(customer_code))
                                return;
                            sensor.setCustomerCode(customer_code);
                        }
                        buildGoogleMapsWithMarkers(googleMap);
                    }
                }
        );

        // Toolbar
        AppBarLayout appBarLayout = findViewById(R.id.appBar);
        Toolbar mainToolbar;
        Toolbar supplierToolbar = appBarLayout.findViewById(R.id.toolbar_supplier);
        Toolbar customerToolbar = appBarLayout.findViewById(R.id.toolbar_customer);


        // Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view_supplier);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_sensors);
        View headerLayout = navigationView.getHeaderView(0);

        // Update Toolbar and NavigationView regarding to the user type = SUPPLIER / CUSTOMER
        if (userType.equals(Constants.SUPPLIER)) {
            // Remove toolbar for customer and set toolbar for supplier
            appBarLayout.removeView(customerToolbar);
            mainToolbar = supplierToolbar;
            // Inflate supplier menu
            navigationView.inflateMenu(R.menu.navigation_drawer_supplier);

            // Get notifications number
            ImageView redSquare = findViewById(R.id.red_square);
            TextView notificationNumber = findViewById(R.id.notifications_number);
            Utils.getNotificationsNumber(redSquare, notificationNumber);

            // Notification Bell
            ImageView notificationsBellImg = findViewById(R.id.notificationBellImg);
            notificationsBellImg.setOnClickListener(Utils::openNotificationsPopUp);
        } else {
            // Remove toolbar for supplier and set toolbar for customer
            appBarLayout.removeView(supplierToolbar);
            mainToolbar = customerToolbar;
            // Inflate customer menu
            navigationView.inflateMenu(R.menu.navigation_drawer_customer);
        }

        // Toolbar
        setSupportActionBar(mainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);

        // Drawer Layout
        DrawerLayout drawer = findViewById(R.id.drawer_layout_supplier_sensors);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        TextView txtName = headerLayout.findViewById(R.id.user_nav_header);
        TextView txtEmail = headerLayout.findViewById(R.id.email_nav_header);
        CircleImageView imgProfile = headerLayout.findViewById(R.id.profile_picture_nav_header);

        String email = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_EMAIL, SensorsMapActivity.this);
        Utils.getSupplierProfileFromDatabase(email, txtName, txtEmail, imgProfile);


        // Create Google Map
        mMarkerArray = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(SensorsMapActivity.this);

        // Add sensor Text View
        addSensorTextView = findViewById(R.id.addSensorTextView);

        // FABs
        addSensorFab = findViewById(R.id.addSensorFab);
        addSensorFab.setOnClickListener(v -> {
            addSensorMode = !addSensorMode;
            if(addSensorMode){
                addSensorTextView.setVisibility(View.VISIBLE);
                addSensorFab.setImageResource(R.drawable.ic_plus_highlighted);
            }
            else{
                addSensorTextView.setVisibility(View.INVISIBLE);
                addSensorFab.setImageResource(R.drawable.ic_plus);
            }
        });
        resetFab = findViewById(R.id.resetFab);
        resetFab.setOnClickListener(v -> resetMapCameraPosition(true));
        toggleViewFab = findViewById(R.id.viewFab);
        toggleViewFab.setOnClickListener(v -> {
            if(this.viewFlag){
                this.viewFlag = false;
                toggleViewFab.setImageTintList(ColorStateList.valueOf(getColor(R.color.light_gray)));
            }
            else{
                this.viewFlag = true;
                toggleViewFab.setImageTintList(null);
            }
            buildGoogleMapsWithMarkers(googleMap);
        });

        if (userType.equals(Constants.CUSTOMER)) {
            toggleViewFab.setVisibility(View.GONE);
            addSensorFab.setVisibility(View.GONE);
        }
    }


    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        // Build sensors data list from MySQL database and Firebase Realtime Database
        Log.e(TAG, "onMapReady called");

        mClusterManager = new CustomClusterManager<>(this, googleMap);
        googleMap.setOnCameraIdleListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        this.googleMap = googleMap;
        getModulesDataFromDatabases(googleMap);
        setMapCameraPosition();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_sensors) {
            startActivity(new Intent(this, SensorsMapActivity.class));
            finish();
        } else if (id == R.id.nav_electrovalve) {
            startActivity(new Intent(this, SupplierElectrovalveActivity.class));
            finish();
        } else if (id == R.id.nav_water_pump) {
            startActivity(new Intent(this, SupplierWaterPumpActivity.class));
            finish();
        } else if (id == R.id.nav_home_customer) {
            startActivity(new Intent(this, CustomerDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_personal_data) {
            startActivity(new Intent(this, CustomerPersonalProfileActivity.class));
            finish();
        } else if (id == R.id.nav_complaints) {
            startActivity(new Intent(this, CustomerComplaintsActivity.class));
            finish();
        } else if (id == R.id.nav_app_support) {
            startActivity(new Intent(this, AppSupportActivity.class));
            finish();
        } else if (id == R.id.nav_sign_out) {
            logoutFromActivity(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_supplier_sensors);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMapCameraPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveMapCameraPosition();
    }

    /**
     * Reset Map's Camera Position and Zoom to fit all available markers
     * @author Mihai Draghici
     */
    private void resetMapCameraPosition(boolean animation){
        if(googleMap == null || mMarkerArray.size() == 0)
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(SensorMarker marker : mMarkerArray){
            builder.include(marker.getPosition());
        }
        CameraUpdate cUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 250);
        if(animation)
            googleMap.animateCamera(cUpdate);
        else
            googleMap.moveCamera(cUpdate);
        Log.i(TAG, "Camera moved to position: " + googleMap.getCameraPosition().toString());
    }

    private void setMapCameraPosition(){
        if(googleMap == null){
            Log.e(TAG, "Trying to setMapCameraPosition while googleMap is null");
            return;
        }
        SharedPreferences sharedPref = this.getSharedPreferences(MAP_CAMERA_POS_KEY, Context.MODE_PRIVATE);
        float lon = sharedPref.getFloat(CAMERA_LON_KEY, -1);
        float lat = sharedPref.getFloat(CAMERA_LAT_KEY, -1);
        float zoom = sharedPref.getFloat(CAMERA_ZOOM_KEY, -1);
        Log.i(TAG, "Got SharedPref info: " + lon + ", " + lat + ", " + zoom + " (lon, lat, zoom)");
        if(lon == -1 || lat == -1 || zoom == -1)
            resetMapCameraPosition(false);
        else{
            CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom);
            googleMap.moveCamera(cUpdate);
        }
    }
    private void saveMapCameraPosition(){
        if(googleMap == null)
            return;

        CameraPosition cameraPosition = googleMap.getCameraPosition();
        SharedPreferences sharedPref = this.getSharedPreferences(MAP_CAMERA_POS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(CAMERA_LAT_KEY, (float)cameraPosition.target.latitude);
        editor.putFloat(CAMERA_LON_KEY, (float)cameraPosition.target.longitude);
        editor.putFloat(CAMERA_ZOOM_KEY, cameraPosition.zoom);
        editor.apply();
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

                final Callback<AllSensorsRealTimeDataResponse> callback2 = new Callback<AllSensorsRealTimeDataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AllSensorsRealTimeDataResponse> call2,@NonNull Response<AllSensorsRealTimeDataResponse> response2) {
                        assert response2.body() != null;

                        List<RegisteredElementData> registeredSensorsData = response.body().getData();
                        Log.d("API-Data", registeredSensorsData.toString());
                        List<MqttDataFormat> realtimeSensorsData = response2.body().getParsedData();
                        Log.d("API-Data", response2.body().toString());

                        registeredSensorsData.forEach(x -> {
                            if(realtimeSensorsData.contains(MqttDataFormat.ofIdValue(x.getSensorId())))
                                x.setHasDataChannels(true);
                        });

                        // Build list of sensors from MySQL database
                        buildSensorsList(registeredSensorsData);

                        // Add customerCode from Firebase and GPS coordinate from MySQL database
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
                    public void onFailure(@NonNull Call<AllSensorsRealTimeDataResponse> call, Throwable t) {
                        Log.e("API", "ERROR on calling ApiManager.getAllSensorsRealTimeDataChannels(). Error Message: " + t.getMessage());
                    }
                };
                ApiManager.getAllSensorsRealTimeDataChannels(callback2);
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
        sensorDataListForMap.clear();
        for (RegisteredElementData sensorDataFromMySqlDb : registeredSensorsData) {
            if (sensorDataFromMySqlDb.getSensorTypeCode() == MqttConstants.SENSOR_NODE_TYPE) {
                if (sensorDataFromMySqlDb.getSensorId() != -1 && sensorDataFromMySqlDb.getLat() != null && sensorDataFromMySqlDb.getLng() != null) {
                    SensorData sensorData = new SensorData();
                    sensorData.setSensorId(sensorDataFromMySqlDb.getSensorId());
                    sensorData.setLatitude(sensorDataFromMySqlDb.getLat());
                    sensorData.setLongitude(sensorDataFromMySqlDb.getLng());
                    sensorData.setHasDataChannels(sensorDataFromMySqlDb.hasDataChannels());
                    sensorDataListForMap.add(sensorData);
                }
            }
        }
    }

    /**
     * Build Google map
     */
    private void buildGoogleMapsWithMarkers(@NonNull GoogleMap googleMap) {
        mMarkerArray.clear();
        mClusterManager.clearItems();
        googleMap.clear();
        for (SensorData sensor : sensorDataListForMap) {
            if(userType.equals(Constants.CUSTOMER) && !sensor.getCustomerCode().equals(customerCode))
                continue;
            if(!viewFlag && !sensor.hasDataChannels())
                continue;
            LatLng place = new LatLng(sensor.getLatitude(), sensor.getLongitude());
            SensorMarker marker = new SensorMarker(place, "SensorId: " + sensor.getSensorId() + "; CustomerCode: " + sensor.getCustomerCode());
            mMarkerArray.add(marker);
        }
        mClusterManager.addItems(mMarkerArray);
        mClusterManager.cluster();

        // Action on click on a marker => view centered on that marker and show the customerId and sensorId
        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(marker -> {
            String[] markerSplitParts = Utils.getModuleIdAndCustomerCodeFromMarkerTitle(Objects.requireNonNull(marker.getTitle()));
            openSensorModuleInfo(markerSplitParts[0], markerSplitParts[1]);
        });

        // Action on click on map, not on a specific marker => open activity to configure sensors (add at selected coordinate a new sensor + optional the customerCode for it)
        googleMap.setOnMapClickListener(latLng -> {
            if(!addSensorMode)
                return;
            new AlertDialog.Builder(SensorsMapActivity.this).setTitle("Add new location")
                .setMessage("Are you sure you want to add a new location and a customer code for an existing sensor?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Bundle b = new Bundle();

                    DecimalFormat numberFormat = new DecimalFormat("#.000000");
                    numberFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                    b.putDouble(getString(R.string.latitude_field), Double.parseDouble(numberFormat.format(latLng.latitude)));
                    b.putDouble(getString(R.string.longitude_field), Double.parseDouble(numberFormat.format(latLng.longitude)));
                    Intent intent = new Intent(SensorsMapActivity.this, AddCoordinateToExistingSensor.class);
                    intent.putExtras(b);
                    startActivity(intent);
                    //finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        mClusterManager.setOnClusterClickListener(cluster -> {
            String[] choices = cluster.getItems().stream().map(SensorMarker::getTitle).toArray(String[]::new);

            AlertDialog.Builder builder = new AlertDialog.Builder(SensorsMapActivity.this);
            builder.setTitle("Select a sensor:");
            builder.setItems(choices, (dialog, which) -> {
                String[] markerSplitParts = Utils.getModuleIdAndCustomerCodeFromMarkerTitle(Objects.requireNonNull(choices[which]));
                openSensorModuleInfo(markerSplitParts[0], markerSplitParts[1]);
            });
            builder.show();
            return true;
        });
    }

    /**
     * Open sensor's channel activity
     */
    private void openSensorModuleInfo(String moduleId, String customerCode) {
        Intent intent = new Intent(SensorsMapActivity.this, SensorsModuleInfoActivity.class);
        Bundle b = new Bundle();
        b.putString(getString(R.string.sensor_id_field), moduleId);
        b.putString(getString(R.string.customer_code_field), customerCode);
        intent.putExtras(b);

        launchSensorModuleInfo.launch(intent);
    }


    private static class CustomClusterManager<T extends ClusterItem> extends ClusterManager<T> {

        CustomClusterManager(Context context, GoogleMap map) {
            super(context, map);
            this.setRenderer(new DefaultClusterRenderer(context, map, this));
            DefaultClusterRenderer<SensorMarker> renderer = (DefaultClusterRenderer<SensorMarker>) this.getRenderer();
            renderer.setMinClusterSize(2);
        }
    }
}
