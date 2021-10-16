package com.example.watermonitoringsystem.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.customer.CustomerPersonalProfileActivity;
import com.example.watermonitoringsystem.activities.supplier.SupplierNotificationsAllPopUpActivity;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.firebase.FirebaseConstants;
import com.example.watermonitoringsystem.firebase.Storage;
import com.example.watermonitoringsystem.models.firebasedb.SupplierData;
import com.example.watermonitoringsystem.models.firebasedb.CustomerData;
import com.example.watermonitoringsystem.models.app.ElectrovalvesData;
import com.example.watermonitoringsystem.models.firebasedb.NotificationData;
import com.example.watermonitoringsystem.mqtt.MqttConstants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class Utils implements MqttConstants, Constants {

    public static void saveValueToSharedPreferences(SharedPrefsKeys key, String value, Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor Ed = sp.edit();
        Ed.putString(key.getKeyValue(), value);
        Ed.apply();
    }

    public static String getValueFromSharedPreferences(SharedPrefsKeys key, Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sp.getString(key.getKeyValue(), "");
    }

    public static void clearSharedPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor Ed = sp.edit();
        Ed.clear();
        Ed.apply();
    }

    public static String composeCustomerProfilePictureName(String customerCode, String customerFullName) {
        String fullName = customerFullName.replace(" ", "");
        fullName = fullName.replace("-", "");
        fullName = fullName.replace(".", "");
        return FirebaseConstants.CUSTOMERS + "/" + customerCode + "_" + fullName + ".png";
    }

    public static String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        return random.ints(48, 123)  // upper letters, lower letters and digits
                .filter(i -> Character.isAlphabetic(i) || Character.isDigit(i))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    public static String[] getModuleIdAndCustomerCodeFromMarkerTitle(String markerTitle) {
        String[] markers = new String[2];
        String[] markerTitleParts = markerTitle.replace(" ", "").split(";");

        String[] markerTitleModuleIdParts = markerTitleParts[0].split(":");
        markers[0] = markerTitleModuleIdParts[1];

        String[] markerTitleCustomerCodeParts = markerTitleParts[1].split(":");
        markers[1] = markerTitleCustomerCodeParts[1];

        return markers;
    }


    public static void getCustomerProfileFromDatabase(String customerCode, final TextView fullNameTxtView, final TextView emailTextView, final CircleImageView imgProfile) {
        Database.getCustomerEndpoint().orderByChild("customerCode").equalTo(customerCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // Customer is registered on the database
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CustomerData customerDataFromDb = postSnapshot.getValue(CustomerData.class);
                        assert customerDataFromDb != null;

                        fullNameTxtView.setText(customerDataFromDb.getFullName());
                        emailTextView.setText(customerDataFromDb.getEmail());
                        downloadImageFromFirebaseStorage(customerDataFromDb.getProfilePictureIdentifier(), imgProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e("Error on getting customer from DB", databaseError.getMessage());
            }
        });
    }


    public static void getSupplierProfileFromDatabase(String email, final TextView fullNameTxtView, final TextView emailTextView, final CircleImageView imgProfile) {
        Database.getSupplierEndpoint().orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // Customer is registered on the database
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SupplierData supplierDataFromDb = postSnapshot.getValue(SupplierData.class);
                        assert supplierDataFromDb != null;

                        fullNameTxtView.setText(supplierDataFromDb.getFullName());
                        emailTextView.setText(supplierDataFromDb.getEmail());
                        downloadImageFromFirebaseStorage(supplierDataFromDb.getProfilePictureIdentifier(), imgProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(Resources.getSystem().getString(R.string.title_utils_class), databaseError.getMessage());
            }
        });
    }

    public static void downloadImageFromFirebaseStorage(String imgLocation, final ImageView imageView) {
        final StorageReference ref = Storage.getImagesReference().child(imgLocation);

        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, imageView.getWidth(), imageView.getHeight(), false));

        }).addOnFailureListener(exception -> {
        });
    }

    public static void uploadImageOnFirebaseStorage(String imgLocation, ImageView imageView, final Context appContext) {

        // Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 25, outStream);
        byte[] data = outStream.toByteArray();

        // Upload
        StorageReference ref = Storage.getImagesReference().child(imgLocation);
        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnFailureListener(exception -> Toast.makeText(appContext, "Cannot upload profile picture because image is not available or Firebase Storage is down!", Toast.LENGTH_LONG).show()).addOnSuccessListener(taskSnapshot -> Toast.makeText(appContext, "Image upload successfully!", Toast.LENGTH_SHORT).show());
    }

    public static void getNotificationsNumber(final ImageView redSquare, final TextView notificationNumber) {

        Database.getNotificationsEndpoint().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    redSquare.setVisibility(View.INVISIBLE);
                    notificationNumber.setVisibility(View.INVISIBLE);
                } else {
                    long number = 0;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        NotificationData notificationData = postSnapshot.getValue(NotificationData.class);
                        assert notificationData != null;
                        if (!notificationData.isRead()) {
                            number++;
                        }
                    }

                    if (number == 0) {
                        redSquare.setVisibility(View.INVISIBLE);
                        notificationNumber.setVisibility(View.INVISIBLE);
                    } else {
                        redSquare.setVisibility(View.VISIBLE);
                        notificationNumber.setVisibility(View.VISIBLE);
                        notificationNumber.setText(String.valueOf(number));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(String.valueOf(R.string.title_utils_class), databaseError.getMessage());
            }
        });

        redSquare.setVisibility(View.INVISIBLE);
        notificationNumber.setVisibility(View.INVISIBLE);

    }

    public static void openNotificationsPopUp(View v) {
        SupplierNotificationsAllPopUpActivity popUpClass = new SupplierNotificationsAllPopUpActivity();
        popUpClass.showPopupWindow(v);
    }


    public static void displayToastErrorDatabase(Context context) {
        Toast.makeText(context, R.string.error_retrieve_data, Toast.LENGTH_SHORT).show();
    }

    public static long getNotificationCurrentDateInMilliseconds() {
        return System.currentTimeMillis();
    }

    public static String convertDateFromMillisecondsToCustomFormat(long timeInMilliseconds, String format) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date date = new Date(timeInMilliseconds);
        return formatter.format(date);
    }

    public static long convertDateFromCustomFormatToMilliseconds(String dateString, String format) {
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return date.getTime();
    }

    /**
     * Build payload for switch electrovalve position command
     */
    public static String buildElectrovalvePayload(ElectrovalvesData electrovalveData) {
        int electrovalveId = electrovalveData.getElectrovalveId();
        int electrovalveValue = electrovalveData.getState().equals(Constants.OPEN) ? 100 : 0;
        return ELECTROVALVE_NODE_TYPE + MQTT_DATA_SEPARATOR + MODULE_ELECTRO + MQTT_DATA_SEPARATOR + SEND_COMMAND + MQTT_DATA_SEPARATOR + electrovalveId + MQTT_DATA_SEPARATOR + electrovalveValue;
    }

    /**
     * Build payload for set pump value command
     */
    public static String buildPumpPayload(int pumpValue) {
        return PUMP_NODE_TYPE + MQTT_DATA_SEPARATOR + MODULE_PUMP + MQTT_DATA_SEPARATOR + SEND_COMMAND + MQTT_DATA_SEPARATOR + PUMP_ID + MQTT_DATA_SEPARATOR + pumpValue;
    }

}
