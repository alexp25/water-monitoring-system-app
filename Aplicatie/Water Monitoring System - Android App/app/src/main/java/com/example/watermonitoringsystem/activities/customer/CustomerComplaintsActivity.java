package com.example.watermonitoringsystem.activities.customer;

import static com.example.watermonitoringsystem.authentication.LogoutHelper.logoutFromActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.common.AboutAppActivity;
import com.example.watermonitoringsystem.activities.common.AppSupportActivity;
import com.example.watermonitoringsystem.activities.common.SensorsMapActivity;
import com.example.watermonitoringsystem.adapters.MessageTypeSpinnerAdapter;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.NotificationData;
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

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Customer complaints activity
 *
 * @author Ioan-Alexandru Chirita
 */
public class CustomerComplaintsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Spinner messageTypeSpinner;
    TextView complaintSubject;
    TextView complaintMessage;
    String customerCode;
    String notificationType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.customer_complaints_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_customer);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);

        // Drawer Layout
        DrawerLayout drawer = findViewById(R.id.drawer_layout_customer_complaints);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view_customer);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_complaints);
        View headerLayout = navigationView.getHeaderView(0);

        TextView txtName = headerLayout.findViewById(R.id.user_nav_header);
        TextView txtEmail = headerLayout.findViewById(R.id.email_nav_header);
        CircleImageView imgProfile = headerLayout.findViewById(R.id.profile_picture_nav_header);

        customerCode = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_CUSTOMER_CODE, CustomerComplaintsActivity.this);

        Utils.getCustomerProfileFromDatabase(customerCode, txtName, txtEmail, imgProfile);

        messageTypeSpinner = findViewById(R.id.complaints_spinner_message_type);
        complaintSubject = findViewById(R.id.complaints_subject);
        complaintMessage = findViewById(R.id.complaints_message);
        Button complaintsSendBtn = findViewById(R.id.complaints_send_btn);

        // Getting the instance of Spinner and applying OnItemSelectedListener on it
        MessageTypeSpinnerAdapter messageTypeSpinnerAdapter = new MessageTypeSpinnerAdapter(getApplicationContext(), NotificationType.values());
        messageTypeSpinner.setAdapter(messageTypeSpinnerAdapter);

        messageTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                notificationType = NotificationType.values()[position].name();
                messageTypeSpinner.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        complaintsSendBtn.setOnClickListener(v -> sendNotification());
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
        } else if (id == R.id.nav_home_customer) {
            startActivity(new Intent(this, CustomerDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_personal_data) {
            startActivity(new Intent(this, CustomerPersonalProfileActivity.class));
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout_customer_complaints);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void sendNotification() {
        final NotificationData notificationData = getNotificationData();
        insertNotificationIntoDatabase(notificationData);
    }

    @NotNull
    private NotificationData getNotificationData() {
        final NotificationData notificationData = new NotificationData();
        notificationData.setNotificationId(Utils.getNotificationCurrentDateInMilliseconds());
        notificationData.setSubject(complaintSubject.getText().toString());
        notificationData.setMessage(complaintMessage.getText().toString());
        notificationData.setDate(Utils.convertDateFromMillisecondsToCustomFormat(notificationData.getNotificationId(), Constants.NOTIFICATION_DATE_FORMAT));
        notificationData.setRead(false);
        notificationData.setType(notificationType);
        notificationData.setCustomerCode(customerCode);
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
                            Toast.makeText(getApplicationContext(), getString(R.string.the_message_was_sent), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.the_message_was_sent), Toast.LENGTH_SHORT).show();
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
