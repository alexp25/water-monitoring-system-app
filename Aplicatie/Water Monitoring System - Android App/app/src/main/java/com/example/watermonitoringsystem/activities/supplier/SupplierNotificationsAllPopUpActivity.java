package com.example.watermonitoringsystem.activities.supplier;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.adapters.NotificationsAdapter;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.NotificationData;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * PopUp with all notifications received by supplier
 *
 * @author Ioan-Alexandru Chirita
 */
public class SupplierNotificationsAllPopUpActivity extends AppCompatActivity {

    private ArrayList<NotificationData> notificationDataList;
    private PopupWindow popupWindow;
    private NotificationsAdapter notificationsAdapter;
    private ListView notificationsListView;
    private TextView notificationsTextView;

    /**
     * PopupWindow display method
     */
    public void showPopupWindow(final View view) {

        // Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.supplier_notifications_all, null);

        // Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        // Create a window with our parameters
        popupWindow = new PopupWindow(popupView, width, height, true);

        // Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Close current pop up
        Button closePopUpButton = popupView.findViewById(R.id.close_notifications_button);
        closePopUpButton.setOnClickListener(v -> popupWindow.dismiss());

        notificationsListView = popupView.findViewById(R.id.supplier_notifications_list_view);
        notificationsTextView = popupView.findViewById(R.id.supplier_notifications_text_view);
        notificationDataList = new ArrayList<>();

        // Notifications list adapter
        notificationsAdapter = new NotificationsAdapter(notificationDataList, popupView.getContext());
        notificationsListView.setAdapter(notificationsAdapter);

        // Update notifications data. Get notifications from Firebase database
        getNotificationsFromDatabase();

        // Action on Simple Click on Notification List Item -> Open notification information
        notificationsListView.setOnItemClickListener((parent, view12, position, id) -> openNotificationInfo(position, view12));

        // Action on Long Click on Notification List Item -> Open popup options (Mark As Read/Unread, Delete, Close)
        notificationsListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            openOptions(position, view1);
            return true;
        });
    }

    private void openNotificationInfo(int position, View v) {
        NotificationData notificationData = notificationDataList.get(position);
        NotificationActivity popUpClass = new NotificationActivity(notificationData);
        markAsReadInDatabase(notificationData);
        popupWindow.dismiss();
        popUpClass.showPopupWindow(v);
    }

    private void openOptions(int position, View v) {
        NotificationData notificationData = notificationDataList.get(position);
        SupplierNotificationOptionsPopUpActivity popUpClass = new SupplierNotificationOptionsPopUpActivity(notificationData);
        popupWindow.dismiss();
        popUpClass.showPopupWindow(v);
    }

    private void getNotificationsFromDatabase() {
        Database.getNotificationsEndpoint().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    notificationsListView.setVisibility(View.INVISIBLE);
                    notificationsTextView.setVisibility(View.VISIBLE);
                } else {
                    notificationsListView.setVisibility(View.VISIBLE);
                    notificationsTextView.setVisibility(View.INVISIBLE);
                    notificationDataList.clear();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        notificationDataList.add(postSnapshot.getValue(NotificationData.class));
                    }
                    // Notifications are in the reverse order by timestamp
                    // When a new notification is added by the customer, it will be the last in the list
                    // So we should reverse the list in order to have the latest received notification as first notification in list
                    Collections.reverse(notificationDataList);

                    notificationsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.displayToastErrorDatabase(getApplicationContext());
                Log.e(String.valueOf(R.string.title_supplier_notifications_popup_window), databaseError.getMessage());
            }
        });
    }

    private void markAsReadInDatabase(NotificationData notificationData) {
        DatabaseReference notificationsEndpoint = Database.getNotificationsEndpoint();
        notificationData.setRead(true);
        notificationsEndpoint.child(String.valueOf(notificationData.getNotificationId())).setValue(notificationData, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Log.e(getString(R.string.title_register_fragment), databaseError.getCode() + ": " + databaseError.getMessage());
            }
        });
        notificationsEndpoint.push();
    }
}

