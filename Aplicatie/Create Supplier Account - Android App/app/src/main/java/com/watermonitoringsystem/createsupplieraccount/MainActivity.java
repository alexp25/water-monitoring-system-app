package com.watermonitoringsystem.createsupplieraccount;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView fullNameView;
    private TextView emailView;
    private TextView passwordView;
    private boolean exist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fullNameView = findViewById(R.id.fullName);
        emailView = findViewById(R.id.email);
        passwordView = findViewById(R.id.password);

        Button btnCreate = findViewById(R.id.btn_create);

        btnCreate.setOnClickListener(v -> createSupplierAccount());
    }

    private void createSupplierAccount() {
        try {
            final SupplierData supplier = new SupplierData();
            supplier.setFullName(fullNameView.getText().toString());
            supplier.setEmail(emailView.getText().toString());
            String passEncrypted = AESCrypt.encrypt(passwordView.getText().toString());
            supplier.setPassword(passEncrypted);
            supplier.setProfilePictureIdentifier(Constants.SUPPLIER_DEFAULT_PROFILE_PICTURE);

            final String key = supplier.getFullName().replace(" ", "").replace("-", "").replace(".", "");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.SUPPLIERS_DB);

            databaseReference.orderByChild("email").equalTo(supplier.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    // Supplier already registered into database
                    if (dataSnapshot.exists() && !exist) {
                        Toast.makeText(getApplicationContext(), "Email already exist in database!", Toast.LENGTH_LONG).show();
                    }
                    // Supplier does not exist into database, so he could be inserted
                    else {
                        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child(Constants.SUPPLIERS_DB);
                        databaseReference2.child(key).setValue(supplier, (databaseError, databaseReference1) -> {
                            if (databaseError == null) {
                                Toast.makeText(getApplicationContext(), "New supplier account created!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "ErrorCode: " + databaseError.getCode(), Toast.LENGTH_LONG).show();
                                Log.e("SupplierCreateAccount", databaseError.getMessage());
                            }
                        });
                        databaseReference2.push();
                        exist = true;
                    }
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                    Log.e("SupplierCreateAccount", databaseError.getMessage());
                }
            });
            databaseReference.push();

        } catch (Exception e) {
            Log.e("SupplierCreateAccount", Objects.requireNonNull(e.getMessage()));
        }
    }
}