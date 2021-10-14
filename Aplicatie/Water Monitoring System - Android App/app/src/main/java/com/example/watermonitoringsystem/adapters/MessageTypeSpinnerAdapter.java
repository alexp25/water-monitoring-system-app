package com.example.watermonitoringsystem.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.utils.NotificationType;
/**
 * Adapter for an item from messages types
 *
 * @author Ioan-Alexandru Chirita
 */
public class MessageTypeSpinnerAdapter extends BaseAdapter {
    Context context;
    NotificationType[] notificationTypes;
    LayoutInflater inflater;

    public MessageTypeSpinnerAdapter(Context applicationContext, NotificationType[] notificationTypes) {
        this.context = applicationContext;
        this.notificationTypes = notificationTypes;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return notificationTypes.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.item_custom_complaints_spinner, null);
        ImageView notificationTypeIcon = view.findViewById(R.id.spinnerImage);
        TextView notificationTypeText = view.findViewById(R.id.spinnerTxtView);
        notificationTypeIcon.setImageResource(notificationTypes[i].getIconValue());
        notificationTypeText.setText(notificationTypes[i].name());
        notificationTypeText.setTextColor(view.getContext().getResources().getColor(notificationTypes[i].getColorCode()));
        return view;
    }
}