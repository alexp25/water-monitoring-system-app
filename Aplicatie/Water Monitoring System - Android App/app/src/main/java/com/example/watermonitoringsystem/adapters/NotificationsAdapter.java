package com.example.watermonitoringsystem.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.CustomerData;
import com.example.watermonitoringsystem.models.firebasedb.NotificationData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.example.watermonitoringsystem.R.id;
import static com.example.watermonitoringsystem.R.layout;

/**
 * Adapter for an item from notifications list - Only for suppliers
 *
 * @author Ioan-Alexandru Chirita
 */
public class NotificationsAdapter extends ArrayAdapter<NotificationData> implements View.OnClickListener {
    Context mContext;

    public NotificationsAdapter(ArrayList<NotificationData> dataSet, Context context) {
        super(context, layout.item_sensors_channels, dataSet);
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
    }

    @SuppressLint({"ResourceAsColor", "ViewHolder"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final NotificationData notificationDataModel = getItem(position);
        final NotificationsAdapter.NotificationViewHolder notificationViewHolder;
        final View view;

        notificationViewHolder = new NotificationsAdapter.NotificationViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(layout.item_notifications, parent, false);

        notificationViewHolder.layoutItemNotification = view.findViewById(R.id.layoutItemNotification);
        notificationViewHolder.timestamp = view.findViewById(id.notif_timestamp_text);
        notificationViewHolder.customerNameTxtView = view.findViewById(id.notif_customer_full_name_text);
        notificationViewHolder.subjectTxtView = view.findViewById(id.notif_subject_text);
        notificationViewHolder.messageTxtView = view.findViewById(id.notif_message_text);
        notificationViewHolder.notificationIdTxtViw = view.findViewById(id.notif_id);

        String customerCodeKey = view.getResources().getString(R.string.customer_code_firebase_field);
        Database.getCustomerEndpoint().orderByChild(customerCodeKey).equalTo(notificationDataModel.getCustomerCode()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // Customer is registered on the database
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CustomerData customerData = postSnapshot.getValue(CustomerData.class);
                        assert customerData != null;

                        notificationViewHolder.timestamp.setText(notificationDataModel.getDate());
                        String msgFrom = view.getResources().getString(R.string.from_field) + " " + customerData.getFullName();
                        notificationViewHolder.customerNameTxtView.setText(msgFrom);
                        notificationViewHolder.subjectTxtView.setText(notificationDataModel.getSubject());
                        notificationViewHolder.messageTxtView.setText(notificationDataModel.getMessage());
                        notificationViewHolder.notificationIdTxtViw.setText(String.valueOf(notificationDataModel.getNotificationId()));

                        changeTextStyleByReadUnread(notificationViewHolder, notificationDataModel.isRead());

                        notificationViewHolder.notificationIdTxtViw.setTag(position);
                        view.setTag(notificationViewHolder);
                    }
                }
                // Customer is not registered into database
                else {
                    Toast.makeText(getContext(), String.valueOf(R.string.customer_does_not_exist_into_database), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                Log.e(String.valueOf(R.string.title_supplier_notifications_popup_window), databaseError.getMessage());
            }
        });

        return view;
    }

    /**
     * Change notification style if it is read or unread
     */
    private void changeTextStyleByReadUnread(NotificationsAdapter.NotificationViewHolder notificationViewHolder, boolean isRead) {
        if (isRead) {
            notificationViewHolder.timestamp.setTypeface(notificationViewHolder.timestamp.getTypeface(), Typeface.NORMAL);
            notificationViewHolder.customerNameTxtView.setTypeface(notificationViewHolder.customerNameTxtView.getTypeface(), Typeface.NORMAL);
            notificationViewHolder.subjectTxtView.setTypeface(notificationViewHolder.subjectTxtView.getTypeface(), Typeface.NORMAL);
            notificationViewHolder.messageTxtView.setTypeface(notificationViewHolder.messageTxtView.getTypeface(), Typeface.NORMAL);
        } else {
            notificationViewHolder.timestamp.setTypeface(notificationViewHolder.timestamp.getTypeface(), Typeface.BOLD_ITALIC);
            notificationViewHolder.customerNameTxtView.setTypeface(notificationViewHolder.customerNameTxtView.getTypeface(), Typeface.BOLD);
            notificationViewHolder.subjectTxtView.setTypeface(notificationViewHolder.subjectTxtView.getTypeface(), Typeface.BOLD);
            notificationViewHolder.messageTxtView.setTypeface(notificationViewHolder.messageTxtView.getTypeface(), Typeface.BOLD_ITALIC);
        }
    }

    /**
     * ViewHolder for notification item
     */
    private static class NotificationViewHolder {
        LinearLayout layoutItemNotification;
        TextView timestamp;
        TextView customerNameTxtView;
        TextView subjectTxtView;
        TextView messageTxtView;
        TextView notificationIdTxtViw;
    }
}

