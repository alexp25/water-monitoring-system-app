package com.example.watermonitoringsystem.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.app.ElectrovalvesData;
import com.example.watermonitoringsystem.mqtt.MqttSenderThread;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Adapter for an item from electrovalves list
 *
 * @author Ioan-Alexandru Chirita
 */
public class ElectrovalvesAdapter extends ArrayAdapter<ElectrovalvesData> implements View.OnClickListener {
    Context mContext;

    public ElectrovalvesAdapter(ArrayList<ElectrovalvesData> dataSet, Context context) {
        super(context, R.layout.item_electrovalve, dataSet);
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
    }

    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables", "DefaultLocale", "ViewHolder"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ElectrovalvesData electrovalvesDataModel = getItem(position);
        final ElectrovalvesViewHolder electrovalveViewHolder;
        final View view;

        electrovalveViewHolder = new ElectrovalvesViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.item_electrovalve, parent, false);

        // Get views
        electrovalveViewHolder.electrovalveIdTxtView = view.findViewById(R.id.electrovalve_id);
        electrovalveViewHolder.latitudeTxtView = view.findViewById(R.id.gps_coordinate_latitude);
        electrovalveViewHolder.longitudeTxtView = view.findViewById(R.id.gps_coordinate_longitude);
        electrovalveViewHolder.electrovalveStateTextView = view.findViewById(R.id.electrovalve_switch_text);
        electrovalveViewHolder.electrovalveStateSwitchView = view.findViewById(R.id.electrovalve_switch);
        electrovalveViewHolder.switchConstraintLayout = view.findViewById(R.id.switchElectrovalveLayout);

        // Set ElectrovalveId, Latitude & Longitude on views
        electrovalveViewHolder.electrovalveIdTxtView.setText(String.valueOf(electrovalvesDataModel.getElectrovalveId()));
        electrovalveViewHolder.latitudeTxtView.setText(String.format(Constants.DOUBLE_GPS_COORDINATE_FORMAT, electrovalvesDataModel.getLatitude()));
        electrovalveViewHolder.longitudeTxtView.setText(String.format(Constants.DOUBLE_GPS_COORDINATE_FORMAT, electrovalvesDataModel.getLongitude()));

        // Set State on views
        final String electrovalveState = electrovalvesDataModel.getState();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(electrovalveViewHolder.switchConstraintLayout);
        assert electrovalveState != null;

        // State = OPEN
        if (electrovalveState.equals(Constants.OPEN)) {
            electrovalveViewHolder.electrovalveStateSwitchView.setChecked(true);
            switchToOpen(constraintSet, electrovalveViewHolder);
        }
        // State = CLOSE
        else {
            electrovalveViewHolder.electrovalveStateSwitchView.setChecked(false);
            switchToClose(constraintSet, electrovalveViewHolder);
        }
        constraintSet.applyTo(electrovalveViewHolder.switchConstraintLayout);

        // Action when the switch is pressed
        electrovalveViewHolder.electrovalveStateSwitchView.setOnCheckedChangeListener((buttonView, isChecked) -> switchPressed(isChecked, electrovalveViewHolder, electrovalvesDataModel));

        electrovalveViewHolder.electrovalveIdTxtView.setTag(position);
        view.setTag(electrovalveViewHolder);
        return view;
    }

    /**
     * Change electrovalve state when switch is pressed
     */
    private void switchPressed(boolean isChecked, ElectrovalvesViewHolder electrovalveViewHolder, ElectrovalvesData electrovalvesDataModel) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(electrovalveViewHolder.switchConstraintLayout);

        if (isChecked) {
            switchToOpen(constraintSet, electrovalveViewHolder);
            electrovalvesDataModel.setState(Constants.OPEN);
        } else {
            switchToClose(constraintSet, electrovalveViewHolder);
            electrovalvesDataModel.setState(Constants.CLOSE);
        }
        constraintSet.applyTo(electrovalveViewHolder.switchConstraintLayout);

        updateElectrovalveState(electrovalvesDataModel.getElectrovalveId(), electrovalvesDataModel);
    }

    /**
     * Electrovalve is switched to closed position
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchToClose(ConstraintSet constraintSet, ElectrovalvesViewHolder electrovalveViewHolder) {
        electrovalveViewHolder.electrovalveStateTextView.setText(mContext.getString(R.string.close_selected));
        constraintSet.connect(R.id.electrovalve_switch_text, ConstraintSet.RIGHT, R.id.electrovalve_switch, ConstraintSet.RIGHT, 0);
        constraintSet.connect(R.id.electrovalve_switch_text, ConstraintSet.LEFT, ConstraintSet.UNSET, ConstraintSet.LEFT, 0);
        electrovalveViewHolder.electrovalveStateSwitchView.setTrackDrawable(ContextCompat.getDrawable(mContext, R.drawable.shape_switch_track_red_open_close));
        electrovalveViewHolder.electrovalveStateSwitchView.setThumbDrawable(mContext.getDrawable(R.drawable.shape_switch_selector_red_open_close));
    }

    /**
     * Electrovalve is switched to opened position
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchToOpen(ConstraintSet constraintSet, ElectrovalvesViewHolder electrovalveViewHolder) {
        electrovalveViewHolder.electrovalveStateTextView.setText(mContext.getString(R.string.open_selected));
        constraintSet.connect(R.id.electrovalve_switch_text, ConstraintSet.LEFT, R.id.electrovalve_switch, ConstraintSet.LEFT, 0);
        constraintSet.connect(R.id.electrovalve_switch_text, ConstraintSet.RIGHT, ConstraintSet.UNSET, ConstraintSet.RIGHT, 0);
        electrovalveViewHolder.electrovalveStateSwitchView.setTrackDrawable(ContextCompat.getDrawable(mContext, R.drawable.shape_switch_track_green_open_close));
        electrovalveViewHolder.electrovalveStateSwitchView.setThumbDrawable(mContext.getDrawable(R.drawable.shape_switch_selector_green_open_close));
    }

    /**
     * Send new electrovalve state via MQTT
     */
    public void updateElectrovalveState(int electrovalveId, ElectrovalvesData electrovalveData) {

        /*
         * TODO: Remove the below lines between *****
         *  Nu mai actualizam in baza de date Firebase, ci doar schimbam fizic starea electrovalvei print MQTT,
         *  Serverul va prinde si el informatia trimisa prin MQTT catre electrovalva si va face update in baza de date MySQL
         */

        /******************************/
        String electrovalveIdString = String.valueOf(electrovalveId);
        DatabaseReference electrovalvesEndpoint = Database.getElectrovalvesEndpoint();
        electrovalvesEndpoint.child(electrovalveIdString).setValue(electrovalveData, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Log.e(String.valueOf(R.string.title_supplier_electrovalve_activity), databaseError.getMessage());
            }
        });
        electrovalvesEndpoint.push();
        /******************************/

        // Send via MQTT Broker
        String electrovalvePayload = Utils.buildElectrovalvePayload(electrovalveData);
        MqttSenderThread.publishToElectrovalveTopic(electrovalvePayload);
    }

    /**
     * ViewHolder for and electrovalves list item
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private static class ElectrovalvesViewHolder {
        TextView latitudeTxtView;
        TextView longitudeTxtView;
        TextView electrovalveIdTxtView;
        Switch electrovalveStateSwitchView;
        TextView electrovalveStateTextView;
        ConstraintLayout switchConstraintLayout;
    }
}

