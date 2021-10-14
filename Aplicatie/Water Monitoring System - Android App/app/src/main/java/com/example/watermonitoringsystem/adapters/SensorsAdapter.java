package com.example.watermonitoringsystem.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.models.app.ChannelsData;

import java.util.ArrayList;

/**
 * Adapter for an item from sensor's channels list - common for suppliers and customers
 *
 * @author Ioan-Alexandru Chirita
 */
public class SensorsAdapter extends ArrayAdapter<ChannelsData> implements View.OnClickListener {
    Context mContext;

    public SensorsAdapter(ArrayList<ChannelsData> dataSet, Context context) {
        super(context, R.layout.item_sensors_channels, dataSet);
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
    }

    @SuppressLint({"ResourceAsColor", "ViewHolder"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ChannelsData sensorModuleData = getItem(position);
        SensorViewHolder sensorViewHolder;
        final View view;

        sensorViewHolder = new SensorViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.item_sensors_channels, parent, false);

        // Get views
        sensorViewHolder.sensorIdTxtView = view.findViewById(R.id.sensor_id);
        sensorViewHolder.waterFlowDataTxtView = view.findViewById(R.id.sensor_flow_data);

        // Set SensorId & WaterFlow on views
        sensorViewHolder.sensorIdTxtView.setText(String.valueOf(sensorModuleData.getChannelId()));
        sensorViewHolder.waterFlowDataTxtView.setText(String.valueOf(sensorModuleData.getWaterFlowValue()));

        sensorViewHolder.sensorIdTxtView.setTag(position);
        view.setTag(sensorViewHolder);

        return view;
    }

    /**
     * ViewHolder for sensor channel item
     */
    private static class SensorViewHolder {
        TextView sensorIdTxtView;
        TextView waterFlowDataTxtView;
    }
}
