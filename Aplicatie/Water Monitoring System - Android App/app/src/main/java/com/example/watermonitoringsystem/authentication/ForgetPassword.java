package com.example.watermonitoringsystem.authentication;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.CustomerData;
import com.example.watermonitoringsystem.security.AESCrypt;
import com.example.watermonitoringsystem.utils.GMailSender;
import com.example.watermonitoringsystem.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Reset customer password activity
 *
 * @author Ioan-Alexandru Chirita
 */
public class ForgetPassword extends AppCompatActivity {

    TextView emailForResetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password);

        emailForResetting = findViewById(R.id.email_for_resetting);
        Button resetPasswordBtn = findViewById(R.id.btn_reset_password);

        resetPasswordBtn.setOnClickListener(v -> {
            try {
                resetPassword();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Reset password function
     */
    private void resetPassword() {
        String emailAddress = emailForResetting.getText().toString();

        if (emailAddress.equals("")) {
            Toast.makeText(getApplicationContext(), R.string.password_resetting_error, Toast.LENGTH_SHORT).show();
        } else {
            String newPassword = sendNewPassword(emailAddress);
            updatePasswordIntoDatabase(emailAddress, newPassword);
            Toast.makeText(getApplicationContext(), R.string.password_resetting_successfully, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Send new password via email
     */
    private String sendNewPassword(String emailAddress) {
        final String newPassword = Utils.generateRandomPassword(10);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String emailSubject = getResources().getString(R.string.reset_password_subject);
        String emailBody = getResources().getString(R.string.reset_password_body_part1) + " "
                + emailAddress
                + getResources().getString(R.string.reset_password_body_part2) + " "
                + newPassword
                + getResources().getString(R.string.reset_password_body_part3) + " "
                + getResources().getString(R.string.support_email)
                + getResources().getString(R.string.reset_password_body_part4) + " "
                + getResources().getString(R.string.support_phone_number)
                + getResources().getString(R.string.reset_password_body_part5);

        GMailSender ms = new GMailSender(emailAddress, emailSubject, emailBody);
        ms.createEmailMessage();
        ms.sendEmail();
        return newPassword;
    }

    /**
     * Update new password into Firabase database
     */
    private void updatePasswordIntoDatabase(String email, String newPassword) {
        Database.getCustomerEndpoint().addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.email_not_found_into_database), Toast.LENGTH_SHORT).show();
                } else {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CustomerData customerData = postSnapshot.getValue(CustomerData.class);
                        assert customerData != null;
                        if (customerData.getEmail().equals(email)) {
                            try {
                                customerData.setPassword(AESCrypt.encrypt(newPassword));
                                Log.e("FORGET_PASSWORD", "RESET PASSWORD: " + newPassword + "   ---   " + AESCrypt.encrypt(newPassword));
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(getString(R.string.title_forget_password_activity), Objects.requireNonNull(e.getMessage()));
                            }
                            DatabaseReference customerEndpoint = Database.getCustomerEndpoint();
                            customerEndpoint.child(customerData.getCustomerCode()).setValue(customerData, (databaseError, databaseReference) -> {
                                if (databaseError != null) {
                                    Toast.makeText(getApplicationContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                                    Log.e(getString(R.string.title_forget_password_activity), databaseError.getMessage());
                                }
                            });
                            customerEndpoint.push();
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.displayToastErrorDatabase(getApplicationContext());
                Log.e(String.valueOf(R.string.title_forget_password_activity), databaseError.getMessage());
            }
        });
    }
}
