package com.example.watermonitoringsystem.authentication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.firebase.FirebaseConstants;
import com.example.watermonitoringsystem.models.firebasedb.CustomerData;
import com.example.watermonitoringsystem.security.AESCrypt;
import com.example.watermonitoringsystem.utils.Constants;
import com.example.watermonitoringsystem.utils.Validator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class for registration process
 *
 * @author Ioan-Alexandru Chirita
 */
public class RegisterFragment extends Fragment {

    public interface FragmentRegisterListener {
        void onRegisterDone(String email, String password);
    }

    private FragmentRegisterListener listener;

    private TextView mFullName;
    private TextView mEmail;
    private TextView mPassword;
    private TextView mPasswordConf;
    private TextView mCustomerCode;
    private TextView mPhoneNumber;
    private TextView mBirthDayDate;

    private final Calendar myCalendar = Calendar.getInstance();
    private final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
            mBirthDayDate.setText(sdf.format(myCalendar.getTime()));
        }
    };

    private CountryCodePicker ccp;
    private volatile boolean areRegDataCompleted;
    private volatile View focusView = null;
    /*
     * Make sure that the Toast was displayed only when the data was not found in the database
     * because if we insert valid data then the message with "already exist" will be also
     * displayed because of the update of the view
     */
    private boolean exist = false;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnRegister = view.findViewById(R.id.btn_register);

        mFullName = view.findViewById(R.id.register_name);
        mEmail = view.findViewById(R.id.register_email);
        mPassword = view.findViewById(R.id.register_password);
        mPasswordConf = view.findViewById(R.id.register_repassword);
        mCustomerCode = view.findViewById(R.id.register_customer_code);
        mPhoneNumber = view.findViewById(R.id.register_phone_number);
        mBirthDayDate = view.findViewById(R.id.register_birthday_date);

        ccp = view.findViewById(R.id.register_ccp);

        mPhoneNumber.setOnFocusChangeListener((view1, b) -> {
            ccp.enableHint(b);
            ccp.registerPhoneNumberTextView(mPhoneNumber);
        });

        mBirthDayDate.setOnClickListener(v -> new DatePickerDialog(v.getContext(), date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        btnRegister.setOnClickListener(v -> {

            // only if this field is true then the register will be done
            areRegDataCompleted = true;

            checkPhoneNumberValidity();
            checkCustomerCodeValidity();
            checkPasswordConfirmationValidity();
            checkPasswordValidity();
            checkEmailValidity();
            checkNameValidity();

            // Required fields are NOT OK
            if (!areRegDataCompleted) {
                focusView.requestFocus();
            }
            // Required fields are OK
            else {
                registerNewAccount();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentRegisterListener) {
            listener = (FragmentRegisterListener) getActivity();
        } else {
            throw new RuntimeException(context.toString() + "must implement FragmentRegisterListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void registerNewAccount() {
        try {
            final CustomerData customerData = new CustomerData();
            customerData.setFullName(mFullName.getText().toString());
            customerData.setEmail(mEmail.getText().toString());
            customerData.setCustomerCode(mCustomerCode.getText().toString());
            customerData.setCountryPhoneNumberPrefix(ccp.getSelectedCountryCodeWithPlus());
            // Set phone number only it is not null
            if (ccp.getPhoneNumber() != null) {
                customerData.setPhoneNumber(String.valueOf(ccp.getPhoneNumber().getNationalNumber()));
            }
            customerData.setBirthDayDate(mBirthDayDate.getText().toString());
            // Set default customer profile picture
            customerData.setProfilePictureIdentifier(FirebaseConstants.CUSTOMER_DEFAULT_PROFILE_PICTURE);
            // Encrypt password and set
            String passEncrypted = AESCrypt.encrypt(mPassword.getText().toString());
            customerData.setPassword(passEncrypted);

            DatabaseReference customerRefEndpoint = Database.getCustomerEndpoint();

            Query query = customerRefEndpoint.orderByChild(getString(R.string.customer_code_firebase_field)).equalTo(mCustomerCode.getText().toString());

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // User already registered into database
                    if (dataSnapshot.exists() && !exist) {
                        Toast.makeText(requireView().getContext(), getString(R.string.customer_code_exist), Toast.LENGTH_LONG).show();
                    }
                    // User does not exist into database, so it could be inserted
                    else {
                        DatabaseReference customerRefEndpointInsert = Database.getCustomerEndpoint();
                        AtomicBoolean isSet = new AtomicBoolean(false);
                        customerRefEndpointInsert.child(customerData.getCustomerCode()).setValue(customerData, (databaseError, databaseReference) -> {
                            // No error, new account created!
                            if (databaseError == null) {
                                if(!isSet.get()) {
                                    Log.e("GOING", "GOING");
                                    String email = String.valueOf(mEmail.getText().toString().toCharArray(), 0, mEmail.getText().toString().length());
                                    String pass = String.valueOf(mPassword.getText().toString().toCharArray(), 0, mPassword.getText().toString().length());
                                    Toast.makeText(requireView().getContext(), getString(R.string.new_account_created), Toast.LENGTH_LONG).show();
                                    listener.onRegisterDone(email, pass);
                                    clearRegistrationData();
                                    System.out.println(email);
                                    isSet.set(true);
                                }
                            }
                            // Error
                            else {
                                Toast.makeText(requireView().getContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                                Log.e(getString(R.string.title_register_fragment), databaseError.getMessage());
                            }
                        });
                        customerRefEndpointInsert.push();
                        exist = true;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(getString(R.string.title_register_fragment), databaseError.getMessage());
                }
            };
            query.addListenerForSingleValueEvent(valueEventListener);
        } catch (Exception e) {
            Log.e(getString(R.string.title_register_fragment), Objects.requireNonNull(e.getMessage()));
        }
    }

    private void clearRegistrationData() {
        mFullName.setText("");
        mEmail.setText("");
        mPassword.setText("");
        mPasswordConf.setText("");
        mCustomerCode.setText("");
        mPhoneNumber.setText("");
        mBirthDayDate.setText("");
    }

    private void checkNameValidity() {
        // If this field is empty => error with focus on this field
        if (TextUtils.isEmpty(mFullName.getText())) {
            mFullName.setError(getString(R.string.error_field_required));
            focusView = mFullName;
            areRegDataCompleted = false;
        }
        // Otherwise => no error
        else {
            mFullName.setError(null);
        }
    }

    private void checkEmailValidity() {
        // If this field is empty => error with focus on this field
        if (TextUtils.isEmpty(mEmail.getText())) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            areRegDataCompleted = false;
        }
        // Check email format validity
        else if (!Validator.isEmailValid(mEmail.getText().toString())) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            areRegDataCompleted = false;
        }
        // Otherwise => no error
        else {
            mEmail.setError(null);
        }
    }

    private void checkPasswordValidity() {
        // If this field is empty or has less then 6 characters => error with focus on this field
        if (TextUtils.isEmpty(mPassword.getText()) || mPassword.getText().toString().length() < 6) {
            mPassword.setError(getString(R.string.error_invalid_password_length));
            focusView = mPassword;
            areRegDataCompleted = false;
        }
        // Check password validity - contains at least one digit, one uppercase letter and one lowercase latter
        else if (!Validator.isPasswordValid(mPassword.getText().toString())) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            areRegDataCompleted = false;
        }
        // Otherwise => no error
        else {
            mPassword.setError(null);
        }
    }

    private void checkPasswordConfirmationValidity() {
        // If this field is empty or has less then 6 characters => error with focus on this field
        if (TextUtils.isEmpty(mPasswordConf.getText()) || mPassword.getText().toString().length() < Constants.MIN_PASSWORD_LENGTH) {
            mPasswordConf.setError(getString(R.string.error_invalid_password_length));
            focusView = mPasswordConf;
            areRegDataCompleted = false;
        }
        // Check password validity - contains at least one digit, one uppercase letter and one lowercase latter
        else if (!Validator.isPasswordValid(mPasswordConf.getText().toString())) {
            mPasswordConf.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordConf;
            areRegDataCompleted = false;
        }
        // Password field and confirmation password field don't match
        else if (!mPassword.getText().toString().equals(mPasswordConf.getText().toString())) {
            mPasswordConf.setError(getString(R.string.error_password_match));
            focusView = mPasswordConf;
            areRegDataCompleted = false;
        }
        // Otherwise => no error
        else {
            mPasswordConf.setError(null);
        }
    }

    private void checkCustomerCodeValidity() {
        // If this field is empty  => error with focus on this field
        if (TextUtils.isEmpty(mCustomerCode.getText())) {
            mCustomerCode.setError(getString(R.string.error_field_required));
            focusView = mCustomerCode;
            areRegDataCompleted = false;
        }
        // Otherwise => no error
        else {
            mPasswordConf.setError(null);
        }
    }

    private void checkPhoneNumberValidity() {
        // Optional field. So if it's empty => no error
        if (TextUtils.isEmpty(mPhoneNumber.getText())) {
            mPhoneNumber.setError(null);
        }
        // Phone number has between 9-15 digits
        else if (!Validator.isPhoneNumberValid(mPhoneNumber.getText().toString())) {
            mPhoneNumber.setError(getString(R.string.error_invalid_phone_number));
            focusView = mPhoneNumber;
            areRegDataCompleted = false;
        }
    }
}