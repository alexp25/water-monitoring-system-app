package com.example.watermonitoringsystem.activities.supplier;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.CustomerData;
import com.example.watermonitoringsystem.models.firebasedb.NotificationData;
import com.example.watermonitoringsystem.utils.NotificationType;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

/**
 * Notification information activity
 *
 * @author Ioan-Alexandru Chirita
 */
public class NotificationActivity extends AppCompatActivity {

    private final NotificationData notificationData;
    private PopupWindow popupWindow;
    private TextView notificationCustomerFullName;
    private TextView notificationEmail;

    public NotificationActivity(NotificationData notificationData) {
        this.notificationData = notificationData;
    }

    public void showPopupWindow(final View view) {

        // Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.supplier_notification, null);

        // Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        // Create a window with our parameters
        popupWindow = new PopupWindow(popupView, width, height, true);

        // Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Initialize the elements of our window, install the handler
        Button backBtn = popupView.findViewById(R.id.notifications_back_btn);
        notificationCustomerFullName = popupView.findViewById(R.id.notification_customerFullName);
        notificationEmail = popupView.findViewById(R.id.notification_mail);
        TextView notificationDate = popupView.findViewById(R.id.notification_date);
        ImageView dotImage = popupView.findViewById(R.id.dotImage);
        TextView notificationType = popupView.findViewById(R.id.notification_type);
        TextView notificationSubject = popupView.findViewById(R.id.notification_subject);
        TextView notificationMessage = popupView.findViewById(R.id.notification_message);

        notificationType.setText(notificationData.getType());
        notificationSubject.setText(notificationData.getSubject());
        notificationMessage.setText(notificationData.getMessage());
        notificationDate.setText(notificationData.getDate());

        // Set notification type icon
        int icon = NotificationType.getIconValueByNotificationType(notificationData.getType());
        dotImage.setBackgroundResource(icon);

        // Get received notification information from Firebase database
        getNotificationFromDb(view, notificationData.getCustomerCode());

        // Action when click on Back button => Return to all notifications popup
        backBtn.setOnClickListener(v -> {
            popupWindow.dismiss();
            Utils.openNotificationsPopUp(v);
        });

        // Action when click on customer's email text (the customer who sent the notification) => Open email phone application in order to send an answer
        notificationEmail.setOnClickListener(this::openEmailApplication);
    }

    @Override
    public void onBackPressed() {
    }

    /**
     * Get received notification information from Firebase database
     */
    private void getNotificationFromDb(final View view, final String customerCode) {
        String customerCodeKey = view.getResources().getString(R.string.customer_code_firebase_field);
        Database.getCustomerEndpoint().orderByChild(customerCodeKey).equalTo(customerCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // Customer is registered on the database
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CustomerData customerData = postSnapshot.getValue(CustomerData.class);
                        assert customerData != null;
                        notificationEmail.setText(customerData.getEmail());
                        notificationCustomerFullName.setText(customerData.getFullName());
                    }
                }
                // Customer is not registered into database
                else {
                    Toast.makeText(view.getContext(), view.getResources().getString(R.string.customer_does_not_exist_into_database), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Toast.makeText(view.getContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                Log.e(view.getResources().getString(R.string.title_supplier_notifications_popup_window), databaseError.getMessage());
            }
        });
    }

    /**
     * Open email phone application in order to send an answer to the customer
     */
    private void openEmailApplication(View view) {
        String email = notificationEmail.getText().toString();
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        view.getContext().startActivity(Intent.createChooser(intent, "Choose an Email client :"));
    }
}