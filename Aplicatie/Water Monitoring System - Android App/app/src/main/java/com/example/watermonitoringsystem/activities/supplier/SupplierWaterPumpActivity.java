package com.example.watermonitoringsystem.activities.supplier;

import static com.example.watermonitoringsystem.authentication.LogoutHelper.logoutFromActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.common.AboutAppActivity;
import com.example.watermonitoringsystem.activities.common.AppSupportActivity;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.mqtt.MqttSenderThread;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.triggertrap.seekarc.SeekArc;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SupplierWaterPumpActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch pumpSwitchView;
    private TextView pumpSwitchTextView;
    private ConstraintLayout switchConstraintLayout;
    private TextView pumpValueTxtView;
    private SeekArc pumpSeekArc;
    private int previousPumpValue;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.supplier_water_pump_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_supplier);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);

        // Drawer Layout
        DrawerLayout drawer = findViewById(R.id.drawer_layout_supplier_water_pump);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view_supplier);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_water_pump);
        View headerLayout = navigationView.getHeaderView(0);

        TextView txtName = headerLayout.findViewById(R.id.user_nav_header);
        TextView txtEmail = headerLayout.findViewById(R.id.email_nav_header);
        CircleImageView imgProfile = headerLayout.findViewById(R.id.profile_picture_nav_header);

        String email = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_EMAIL, SupplierWaterPumpActivity.this);
        Utils.getSupplierProfileFromDatabase(email, txtName, txtEmail, imgProfile);

        // Get notifications number
        ImageView redSquare = findViewById(R.id.red_square);
        TextView notificationNumber = findViewById(R.id.notifications_number);
        Utils.getNotificationsNumber(redSquare, notificationNumber);

        // Notification Bell
        ImageView notificationsBellImg = findViewById(R.id.notificationBellImg);
        notificationsBellImg.setOnClickListener(Utils::openNotificationsPopUp);

        pumpValueTxtView = findViewById(R.id.pump_value);
        pumpSwitchTextView = findViewById(R.id.pump_switch_text);
        pumpSwitchView = findViewById(R.id.pump_switch);
        switchConstraintLayout = findViewById(R.id.switchPumpLayout);
        pumpSeekArc = findViewById(R.id.pump_seekarc);
        Button pumpUpdateValueBtn = findViewById(R.id.btn_update_pump_value);
        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(switchConstraintLayout);

        // Update switch on change
        pumpSwitchView.setOnCheckedChangeListener((buttonView, isChecked) -> updateSwitchOnChanged(isChecked));

        // Pump seek arc listener on change
        pumpSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                pumpValueTxtView.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        // Get water pump data from MySQL database
        getWaterPumpDataFromDatabase(constraintSet);

        // Update water pump data in database when "Update value" button is pressed
        pumpUpdateValueBtn.setOnClickListener(v -> {
            // No action if the switch is CLOSE
            if (!pumpSwitchView.isChecked()) {
                return;
            }
            // Otherwise update water pump value
            int pumpValue = Integer.parseInt(pumpValueTxtView.getText().toString());
            updateWaterPumpValue(pumpValue);
            Toast.makeText(getApplicationContext(), getString(R.string.water_pump_value_updated), Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sensors) {
            startActivity(new Intent(this, SupplierSensorsMapActivity.class));
            finish();
        } else if (id == R.id.nav_electrovalve) {
            startActivity(new Intent(this, SupplierElectrovalveActivity.class));
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout_supplier_water_pump);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void getWaterPumpDataFromDatabase(final ConstraintSet constraintSet) {
        /*
         * TODO-1: Remove the lines between ****
         * TODO-2: Get pump from MySQL database
         */

        /*****************************************************************************************/
        Database.getWaterPumpEndpoint().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), R.string.no_sensor_data_into_database, Toast.LENGTH_SHORT).show();
                } else {
                    String state = null;
                    int value = 0;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        switch (Objects.requireNonNull(postSnapshot.getKey())) {
                            case Constants.WATER_PUMP_STATE:
                                state = Objects.requireNonNull(postSnapshot.getValue()).toString();
                                break;
                            case Constants.WATER_PUMP_VALUE:
                                value = Integer.parseInt(Objects.requireNonNull(postSnapshot.getValue()).toString());
                                break;
                            default:
                                Utils.displayToastErrorDatabase(getApplicationContext());
                                Log.e(getString(R.string.title_supplier_water_pump_activity), "Data from database: " + postSnapshot.toString() + "! \"key\" should be \"state\" or \"value\"");
                                break;
                        }
                    }
                    assert state != null;
                    initializeViews(constraintSet, state, value);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.displayToastErrorDatabase(getApplicationContext());
                Log.e(String.valueOf(R.string.title_supplier_water_pump_activity), databaseError.getMessage());
            }
        });
        /*****************************************************************************************/

        // Get registered pump from MySQL database
        // TODO
    }

    private void updateWaterPumpState(String waterPumpState) {
        DatabaseReference waterPumpEndpoint = Database.getWaterPumpEndpoint();
        waterPumpEndpoint.child(Constants.WATER_PUMP_STATE).setValue(waterPumpState, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Log.e(String.valueOf(R.string.title_supplier_water_pump_activity), databaseError.getMessage());
            }
        });
        waterPumpEndpoint.push();
    }

    private void updateWaterPumpValue(int waterPumpValue) {

        /*
         * TODO: Remove the below lines between *****
         *  Nu mai actualizam in baza de date Firebase, ci doar schimbam fizic pompa print MQTT,
         *  Serverul va prinde si el informatia trimisa prin MQTT catre pompa si va face update in baza de date MySQL
         */

        /******************************/
        DatabaseReference waterPumpEndpoint = Database.getWaterPumpEndpoint();
        waterPumpEndpoint.child(Constants.WATER_PUMP_VALUE).setValue(waterPumpValue, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Log.e(String.valueOf(R.string.title_supplier_water_pump_activity), databaseError.getMessage());
            }
        });
        waterPumpEndpoint.push();
        /****************************/

        // Send via MQTT Broker
        String waterPumpPayload = Utils.buildPumpPayload(waterPumpValue);
        MqttSenderThread.publishToPumpTopic(waterPumpPayload);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initializeViews(ConstraintSet constraintSet, String initialState, int initialValue) {
        pumpValueTxtView.setText(String.valueOf(initialValue));

        // Checked - Pump is opened
        if (initialState.equals(Constants.OPEN)) {
            pumpSwitchView.setChecked(true);
            switchToOpen(constraintSet);
        }
        // Unchecked - Pump is closed
        else {
            pumpSwitchView.setChecked(false);
            switchToClose(constraintSet);
        }

        pumpSeekArc.setProgress(initialValue);
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateSwitchOnChanged(boolean isChecked) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(switchConstraintLayout);
        String state;

        if (isChecked) {
            switchToOpen(constraintSet);
            changeSeekBarOnOpen();
            state = Constants.OPEN;
        } else {
            switchToClose(constraintSet);
            changeSeekBarOnClose();
            state = Constants.CLOSE;
        }
        constraintSet.applyTo(switchConstraintLayout);
        // Enable/Disable the seek bar
        pumpSeekArc.setEnabled(isChecked);

        int value = Integer.parseInt(pumpValueTxtView.getText().toString());
        updateWaterPumpState(state);
        updateWaterPumpValue(value);
        Toast.makeText(getApplicationContext(), getString(R.string.water_pump_state_updated) + " " + state, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchToOpen(ConstraintSet constraintSet) {
        pumpSwitchTextView.setText(getString(R.string.open_selected));
        constraintSet.connect(R.id.pump_switch_text, ConstraintSet.LEFT, R.id.pump_switch, ConstraintSet.LEFT, 0);
        constraintSet.connect(R.id.pump_switch_text, ConstraintSet.RIGHT, ConstraintSet.UNSET, ConstraintSet.RIGHT, 0);
        pumpSwitchView.setTrackDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_switch_track_green_open_close));
        pumpSwitchView.setThumbDrawable(getDrawable(R.drawable.shape_switch_selector_green_open_close));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchToClose(ConstraintSet constraintSet) {
        pumpSwitchTextView.setText(getString(R.string.close_selected));
        constraintSet.connect(R.id.pump_switch_text, ConstraintSet.RIGHT, R.id.pump_switch, ConstraintSet.RIGHT, 0);
        constraintSet.connect(R.id.pump_switch_text, ConstraintSet.LEFT, ConstraintSet.UNSET, ConstraintSet.LEFT, 0);
        pumpSwitchView.setTrackDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_switch_track_red_open_close));
        pumpSwitchView.setThumbDrawable(getDrawable(R.drawable.shape_switch_selector_red_open_close));
    }

    private void changeSeekBarOnOpen() {
        pumpSeekArc.setProgressColor(getColor(R.color.dark_red));
        pumpSeekArc.setArcColor(getColor(R.color.dark_green));
        pumpValueTxtView.setText(String.valueOf(previousPumpValue));
    }

    private void changeSeekBarOnClose() {
        pumpSeekArc.setProgressColor(getColor(R.color.dark_gray));
        pumpSeekArc.setArcColor(getColor(R.color.light_gray));
        previousPumpValue = Integer.parseInt(pumpValueTxtView.getText().toString());
        pumpValueTxtView.setText(String.valueOf(0));
    }
}
