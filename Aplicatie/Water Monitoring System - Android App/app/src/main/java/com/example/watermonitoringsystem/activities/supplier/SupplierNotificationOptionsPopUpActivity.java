package com.example.watermonitoringsystem.activities.supplier;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.NotificationData;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.firebase.database.DatabaseReference;

/**
 * PopUp with options for a notification - Mark as unread/read, Delete or Close
 *
 * @author Ioan-Alexandru Chirita
 */
public class SupplierNotificationOptionsPopUpActivity extends AppCompatActivity {

    private final NotificationData notificationData;
    private PopupWindow popupWindow;

    public SupplierNotificationOptionsPopUpActivity(NotificationData notificationData) {
        this.notificationData = notificationData;
    }

    public void showPopupWindow(final View view) {

        // Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.notification_options, null);

        // Specify the length and width through constants
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        // Create a window with our parameters
        popupWindow = new PopupWindow(popupView, width, height, true);

        // Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Initialize the elements of our window, install the handler
        Button markReadUnreadBtn = popupView.findViewById(R.id.notifications_option_read_unread_btn);
        Button markDeleteBtn = popupView.findViewById(R.id.notifications_option_delete_btn);
        Button markCloseBtn = popupView.findViewById(R.id.notifications_option_close_btn);

        // If notification is read, we can mark as unread
        if (notificationData.isRead()) {
            markReadUnreadBtn.setText(R.string.mark_as_unread);
        }
        // If notification is unread, we can mark as read
        else {
            markReadUnreadBtn.setText(R.string.mark_as_read);
        }

        // Update notification as read/unread property into Firebase database and close the popup
        markReadUnreadBtn.setOnClickListener(v -> {
            notificationData.setRead(!notificationData.isRead());

            DatabaseReference notificationEndpoint = Database.getNotificationsEndpoint();
            notificationEndpoint.child(String.valueOf(notificationData.getNotificationId())).setValue(notificationData, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    Toast.makeText(v.getContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                    Log.e(getString(R.string.title_notification_option_popup_window), databaseError.getMessage());
                }
            });

            notificationEndpoint.push();
            popupWindow.dismiss();
            Utils.openNotificationsPopUp(v);
        });

        // Delete notification from Firebase database and close the popup (after confirmation delete) or just close popup
        markDeleteBtn.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.delete_notification_title)
                .setMessage(R.string.delete_notification_message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    DatabaseReference notificationEndpoint = Database.getNotificationsEndpoint();
                    notificationEndpoint.child(String.valueOf(notificationData.getNotificationId())).removeValue();
                    popupWindow.dismiss();
                    Utils.openNotificationsPopUp(v);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    popupWindow.dismiss();
                    Utils.openNotificationsPopUp(v);
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show());

        // Close the popup
        markCloseBtn.setOnClickListener(v -> {
            popupWindow.dismiss();
            Utils.openNotificationsPopUp(v);
        });
    }
}