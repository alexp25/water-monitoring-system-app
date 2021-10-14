package com.example.watermonitoringsystem.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.models.app.SensorData;

import java.util.ArrayList;

/**
 * Adapter for an item from sensor list - Only for customers
 *
 * @author Ioan-Alexandru Chirita
 */
public class ModulesAdapter extends ArrayAdapter<SensorData> implements View.OnClickListener {
    Context mContext;

    public ModulesAdapter(ArrayList<SensorData> dataSet, Context context) {
        super(context, R.layout.item_sensors_module, dataSet);
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
    }

    @SuppressLint({"ResourceAsColor", "ViewHolder"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SensorData moduleData = getItem(position);
        ModuleViewHolder moduleViewHolder;
        final View view;

        moduleViewHolder = new ModuleViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.item_sensors_module, parent, false);

        // Get views
        moduleViewHolder.customerImageView = view.findViewById(R.id.customer_code_picture);
        moduleViewHolder.moduleIdTxtView = view.findViewById(R.id.module_id);
        moduleViewHolder.latitudeTxtView = view.findViewById(R.id.gps_coordinate_latitude);
        moduleViewHolder.longitudeTxtView = view.findViewById(R.id.gps_coordinate_longitude);
        moduleViewHolder.customerCodeTxtView = view.findViewById(R.id.customer_code);

        // Set SensorId, Latitude, Longitude & WaterFlow on views
        moduleViewHolder.moduleIdTxtView.setText(String.valueOf(moduleData.getSensorId()));
        moduleViewHolder.latitudeTxtView.setText(String.valueOf(moduleData.getLatitude()));
        moduleViewHolder.longitudeTxtView.setText(String.valueOf(moduleData.getLongitude()));

        // Set CustomerCode on views
        if (moduleData.getCustomerCode() == null || moduleData.getCustomerCode().isEmpty()) {
            moduleViewHolder.customerCodeTxtView.setText("-");
            moduleViewHolder.customerImageView.setBackgroundResource(R.drawable.icon_user_sensor_unavailable);
        } else {
            moduleViewHolder.customerCodeTxtView.setText(moduleData.getCustomerCode());
            moduleViewHolder.customerImageView.setBackgroundResource(R.drawable.icon_user_sensor);
        }

        moduleViewHolder.moduleIdTxtView.setTag(position);
        view.setTag(moduleViewHolder);
        return view;
    }

    /**
     * ViewHolder for sensor module item
     */
    private static class ModuleViewHolder {
        ImageView customerImageView;
        TextView moduleIdTxtView;
        TextView latitudeTxtView;
        TextView longitudeTxtView;
        TextView customerCodeTxtView;
    }
}
