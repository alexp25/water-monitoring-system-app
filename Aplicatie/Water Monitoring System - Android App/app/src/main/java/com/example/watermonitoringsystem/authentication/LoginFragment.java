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
    private MqttReceiverThread mqttReceiverThread;
    private static MqttSenderThread mqttSenderThread;
    private boolean isMqttStarted = false;


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

        @SuppressLint("UseSwitchCompatOrMaterialCode") final Switch cSwitch = view.findViewById(R.id.cSwitch);
        final TextView cSwitchText = view.findViewById(R.id.cSwitch_textView);
        final ConstraintLayout constraintLayout = view.findViewById(R.id.switchLayout);

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

    }

    @Override
    public void onResume() {
        super.onResume();
        stopMqttConnections();
    }

    public void updateLoginData(String email, String password) {
        mEmailLogin.setText(email);
        mPasswordLogin.setText(password);
    }

    private void startReceiverThread() {
        this.mqttReceiverThread = new MqttReceiverThread();
        this.mqttReceiverThread.start();
    }

    private void startSenderThread() {
        mqttSenderThread = new MqttSenderThread();
        mqttSenderThread.start();
    }


    private void stopMqttConnections() {
        Log.e("TEST", "STOP CALLED");
        if (isMqttStarted) {
            Log.e("TEST", "STOP CALLED inside");
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

        //if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
        //    Toast.makeText(requireView().getContext(), R.string.email_and_password_cannot_be_empty, Toast.LENGTH_SHORT).show();
        //    return;
        //}

        // Supplier
        if (isSupplierSelected) {
            Log.e("LOGIN", "Login as supplier");
            email = "supplier@test.com";
            password = "Qwerty12";
            loginAsSupplier(email, password);
        }
        // Customer
        else {
            Log.e("LOGIN", "Login as customer");
            email = "alexxchirita13@gmail.com";
            password = "Qwerty1234";
            loginAsCustomer(email, password);
        }
    }

    private void loginAsCustomer(final String email, final String password) {
        Database.getCustomerEndpoint().orderByChild(getString(R.string.email_firebase_field)).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CustomerData returnedUser = postSnapshot.getValue(CustomerData.class);
                        assert returnedUser != null;
                        String decryptedPassword;
                        try {
                            decryptedPassword = AESCrypt.decrypt(returnedUser.getPassword());
                            Log.e("LOGIN", "DecryptedPasseword: " + decryptedPassword);
                            Log.e("LOGIN", "GivenPassword: " + password);

                        } catch (Exception e) {
                            Log.e(String.valueOf(R.string.title_login_fragment), Objects.requireNonNull(e.getMessage()));
                            return;
                        }

                        // Inserted password is not the same with the password for this account from database
                        if (!password.equals(decryptedPassword)) {
                            Toast.makeText(requireView().getContext(), R.string.wrong_email_or_password_customer, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireView().getContext(), R.string.login_successfully, Toast.LENGTH_SHORT).show();
                            Utils.saveValueToSharedPreferences(Constants.keyUserType, Constants.CUSTOMER, requireContext());
                            Utils.saveValueToSharedPreferences(Constants.keyFullName, returnedUser.getFullName(), requireContext());
                            Utils.saveValueToSharedPreferences(Constants.keyEmail, returnedUser.getEmail(), requireContext());
                            Utils.saveValueToSharedPreferences(Constants.keyCustomerCode, returnedUser.getCustomerCode(), requireContext());
                            Utils.saveValueToSharedPreferences(Constants.keyProfilePicture, returnedUser.getProfilePictureIdentifier(), requireContext());

                            Intent intent = new Intent(requireContext(), CustomerDashboardActivity.class);
                            isMqttStarted = true;
                            startReceiverThread();
                            startSenderThread();

                            mEmailLogin.setText("");
                            mPasswordLogin.setText("");
                            startActivity(intent);
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
                // Supplier does not exist into database => login failed
                if (!dataSnapshot.exists()) {
                    Toast.makeText(requireView().getContext(), R.string.wrong_email_or_password_supplier, Toast.LENGTH_SHORT).show();
                }
                // Supplier found on the database => login ok
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SupplierData returnedSupplier = postSnapshot.getValue(SupplierData.class);
                        try {
                            assert returnedSupplier != null;
                            String decryptedPassword = AESCrypt.decrypt(returnedSupplier.getPassword());
                            Log.e("LOGIN", "DecryptedPasseword: " + decryptedPassword);
                            Log.e("LOGIN", "GivenPassword: " + password);
                            // Inserted password is not the same with the password for this account from database
                            if (!password.equals(decryptedPassword)) {
                                Toast.makeText(requireView().getContext(), R.string.wrong_email_or_password_supplier, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireView().getContext(), R.string.login_successfully, Toast.LENGTH_SHORT).show();
                                Utils.saveValueToSharedPreferences(Constants.keyUserType, Constants.SUPPLIER, requireContext());
                                Utils.saveValueToSharedPreferences(Constants.keyFullName, returnedSupplier.getFullName(), requireContext());
                                Utils.saveValueToSharedPreferences(Constants.keyEmail, returnedSupplier.getEmail(), requireContext());
                                Utils.saveValueToSharedPreferences(Constants.keyProfilePicture, returnedSupplier.getProfilePictureIdentifier(), requireContext());

                                final Intent intent = new Intent(requireContext(), SupplierSensorsMapActivity.class);
                                isMqttStarted = true;
                                startReceiverThread();
                                startSenderThread();

                                mEmailLogin.setText("");
                                mPasswordLogin.setText("");
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            Log.e(String.valueOf(R.string.title_login_fragment), Objects.requireNonNull(e.getMessage()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(String.valueOf(R.string.title_login_fragment), databaseError.getMessage());
            }
        });
    }
}
