package com.example.watermonitoringsystem.activities.supplier;

import static com.example.watermonitoringsystem.authentication.LogoutHelper.logoutFromActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.example.watermonitoringsystem.activities.common.AboutAppActivity;
import com.example.watermonitoringsystem.activities.common.AppSupportActivity;
import com.example.watermonitoringsystem.activities.common.SensorsMapActivity;
import com.example.watermonitoringsystem.adapters.ElectrovalvesAdapter;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.app.ElectrovalvesData;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Electrovalves activity
 *
 * @author Ioan-Alexandru Chirita
 */
public class SupplierElectrovalveActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ElectrovalvesAdapter electrovalveAdapter;
    private ArrayList<ElectrovalvesData> electrovalveDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.supplier_electrovalve_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_supplier);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);

        // Drawer Layout
        DrawerLayout drawer = findViewById(R.id.drawer_layout_supplier_electrovalve);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view_supplier);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_electrovalve);
        View headerLayout = navigationView.getHeaderView(0);

        TextView txtName = headerLayout.findViewById(R.id.user_nav_header);
        TextView txtEmail = headerLayout.findViewById(R.id.email_nav_header);
        CircleImageView imgProfile = headerLayout.findViewById(R.id.profile_picture_nav_header);

        String email = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_EMAIL, SupplierElectrovalveActivity.this);
        Utils.getSupplierProfileFromDatabase(email, txtName, txtEmail, imgProfile);

        // Get notifications number
        ImageView redSquare = findViewById(R.id.red_square);
        TextView notificationNumber = findViewById(R.id.notifications_number);
        Utils.getNotificationsNumber(redSquare, notificationNumber);

        // Notification Bell
        ImageView notificationsBellImg = findViewById(R.id.notificationBellImg);
        notificationsBellImg.setOnClickListener(Utils::openNotificationsPopUp);

        // Electrovalves list adapter
        ListView electrovalvesListView = findViewById(R.id.supplier_electrovalves_list_view);
        electrovalveDataList = new ArrayList<>();
        electrovalveAdapter = new ElectrovalvesAdapter(electrovalveDataList, getApplicationContext());
        electrovalvesListView.setAdapter(electrovalveAdapter);

        // Get electrovalves data from MySQL database
        getElectrovalveDataFromDatabase();
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
            logoutFromActivity(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_supplier_electrovalve);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getElectrovalveDataFromDatabase() {
        /*
         * TODO-1: Remove the lines between ****
         * TODO-2: Get electrovalves from MySQL database
         */
        /******************************************************************************************/
        Database.getElectrovalvesEndpoint().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), R.string.no_sensor_data_into_database, Toast.LENGTH_SHORT).show();
                } else {
                    electrovalveDataList.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        electrovalveDataList.add(postSnapshot.getValue(ElectrovalvesData.class));
                    }
                    electrovalveAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.displayToastErrorDatabase(getApplicationContext());
                Log.e(String.valueOf(R.string.title_supplier_electrovalve_activity), databaseError.getMessage());
            }
        });
        /******************************************************************************************/

        // Get registered electrovalves from MySQL database
        // TODO
    }
}
