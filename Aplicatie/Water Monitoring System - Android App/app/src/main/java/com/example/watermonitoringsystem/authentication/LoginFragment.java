package com.example.watermonitoringsystem.authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.watermonitoringsystem.MainActivity;
import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.customer.CustomerDashboardActivity;
import com.example.watermonitoringsystem.activities.supplier.SupplierSensorsMapActivity;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.CustomerData;
import com.example.watermonitoringsystem.models.firebasedb.SupplierData;
import com.example.watermonitoringsystem.mqtt.MqttReceiverThread;
import com.example.watermonitoringsystem.mqtt.MqttSenderThread;
import com.example.watermonitoringsystem.security.AESCrypt;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Main class for login fragment
 *
 * @author Ioan-Alexandru Chirita
 */
public class LoginFragment extends Fragment {

    private TextView mEmailLogin;
    private TextView mPasswordLogin;
    private static MqttReceiverThread mqttReceiverThread;
    private static MqttSenderThread mqttSenderThread;
    private static boolean isMqttStarted = false;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch cSwitch;
    private TextView cSwitchText;
    private ConstraintLayout constraintLayout;

    private boolean isSupplierSelected;

    public LoginFragment() {
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button btnLogin = view.findViewById(R.id.btn_login);

        mEmailLogin = view.findViewById(R.id.login_email);
        mPasswordLogin = view.findViewById(R.id.login_password);

        btnLogin.setOnClickListener(v -> loginBtnPressed());

        cSwitch = view.findViewById(R.id.cSwitch);
        cSwitchText = view.findViewById(R.id.cSwitch_textView);
        constraintLayout = view.findViewById(R.id.switchLayout);

        cSwitch.isChecked();
        cSwitchText.setText(getResources().getString(R.string.supplier_selected));
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.connect(R.id.cSwitch_textView, ConstraintSet.LEFT, R.id.cSwitch, ConstraintSet.LEFT, 0);
        constraintSet.connect(R.id.cSwitch_textView, ConstraintSet.RIGHT, ConstraintSet.UNSET, ConstraintSet.RIGHT, 0);
        cSwitch.setTrackDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.shape_switch_track_green_supplier_customer));
        cSwitch.setThumbDrawable(view.getResources().getDrawable(R.drawable.shape_switch_selector_green_supplier_customer));
        isSupplierSelected = true;

        cSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConstraintSet constraintSet1 = new ConstraintSet();
            constraintSet1.clone(constraintLayout);

            if (isChecked) {
                cSwitchText.setText(getResources().getString(R.string.supplier_selected));
                constraintSet1.connect(R.id.cSwitch_textView, ConstraintSet.LEFT, R.id.cSwitch, ConstraintSet.LEFT, 0);
                constraintSet1.connect(R.id.cSwitch_textView, ConstraintSet.RIGHT, ConstraintSet.UNSET, ConstraintSet.RIGHT, 0);
                cSwitch.setTrackDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.shape_switch_track_green_supplier_customer));
                cSwitch.setThumbDrawable(view.getResources().getDrawable(R.drawable.shape_switch_selector_green_supplier_customer));
                isSupplierSelected = true;
            } else {
                cSwitchText.setText(getResources().getString(R.string.customer_selected));
                constraintSet1.connect(R.id.cSwitch_textView, ConstraintSet.RIGHT, R.id.cSwitch, ConstraintSet.RIGHT, 0);
                constraintSet1.connect(R.id.cSwitch_textView, ConstraintSet.LEFT, ConstraintSet.UNSET, ConstraintSet.LEFT, 0);
                cSwitch.setTrackDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.shape_switch_track_red_supplier_customer));
                cSwitch.setThumbDrawable(view.getResources().getDrawable(R.drawable.shape_switch_selector_red_supplier_customer));
                isSupplierSelected = false;
            }
            constraintSet1.applyTo(constraintLayout);
        });

        TextView hereTxtView = view.findViewById(R.id.forget_password_text_here);
        hereTxtView.setOnClickListener(v -> startActivity(new Intent(getContext(), ForgetPassword.class)));

        String storedUserType = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_USER_TYPE, requireContext());

        if (storedUserType != null && !storedUserType.isEmpty()) {
            // A supplier account exist into local storage => Autologin
            if (storedUserType.equals(Constants.SUPPLIER)) {
                try {
                    String supplierEmail = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_EMAIL, requireContext());
                    String supplierPassword = AESCrypt.decrypt(Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_PASSWORD, requireContext()));
                    loginAsSupplier(supplierEmail, supplierPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // A customer account exist into local storage => Autologin
            if (storedUserType.equals(Constants.CUSTOMER)) {
                try {
                    String customerEmail = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_EMAIL, requireContext());
                    String customerPassword = AESCrypt.decrypt(Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_PASSWORD, requireContext()));
                    loginAsCustomer(customerEmail, customerPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMqttStarted) {
            Toast.makeText(getContext(), R.string.logout_successfully, Toast.LENGTH_SHORT).show();
            Utils.clearSharedPreferences(requireContext());
        }
        stopMqttConnections();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void updateLoginDataAfterRegistration(String email, String password) {
        mEmailLogin.setText(email);
        mPasswordLogin.setText(password);
        cSwitch.isChecked();
        cSwitchText.setText(getResources().getString(R.string.customer_selected));
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.connect(R.id.cSwitch_textView, ConstraintSet.RIGHT, R.id.cSwitch, ConstraintSet.RIGHT, 0);
        constraintSet.connect(R.id.cSwitch_textView, ConstraintSet.LEFT, ConstraintSet.UNSET, ConstraintSet.LEFT, 0);
        cSwitch.setTrackDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.shape_switch_track_red_supplier_customer));
        cSwitch.setThumbDrawable(getResources().getDrawable(R.drawable.shape_switch_selector_red_supplier_customer));
        isSupplierSelected = false;
    }

    private void startReceiverThread() {
        this.mqttReceiverThread = new MqttReceiverThread();
        this.mqttReceiverThread.start();
    }

    private void startSenderThread() {
        mqttSenderThread = new MqttSenderThread();
        mqttSenderThread.start();
    }


    public static void stopMqttConnections() {
        if (isMqttStarted) {
            isMqttStarted = false;
            mqttReceiverThread.setCallback(null);
            try {
                mqttReceiverThread.disconnect();
                mqttSenderThread.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            try {
                mqttReceiverThread.join();
                mqttSenderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void loginBtnPressed() {
        String email = mEmailLogin.getText().toString();
        String password = mPasswordLogin.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(requireView().getContext(), R.string.email_and_password_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // Supplier
        if (isSupplierSelected) {
            Log.e("LOGIN", "Login as supplier");
            loginAsSupplier(email, password);
        }
        // Customer
        else {
            Log.e("LOGIN", "Login as customer");
            loginAsCustomer(email, password);
        }
    }

    private void loginAsCustomer(final String email, final String password) {
        Database.getCustomerEndpoint().orderByChild(getString(R.string.email_firebase_field)).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // Customer exists into Firebase database
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CustomerData customerDataFromDb = postSnapshot.getValue(CustomerData.class);
                        assert customerDataFromDb != null;

                        try {
                            String decryptedPassword = AESCrypt.decrypt(customerDataFromDb.getPassword());
                            // Password is not the same with the password for this account from database
                            if (!password.equals(decryptedPassword)) {
                                Toast.makeText(requireView().getContext(), R.string.wrong_email_or_password_customer, Toast.LENGTH_SHORT).show();
                            }
                            // Everything is ok => Login successfully
                            else {
                                // Save login credentials to Shared Preferences in order to autologin next time
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_EMAIL, customerDataFromDb.getEmail(), requireContext());
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_PASSWORD, customerDataFromDb.getPassword(), requireContext());
                                // Save user data to Shared Preferences in order to complete fields from main account page
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_USER_TYPE, Constants.CUSTOMER, requireContext());
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_FULLNAME, customerDataFromDb.getFullName(), requireContext());
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_CUSTOMER_CODE, customerDataFromDb.getCustomerCode(), requireContext());
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_PROFILE_PICTURE, customerDataFromDb.getProfilePictureIdentifier(), requireContext());

                                // Start MQTT threads - sender/receiver
                                isMqttStarted = true;
                                startReceiverThread();
                                startSenderThread();

                                mEmailLogin.setText("");
                                mPasswordLogin.setText("");

                                // Go to customer main activity
                                Toast.makeText(requireView().getContext(), R.string.login_successfully, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(requireContext(), CustomerDashboardActivity.class);
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            Log.e(String.valueOf(R.string.title_login_fragment), Objects.requireNonNull(e.getMessage()));
                            return;
                        }
                    }
                } else {
                    Toast.makeText(requireView().getContext(), R.string.wrong_email_or_password_customer, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(String.valueOf(R.string.title_login_fragment), databaseError.getMessage());
            }
        });
    }

    private void loginAsSupplier(final String email, final String password) {
        Database.getSupplierEndpoint().orderByChild(getString(R.string.email_firebase_field)).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                // Supplier found on the database => login ok
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SupplierData supplierDataFromDb = postSnapshot.getValue(SupplierData.class);
                        assert supplierDataFromDb != null;

                        try {
                            String decryptedPassword = AESCrypt.decrypt(supplierDataFromDb.getPassword());
                            // Password is not the same with the password for this account from database
                            if (!password.equals(decryptedPassword)) {
                                Toast.makeText(requireView().getContext(), R.string.wrong_email_or_password_supplier, Toast.LENGTH_SHORT).show();
                            }
                            // Everything is ok => Login successfully
                            else {
                                // Save login credentials to Shared Preferences in order to autologin next time
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_EMAIL, supplierDataFromDb.getEmail(), requireContext());
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_PASSWORD, supplierDataFromDb.getPassword(), requireContext());
                                // Save user data to Shared Preferences in order to complete fields from main account page
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_USER_TYPE, Constants.SUPPLIER, requireContext());
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_FULLNAME, supplierDataFromDb.getFullName(), requireContext());
                                Utils.saveValueToSharedPreferences(SharedPrefsKeys.KEY_PROFILE_PICTURE, supplierDataFromDb.getProfilePictureIdentifier(), requireContext());

                                // Start MQTT threads - sender/receiver
                                isMqttStarted = true;
                                startReceiverThread();
                                startSenderThread();

                                mEmailLogin.setText("");
                                mPasswordLogin.setText("");

                                // Go to supplier main activity
                                final Intent intent = new Intent(requireContext(), SupplierSensorsMapActivity.class);
                                Toast.makeText(requireView().getContext(), R.string.login_successfully, Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            Log.e(String.valueOf(R.string.title_login_fragment), Objects.requireNonNull(e.getMessage()));
                        }
                    }
                }
                // Supplier does not exist into database => login failed
                else {
                    Toast.makeText(requireView().getContext(), R.string.wrong_email_or_password_supplier, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(String.valueOf(R.string.title_login_fragment), databaseError.getMessage());
            }
        });
    }
}
