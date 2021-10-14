package com.example.watermonitoringsystem.activities.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.watermonitoringsystem.api.ApiManager;
import com.example.watermonitoringsystem.models.sqldb.HistoryData;
import com.example.watermonitoringsystem.models.sqldb.HistoryRawData;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Graphic data activity - common for supplier and customer
 *
 * @author Ioan-Alexandru Chirita
 */
public class SensorsChannelInfoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String userType;
    private int sensorId;
    private int channelId;
    private String customerCode;
    private GraphView graphView;

    private TextView historyDataNumberTxtView;

    private DataPoint minPoint;
    private DataPoint maxPoint;
    private DataPoint[] points;

    private boolean wasGraphicCustomized = false;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userType = Utils.getValueFromSharedPreferences(Constants.keyUserType, SensorsChannelInfoActivity.this);

        setContentView(R.layout.sensors_channel_info_activity);

        // Toolbar
        AppBarLayout appBarLayout = findViewById(R.id.sensor_channel_info_app_bar_layout);
        Toolbar mainToolbar;
        Toolbar supplierToolbar = appBarLayout.findViewById(R.id.toolbar_supplier);
        Toolbar customerToolbar = appBarLayout.findViewById(R.id.toolbar_customer);

        // NavigationView + Clear menu
        NavigationView navigationView = findViewById(R.id.nav_view_sensor_channel_info);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_sensor_channel_info);
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
            String customerCode = Utils.getValueFromSharedPreferences(Constants.keyCustomerCode, SensorsChannelInfoActivity.this);
            Utils.getCustomerProfileFromDatabase(customerCode, txtName, txtEmail, imgProfile);
        }
        // Only supplier has notifications bell
        else {
            String email = Utils.getValueFromSharedPreferences(Constants.keyEmail, SensorsChannelInfoActivity.this);
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
        channelId = Integer.parseInt(b.getString(getString(R.string.channel_id_field)));
        customerCode = b.getString(getString(R.string.customer_code_field));

        TextView sensorChannelTitle = findViewById(R.id.sensor_channel_title);
        String newText = getString(R.string.sensor_module_title) + " " + sensorId + getString(R.string.sensor_channel_title) + " " + channelId;
        sensorChannelTitle.setText(newText);
        graphView = findViewById(R.id.sensor_data_history_graph);

        historyDataNumberTxtView = findViewById(R.id.dataHistoryNumber);
        Button historyDataBtn = findViewById(R.id.btnChangeDataHistoryNumber);

        // Default number of data history
        int defaultNumberOfDataHistory = 24;
        historyDataNumberTxtView.setText(String.valueOf(defaultNumberOfDataHistory));

        // Get data from MySQL database and build the graphic
        int nrOfDefaultDataHistory = Integer.parseInt(historyDataNumberTxtView.getText().toString());
        getSensorsHistoryDataAndBuildGraphic(nrOfDefaultDataHistory);

        // Action when click on get history button -> Get from MySQL database the latest X registrations
        historyDataBtn.setOnClickListener(v -> {
            int nrOfDataHistory = Integer.parseInt(historyDataNumberTxtView.getText().toString());
            graphView.removeAllSeries();
            getSensorsHistoryDataAndBuildGraphic(nrOfDataHistory);
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        Bundle b = new Bundle();
        b.putString(getString(R.string.sensor_id_field), String.valueOf(sensorId));
        b.putString(getString(R.string.customer_code_field), String.valueOf(customerCode));
        if (userType.equals(Constants.SUPPLIER)) {
            intent = new Intent(this, SensorsModuleInfoActivity.class);
        } else {
            intent = new Intent(this, CustomerDashboardActivity.class);
        }
        intent.putExtras(b);
        finish();
        startActivity(intent);
    }

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
            Toast.makeText(getApplicationContext(), R.string.logout_successfully, Toast.LENGTH_SHORT).show();
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout_sensor_channel_info);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Get sensors data (latest X) from MySQL database and build the graphic
     */
    private void getSensorsHistoryDataAndBuildGraphic(int nrOfDataHistory) {

        Callback<HistoryRawData> callback = new Callback<HistoryRawData>() {
            @Override
            public void onResponse(@NonNull Call<HistoryRawData> call, Response<HistoryRawData> response) {
                assert response.body() != null;
                List<HistoryData> historyDataList = response.body().getData();

                ArrayList<DataPoint> graphDataList = new ArrayList<>();
                for (HistoryData historyData : historyDataList) {
                    double waterFlow = historyData.getValue();
                    long timestamp = Utils.convertDateFromCustomFormatToMilliseconds(historyData.getTimestamp(), Constants.TIMESTAMPS_DATES_FROM_MYSQL_DB);
                    DataPoint dataPoint = new DataPoint(timestamp, waterFlow);
                    graphDataList.add(dataPoint);
                }

                graphDataList.sort((o1, o2) -> {
                    if (o1.getX() >= o2.getX()) {
                        return 1;
                    } else if (o1.getX() == o2.getX()) {
                        return 0;
                    } else {
                        return -1;
                    }
                });

                // Get min and max DataPoint by Y value (to set Y bounds).
                // The X bounds will be first and last element from array because data from array is sorted ascending by X
                minPoint = Collections.min(graphDataList, Comparator.comparingDouble(DataPoint::getY));
                maxPoint = Collections.max(graphDataList, Comparator.comparingDouble(DataPoint::getY));

                // Convert from list to array of DataPoint
                points = convertDataPointsListToArray(graphDataList);

                // Create and add series to graph
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
                series.setThickness(8);
                series.setDrawDataPoints(true);
                series.setDataPointsRadius(15);
                graphView.addSeries(series);

                buildLegend(series);
                if (!wasGraphicCustomized) {
                    customizeGraphic(minPoint, maxPoint, points);
                }

                graphView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<HistoryRawData> call, @NonNull Throwable t) {

            }
        };

        ApiManager.getChannelDataHistoryBySensorIdAndChannelId(sensorId, channelId, nrOfDataHistory, Constants.LAST_TIME_HOURS_MODE, callback);
    }

    /**
     * Customize the graphic
     */
    private void customizeGraphic(DataPoint minPoint, DataPoint maxPoint, DataPoint[] points) {
        setBounds(minPoint, maxPoint, points);
        enableScalingAndScrolling();
        changeLabelFormat();
        changeLabelColorAndTitle();
        wasGraphicCustomized = true;
    }

    /**
     * Convertion from list of points to array of points
     */
    @NonNull
    private DataPoint[] convertDataPointsListToArray(ArrayList<DataPoint> graphDataList) {
        DataPoint[] points = new DataPoint[graphDataList.size()];
        for (int i = 0; i < graphDataList.size(); i++) {
            points[i] = graphDataList.get(i);
        }
        return points;
    }

    /**
     * Change labels color, title and title color
     */
    private void changeLabelColorAndTitle() {
        // Y axis
        graphView.getGridLabelRenderer().setVerticalLabelsColor(getColor(R.color.dark_red));
        graphView.getGridLabelRenderer().setVerticalAxisTitle("WaterFlow (l/h)");
        graphView.getGridLabelRenderer().setVerticalAxisTitleTextSize(45);
        graphView.getGridLabelRenderer().setVerticalAxisTitleColor(getColor(R.color.red));

        // X axis
        graphView.getGridLabelRenderer().setHorizontalLabelsColor(getColor(R.color.dark_blue));
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        graphView.getGridLabelRenderer().setHorizontalAxisTitleTextSize(45);
        graphView.getGridLabelRenderer().setHorizontalAxisTitleColor(getColor(R.color.blue));
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }

    /**
     * Change labels format
     */
    private void changeLabelFormat() {
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                // Label for X values
                if (isValueX) {
                    return Utils.convertDateFromMillisecondsToCustomFormat((long) value, Constants.TIMESTAMPS_GRAPH_DATE_FORMAT);
                }
                // Label for Y values
                else {
                    return super.formatLabel(value, false);
                }
            }
        });
    }

    /**
     * Build graphic legend
     */
    private void buildLegend(LineGraphSeries<DataPoint> series) {
        series.setTitle("Water Flow");
        series.setColor(getColor(R.color.dark_green));
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    /**
     * Enable scaling and scrolling
     */
    private void enableScalingAndScrolling() {
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(true);
        graphView.getViewport().setScalable(true); // enables horizontal scrolling
        graphView.getViewport().setScalableY(true);
    }

    /**
     * Set bounds
     */
    private void setBounds(DataPoint minPoint, DataPoint maxPoint, DataPoint[] points) {
        // Set manual Y bounds
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(minPoint.getY());
        graphView.getViewport().setMaxY(maxPoint.getY());

        // Set manual X bounds
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(points[0].getX());
        graphView.getViewport().setMaxX(points[points.length - 1].getX());
    }
}
