package com.example.watermonitoringsystem.activities.common;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.api.ApiManager;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.models.sqldb.HistoryData;
import com.example.watermonitoringsystem.models.sqldb.HistoryRawData;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.android.material.appbar.AppBarLayout;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Graphic data activity - common for supplier and customer
 *
 * @author Ioan-Alexandru Chirita
 */
public class SensorsChannelInfoActivity extends AppCompatActivity {

    private String userType;
    private String customerCode;
    private int selectedMode;
    private int sensorId;
    private int channelId;

    private GraphView graphView;
    private TextView historyDataNumberTxtView;
    private Spinner selectionSpinner;

    private DataPoint minPoint;
    private DataPoint maxPoint;
    private DataPoint[] points;

    private boolean wasGraphicCustomized = false;



    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userType = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_USER_TYPE, SensorsChannelInfoActivity.this);

        setContentView(R.layout.sensors_channel_info_activity);

        // Toolbar
        AppBarLayout appBarLayout = findViewById(R.id.sensor_channel_info_app_bar_layout);
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
        mainToolbar.setNavigationOnClickListener(v -> finish());

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
        channelId = Integer.parseInt(b.getString(getString(R.string.channel_id_field)));
        customerCode = b.getString(getString(R.string.customer_code_field));

        TextView sensorChannelTitle = findViewById(R.id.sensor_channel_title);
        String newText = getString(R.string.sensor_module_title) + " " + sensorId + getString(R.string.sensor_channel_title) + " " + channelId;
        sensorChannelTitle.setText(newText);
        graphView = findViewById(R.id.sensor_data_history_graph);

        historyDataNumberTxtView = findViewById(R.id.dataHistoryNumber);
        Button historyDataBtn = findViewById(R.id.btnChangeDataHistoryNumber);


        // Creating the ArrayAdapter instance having the modes list
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.selection_mode_spinner, R.layout.item_mode_selection_spinner);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Getting the instance of Spinner
        selectionSpinner = findViewById(R.id.modeSpinner);

        //Setting the ArrayAdapter data on the Spinner
        selectionSpinner.setAdapter(arrayAdapter);
        selectionSpinner.setSelection(2); // default selection is lastTimeHour
        selectedMode = 3;

        selectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // lastCount selected
                        selectedMode = Constants.LAST_COUNT_MODE;
                        break;
                    case 1: // lastTimeSecond selected
                        selectedMode = Constants.LAST_TIME_SECONDS_MODE;
                        break;
                    case 2: //lastTimeHour selected
                        selectedMode = Constants.LAST_TIME_HOURS_MODE;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Default number of data history
        int defaultNumberOfDataHistory = Constants.DEFAULT_LIMIT_GRAPHIC_DATA;
        historyDataNumberTxtView.setText(String.valueOf(defaultNumberOfDataHistory));

        // Get data from MySQL database and build the graphic
        int nrOfDefaultDataHistory = Integer.parseInt(historyDataNumberTxtView.getText().toString());

        getSensorsHistoryDataAndBuildGraphic(nrOfDefaultDataHistory);

        // Action when click on get history button -> Get from MySQL database the latest X registrations
        historyDataBtn.setOnClickListener(v ->

        {
            int nrOfDataHistory = Integer.parseInt(historyDataNumberTxtView.getText().toString());
            graphView.removeAllSeries();
            getSensorsHistoryDataAndBuildGraphic(nrOfDataHistory);
        });
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
                if(graphDataList.size() == 0) {
                    Toast.makeText(SensorsChannelInfoActivity.this, "No datapoints", Toast.LENGTH_LONG).show();
                    return;
                }
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

        ApiManager.getChannelDataHistoryBySensorIdAndChannelId(sensorId, channelId, nrOfDataHistory, selectedMode, callback);
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
     * Conversion from list of points to array of points
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
