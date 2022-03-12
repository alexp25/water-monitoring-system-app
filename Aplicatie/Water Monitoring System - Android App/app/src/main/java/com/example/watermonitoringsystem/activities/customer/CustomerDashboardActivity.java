package com.example.watermonitoringsystem.activities.customer;

import static com.example.watermonitoringsystem.authentication.LogoutHelper.logoutFromActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.example.watermonitoringsystem.activities.common.AboutAppActivity;
import com.example.watermonitoringsystem.activities.common.AppSupportActivity;
import com.example.watermonitoringsystem.activities.common.SensorsModuleInfoActivity;
import com.example.watermonitoringsystem.adapters.ModulesAdapter;
import com.example.watermonitoringsystem.api.ApiManager;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.CustomerData;
import com.example.watermonitoringsystem.models.app.SensorData;
import com.example.watermonitoringsystem.models.firebasedb.NotificationData;
import com.example.watermonitoringsystem.models.firebasedb.SensorsPerCustomersData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredRawElementsData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredElementData;
import com.example.watermonitoringsystem.mqtt.MqttConstants;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.NotificationType;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Main class for customer - He can see his assigned sensors
 *
 * @author Ioan-Alexandru Chirita
 */
public class CustomerDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<SensorData> sensorsDataList;
    private ModulesAdapter sensorAdaptor;
    private String currentCustomerCode;
    private Button btnNoSensorAvailable;
    private ListView modulesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.customer_dashboard_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_customer);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);

        // Drawer Layout
        DrawerLayout drawer = findViewById(R.id.drawer_layout_customer_dashboard);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view_customer);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home_customer);
        View headerLayout = navigationView.getHeaderView(0);

        TextView txtName = headerLayout.findViewById(R.id.user_nav_header);
        TextView txtEmail = headerLayout.findViewById(R.id.email_nav_header);
        CircleImageView imgProfile = headerLayout.findViewById(R.id.profile_picture_nav_header);

        currentCustomerCode = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_CUSTOMER_CODE, CustomerDashboardActivity.this);

        Utils.getCustomerProfileFromDatabase(currentCustomerCode, txtName, txtEmail, imgProfile);

        // List adapter
        sensorsDataList = new ArrayList<>();
        btnNoSensorAvailable = findViewById(R.id.btn_customer_sensor_request);
        modulesListView = findViewById(R.id.customer_sensors_list_view);
        sensorAdaptor = new ModulesAdapter(sensorsDataList, getApplicationContext());
        modulesListView.setAdapter(sensorAdaptor);
        modulesListView.setOnItemClickListener((parent, view, position, id) -> openSensorInfo(position));

        // Init modules data
        getSensorsDataFromDatabases();

        btnNoSensorAvailable.setOnClickListener(v -> sendNotificationForSensorRequest(currentCustomerCode));
    }


    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_personal_data) {
            startActivity(new Intent(this, CustomerPersonalProfileActivity.class));
            finish();
        } else if (id == R.id.nav_complaints) {
            startActivity(new Intent(this, CustomerComplaintsActivity.class));
            finish();
        } else if (id == R.id.nav_app_support) {
            startActivity(new Intent(this, AppSupportActivity.class));
            finish();
        } else if (id == R.id.nav_about_app) {
            startActivity(new Intent(this, AboutAppActivity.class));
            finish();
        } else if (id == R.id.nav_sign_out) {
            logoutFromActivity(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_customer_dashboard);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openSensorInfo(int position) {
        SensorData moduleData = sensorsDataList.get(position);
        Intent intent = new Intent(CustomerDashboardActivity.this, SensorsModuleInfoActivity.class);
        Bundle b = new Bundle();
        b.putString(getString(R.string.sensor_id_field), String.valueOf(moduleData.getSensorId()));
        b.putString(getString(R.string.customer_code_field), String.valueOf(moduleData.getCustomerCode()));
        intent.putExtras(b);
        startActivity(intent);
    }

    private void getSensorsDataFromDatabases() {

        // Add customerCode and sensorId from Firebase Realtime Database to customer sensors
        Database.getSensorsEndpoint().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getApplicationContext(), R.string.no_sensor_data_into_database, Toast.LENGTH_SHORT).show();
                } else {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        SensorsPerCustomersData sensorsPerCustomersDataFromDb = postSnapshot.getValue(SensorsPerCustomersData.class);
                        assert sensorsPerCustomersDataFromDb != null;
                        if (sensorsPerCustomersDataFromDb.getCustomerCode().equals(currentCustomerCode)) {
                            SensorData sensorData = new SensorData();
                            sensorData.setSensorId(sensorsPerCustomersDataFromDb.getSensorId());
                            sensorData.setCustomerCode(sensorsPerCustomersDataFromDb.getCustomerCode());
                            sensorsDataList.add(sensorData);
                        }
                    }

                    // Get registered sensors from MySQL database for this customer by its customer code
                    final Callback<RegisteredRawElementsData> callback = new Callback<RegisteredRawElementsData>() {
                        @Override
                        public void onResponse(@NonNull Call<RegisteredRawElementsData> call, Response<RegisteredRawElementsData> response) {
                            assert response.body() != null;
                            Log.i("API", "Response Code for calling APIManager.getRegisteredSensors(): " + response.code() + "; Body: " + response.body().toString());

                            List<RegisteredElementData> registeredSensorsDataList = response.body().getData();

                            for (RegisteredElementData registeredSensorsData : registeredSensorsDataList) {
                                for (SensorData sensorData : sensorsDataList) {
                                    if (registeredSensorsData.getSensorId() == sensorData.getSensorId()) {
                                        sensorData.setLatitude(registeredSensorsData.getLat());
                                        sensorData.setLongitude(registeredSensorsData.getLng());
                                    }
                                }
                            }

                            sensorAdaptor.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(@NonNull Call<RegisteredRawElementsData> call, Throwable t) {
                            Log.e("API", "ERROR on calling ApiManager.getRegisteredSensors(). Error Message: " + t.getMessage());
                        }
                    };

                    ApiManager.getRegisteredSensors(MqttConstants.SENSOR_NODE_TYPE, callback);

                    // No module assign for the current customer - Display sensor request button
                    if (sensorsDataList.isEmpty()) {
                        btnNoSensorAvailable.setVisibility(View.VISIBLE);
                        modulesListView.setVisibility(View.INVISIBLE);
                    }
                    // Module for the current customer is available - Display module data
                    else {
                        modulesListView.setVisibility(View.VISIBLE);
                        btnNoSensorAvailable.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase DB", "Database.getSensorsEndpoint(). Error: " + error);
            }
        });
    }

    private void sendNotificationForSensorRequest(String customerCode) {
        Database.getCustomerEndpoint().orderByChild(getString(R.string.customer_code_firebase_field)).equalTo(customerCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // Customer is registered on the database
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CustomerData customerFromDb = postSnapshot.getValue(CustomerData.class);
                        assert customerFromDb != null;
                        final NotificationData notificationData = getNotificationDataForSensorRequest(customerFromDb);
                        insertNotificationIntoDatabase(notificationData);
                    }
                }
                // Customer is not registered into database
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.customer_does_not_exist_into_database), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                Log.e(getString(R.string.title_customer_personal_profile), databaseError.getMessage());
            }
        });
    }

    @NotNull
    private NotificationData getNotificationDataForSensorRequest(CustomerData customerData) {
        final NotificationData notificationData = new NotificationData();
        notificationData.setNotificationId(Utils.getNotificationCurrentDateInMilliseconds());
        notificationData.setSubject(getString(R.string.new_sensor_request_customer));

        String phoneNumber = "";
        if (customerData.getPhoneNumber() != null) {
            phoneNumber = getString(R.string.phone_number_sensor_request_customer) + " " + customerData.getCountryPhoneNumberPrefix() + ") " + customerData.getPhoneNumber();
        }

        String sb = getString(R.string.auto_message_part1) + "\n\n" +
                getString(R.string.auto_message_part2) + "\n\t" +
                getString(R.string.name_sensor_request_customer) + " " + customerData.getFullName() + "\n\t" +
                getString(R.string.customer_code_sensor_request_customer) + " " + customerData.getCustomerCode() + "\n\t" +
                getString(R.string.email_sensor_request_customer) + " " + customerData.getEmail() + "\n\t" + phoneNumber;
        notificationData.setMessage(sb);
        notificationData.setDate(Utils.convertDateFromMillisecondsToCustomFormat(notificationData.getNotificationId(), Constants.NOTIFICATION_DATE_FORMAT));
        notificationData.setRead(false);
        notificationData.setType(NotificationType.SENSOR_REQUEST.name());
        notificationData.setCustomerCode(customerData.getCustomerCode());
        return notificationData;
    }

    private void insertNotificationIntoDatabase(NotificationData notificationData) {
        DatabaseReference notificationsEndpoint2 = Database.getNotificationsEndpoint();

        final Query query = notificationsEndpoint2.orderByChild(getString(R.string.notification_id_field)).equalTo(String.valueOf(notificationData.getNotificationId()));

        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // One message inserted already at that time into database
                if (dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_try_again), Toast.LENGTH_LONG).show();
                }
                // Normal insert
                else {
                    DatabaseReference notificationsEndpoint = Database.getNotificationsEndpoint();
                    notificationsEndpoint.child(String.valueOf(notificationData.getNotificationId())).setValue(notificationData, (databaseError, databaseReference) -> {
                        if (databaseError == null) {
                            Toast.makeText(getApplicationContext(), "Sensor Request message was sent to supplier!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.the_message_was_not_sent), Toast.LENGTH_SHORT).show();
                            Log.e(getString(R.string.title_register_fragment), databaseError.getCode() + ": " + databaseError.getMessage());
                        }
                    });
                    notificationsEndpoint.push();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(getString(R.string.title_register_fragment), databaseError.getMessage());
            }
        };
        query.addValueEventListener(valueEventListener);
    }
}