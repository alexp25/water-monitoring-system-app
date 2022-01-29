package com.example.watermonitoringsystem.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.customer.CustomerComplaintsActivity;
import com.example.watermonitoringsystem.activities.customer.CustomerDashboardActivity;
import com.example.watermonitoringsystem.activities.customer.CustomerPersonalProfileActivity;
import com.example.watermonitoringsystem.activities.supplier.SupplierElectrovalveActivity;
import com.example.watermonitoringsystem.activities.supplier.SupplierSensorsMapActivity;
import com.example.watermonitoringsystem.activities.supplier.SupplierWaterPumpActivity;
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
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Channels data activity, updated in real time - common for supplier and customer
 *
 * @author Ioan-Alexandru Chirita
 */
public class SensorsModuleInfoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String userType;
    private ArrayList<ChannelsData> sensorChannelsDataList;
    private SensorsAdapter sensorAdapter;
    public static SensorDataListener sensorsDataFromMQTT;

    private int sensorId;
    private String customerCode;

    private ListView sensorsListView;
    private TextView customerCodeLabel;
    private TextView sensorIdLabel;
    private Button setCustomerCodeBtn;

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

        // NavigationView + Clear menu
        NavigationView navigationView = findViewById(R.id.nav_view_sensor_info);
        navigationView.getMenu().clear();

        // Update Toolbar and NavigationView regarding to the user type = SUPPLIER / CUSTOMER
        if (userType.equals(Constants.SUPPLIER)) {
            // Remove toolbar for customer and set toolbar for supplier
            appBarLayout.removeView(customerToolbar);
            mainToolbar = supplierToolbar;
            // Inflate supplier menu
            navigationView.inflateMenu(R.menu.navigation_drawer_supplier);
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

        // DrawerLayout
        DrawerLayout drawer = findViewById(R.id.drawer_layout_sensor_info);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView
        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);

        TextView txtName = headerLayout.findViewById(R.id.user_nav_header);
        TextView txtEmail = headerLayout.findViewById(R.id.email_nav_header);
        CircleImageView imgProfile = headerLayout.findViewById(R.id.profile_picture_nav_header);

        if (userType.equals(Constants.CUSTOMER)) {
            String customerCode = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_CUSTOMER_CODE, SensorsModuleInfoActivity.this);
            Utils.getCustomerProfileFromDatabase(customerCode, txtName, txtEmail, imgProfile);
        }
        // Only supplier has notifications bell
        else {
            String email = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_EMAIL, SensorsModuleInfoActivity.this);
            Utils.getSupplierProfileFromDatabase(email, txtName, txtEmail, imgProfile);

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

        sensorsListView = findViewById(R.id.sensors_module_list_view);
        customerCodeLabel = findViewById(R.id.customerCodeLabel);
        sensorIdLabel = findViewById(R.id.sensorIdLabel);
        setCustomerCodeBtn = findViewById(R.id.btnAddNewCustomer);

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

//    @Override
//    public void onBackPressed() {
//        Intent intent;
//        if (userType.equals(Constants.SUPPLIER)) {
//            intent = new Intent(this, SupplierSensorsMapActivity.class);
//        } else {
//            intent = new Intent(this, CustomerDashboardActivity.class);
//        }
//        finish();
//        startActivity(intent);
//    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_sensors) {
            startActivity(new Intent(this, SupplierSensorsMapActivity.class));
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
        } else if (id == R.id.nav_about_app) {
            startActivity(new Intent(this, AboutAppActivity.class));
            finish();
        } else if (id == R.id.nav_app_support) {
            startActivity(new Intent(this, AppSupportActivity.class));
            finish();
        } else if (id == R.id.nav_sign_out) {
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_sensor_info);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

                            String text = getString(R.string.customer_code) + ": " + customerCode;
                            customerCodeLabel.setText(text);
                            customerCodeLabel.setVisibility(View.VISIBLE);

                            text = getString(R.string.sensor_id_text) + ": " + sensorId;
                            sensorIdLabel.setText(text);
                            sensorIdLabel.setVisibility(View.VISIBLE);

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
        finish();
        intent.putExtras(b);
        startActivity(intent);
    }
}
