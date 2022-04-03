package com.example.watermonitoringsystem.activities.customer;

import static com.example.watermonitoringsystem.authentication.LogoutHelper.logoutFromActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.activities.common.AboutAppActivity;
import com.example.watermonitoringsystem.activities.common.AppSupportActivity;
import com.example.watermonitoringsystem.activities.common.SensorsMapActivity;
import com.example.watermonitoringsystem.authentication.SharedPrefsKeys;
import com.example.watermonitoringsystem.firebase.Database;
import com.example.watermonitoringsystem.models.firebasedb.CustomerData;
import com.example.watermonitoringsystem.security.AESCrypt;
import com.example.watermonitoringsystem.utils.Utils;
import com.example.watermonitoringsystem.utils.Validator;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Customer personal profile activity
 *
 * @author Ioan-Alexandru Chirita
 */
public class CustomerPersonalProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_READ_STORAGE = 2;
    private static final int RESULT_LOAD_IMAGE = 2;

    private final Calendar myCalendar = Calendar.getInstance();
    TextView txtName;
    TextView txtEmail;
    CircleImageView imgProfile;
    private ImageView pictureProfileImgView;
    private TextView fullNameTxtView;
    private TextView emailTxtView;
    private TextView oldPasswordTxtView;
    private TextView newPasswordTxtView;
    private TextView newPasswordConfTxtView;
    private TextView customerCodeTxtView;
    private TextView phoneNumberTxtView;
    private TextView birthDayDateTxtView;
    private DatePickerDialog.OnDateSetListener date;
    private CustomerData customerFromDb;
    private String customerCode;
    private String newProfilePictureLocation;
    private CountryCodePicker ccp;
    private volatile boolean isProfileDataCompleted;
    private volatile boolean isProfilePasswordCompleted;
    private View focusViewData = null;
    private View focusViewPassword = null;
    private Target target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.customer_personal_profile_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_customer);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);

        // Drawer Layout
        DrawerLayout drawer = findViewById(R.id.drawer_layout_customer_personal_profile);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view_customer);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_personal_data);
        View headerLayout = navigationView.getHeaderView(0);

        txtName = headerLayout.findViewById(R.id.user_nav_header);
        txtEmail = headerLayout.findViewById(R.id.email_nav_header);
        imgProfile = headerLayout.findViewById(R.id.profile_picture_nav_header);

        customerCode = Utils.getValueFromSharedPreferences(SharedPrefsKeys.KEY_CUSTOMER_CODE, CustomerPersonalProfileActivity.this);

        Utils.getCustomerProfileFromDatabase(customerCode, txtName, txtEmail, imgProfile);

        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (bitmap != null) {
                    pictureProfileImgView.setImageBitmap(bitmap);
                    new AlertDialog.Builder(CustomerPersonalProfileActivity.this).setTitle("Loading ...")
                            .setMessage("The new profile picture will be set! Please wait!")
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            })
                            .show();

                    updateProfilePicture();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };


        date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
            birthDayDateTxtView.setText(sdf.format(myCalendar.getTime()));
        };

        pictureProfileImgView = findViewById(R.id.profile_picture);
        pictureProfileImgView.setTag(target);
        fullNameTxtView = findViewById(R.id.profile_full_name);
        emailTxtView = findViewById(R.id.profile_email);
        oldPasswordTxtView = findViewById(R.id.profile_old_password);
        newPasswordTxtView = findViewById(R.id.profile_new_password);
        newPasswordConfTxtView = findViewById(R.id.profile_retype_new_password);
        customerCodeTxtView = findViewById(R.id.profile_customer_code);
        phoneNumberTxtView = findViewById(R.id.profile_phone_number);
        birthDayDateTxtView = findViewById(R.id.profile_birthday_date);
        ccp = findViewById(R.id.profile_ccp);

        fillViewWithDataFromDatabase();

        phoneNumberTxtView.setOnFocusChangeListener((view, b) -> {
            ccp.enableHint(b);
            ccp.registerPhoneNumberTextView(phoneNumberTxtView);
        });

        birthDayDateTxtView.setOnClickListener(v -> new DatePickerDialog(v.getContext(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        pictureProfileImgView.setOnClickListener(v -> uploadImage());

        Button btnUpdateProfileData = findViewById(R.id.btn_update_data);
        btnUpdateProfileData.setOnClickListener(v -> updateProfileData());

        Button btnUpdateProfilePassword = findViewById(R.id.btn_update_password);
        btnUpdateProfilePassword.setOnClickListener(v -> updateProfilePassword());
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_sensors) {
            startActivity(new Intent(this, SensorsMapActivity.class));
            finish();
        } else if (id == R.id.nav_home_customer) {
            startActivity(new Intent(this, CustomerDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_complaints) {
            startActivity(new Intent(this, CustomerComplaintsActivity.class));
            finish();
        } else if (id == R.id.nav_app_support) {
            startActivity(new Intent(this, AppSupportActivity.class));
            finish();
        } else if (id == R.id.nav_about_app) {
            startActivity(new Intent(this, AboutAppActivity.class));
            finish();
        } else if (id == R.id.nav_sign_out) {
            logoutFromActivity(this);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_customer_personal_profile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Executed after a picture was selected
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Picasso.get().load(data.getData()).into(target);
        }
    }

    private void fillViewWithDataFromDatabase() {
        Database.getCustomerEndpoint().orderByChild(getString(R.string.customer_code_firebase_field)).equalTo(customerCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // Customer is registered on the database
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        customerFromDb = postSnapshot.getValue(CustomerData.class);
                        assert customerFromDb != null;
                        fullNameTxtView.setText(customerFromDb.getFullName());
                        emailTxtView.setText(customerFromDb.getEmail());
                        customerCodeTxtView.setText(customerFromDb.getCustomerCode());
                        phoneNumberTxtView.setText(customerFromDb.getPhoneNumber());
                        birthDayDateTxtView.setText(customerFromDb.getBirthDayDate());
                        newProfilePictureLocation = customerFromDb.getProfilePictureIdentifier();
                        ccp.setDefaultCountryUsingNameCode(customerFromDb.getCountryPhoneNumberPrefix());
                        ccp.registerPhoneNumberTextView(phoneNumberTxtView);
                        Utils.downloadImageFromFirebaseStorage(customerFromDb.getProfilePictureIdentifier(), pictureProfileImgView);
                    }
                }
                // Customer is not registered into database
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.customer_does_not_exist_into_database), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                Log.e(getString(R.string.title_customer_personal_profile), databaseError.getMessage());
            }
        });
    }

    private void checkEmail() {
        if (TextUtils.isEmpty(emailTxtView.getText())) {
            emailTxtView.setError(getString(R.string.error_field_required));
            focusViewData = emailTxtView;
            isProfileDataCompleted = false;
        } else if (!Validator.isEmailValid(emailTxtView.getText().toString())) {
            emailTxtView.setError(getString(R.string.error_invalid_email));
            focusViewData = emailTxtView;
            isProfileDataCompleted = false;
        } else {
            emailTxtView.setError(null);
        }
    }

    private void checkPhoneNumber() {
        if (TextUtils.isEmpty(phoneNumberTxtView.getText())) {
            phoneNumberTxtView.setError(getString(R.string.error_field_required));
            focusViewData = phoneNumberTxtView;
            isProfileDataCompleted = false;
        } else if (!Validator.isPhoneNumberValid(phoneNumberTxtView.getText().toString())) {
            phoneNumberTxtView.setError(getString(R.string.error_invalid_phone_number));
            focusViewData = phoneNumberTxtView;
            isProfileDataCompleted = false;
        } else {
            phoneNumberTxtView.setError(null);
        }
    }

    private void checkBirthdayDate() {
        if (TextUtils.isEmpty(birthDayDateTxtView.getText())) {
            birthDayDateTxtView.setError(getString(R.string.error_field_required));
            focusViewData = birthDayDateTxtView;
            isProfileDataCompleted = false;
        } else {
            birthDayDateTxtView.setError(null);
        }
    }

    private void checkPassword() {
        // OLD PASSWORD validity - is not empty and has at least 1 upper letter, 1 number and 1 lower letter
        if (TextUtils.isEmpty(oldPasswordTxtView.getText()) || oldPasswordTxtView.getText().toString().length() < 6) {
            oldPasswordTxtView.setError(getString(R.string.error_invalid_password_length));
            focusViewPassword = oldPasswordTxtView;
            isProfilePasswordCompleted = false;
        } else if (!Validator.isPasswordValid(oldPasswordTxtView.getText().toString())) {
            oldPasswordTxtView.setError(getString(R.string.error_invalid_password));
            focusViewPassword = oldPasswordTxtView;
            isProfilePasswordCompleted = false;
        } else {
            oldPasswordTxtView.setError(null);
        }

        // NEW PASSWORD validity - is not empty and has at least 1 upper letter, 1 digit and 1 lower letter
        if (TextUtils.isEmpty(newPasswordTxtView.getText()) || newPasswordTxtView.getText().toString().length() < 6) {
            newPasswordTxtView.setError(getString(R.string.error_invalid_password_length));
            focusViewPassword = newPasswordTxtView;
            isProfilePasswordCompleted = false;
        } else if (!Validator.isPasswordValid(newPasswordTxtView.getText().toString())) {
            newPasswordTxtView.setError(getString(R.string.error_invalid_password));
            focusViewPassword = newPasswordTxtView;
            isProfilePasswordCompleted = false;
        } else if (newPasswordTxtView.getText().toString().equals(oldPasswordTxtView.getText().toString())) {
            newPasswordTxtView.setError(getString(R.string.error_old_new_password_match));
            focusViewPassword = newPasswordTxtView;
            isProfilePasswordCompleted = false;
        } else {
            newPasswordTxtView.setError(null);
        }

        // NEW_PASSWORD_CONFIRM validity - is not empty and has at least 1 upper letter, 1 digit and 1 lower letter and match with new password
        if (TextUtils.isEmpty(newPasswordConfTxtView.getText()) || newPasswordConfTxtView.getText().toString().length() < 6) {
            newPasswordConfTxtView.setError(getString(R.string.error_invalid_password_length));
            focusViewPassword = newPasswordConfTxtView;
            isProfilePasswordCompleted = false;
        } else if (!Validator.isPasswordValid(newPasswordConfTxtView.getText().toString())) {
            newPasswordTxtView.setError(getString(R.string.error_invalid_password));
            focusViewPassword = newPasswordConfTxtView;
            isProfilePasswordCompleted = false;
        } else if (!newPasswordConfTxtView.getText().toString().equals(newPasswordTxtView.getText().toString())) {
            newPasswordConfTxtView.setError(getString(R.string.error_password_match));
            focusViewPassword = newPasswordConfTxtView;
            isProfilePasswordCompleted = false;
        } else {
            newPasswordConfTxtView.setError(null);
        }
    }

    private void updateProfileData() {
        isProfileDataCompleted = true;

        checkEmail();
        checkPhoneNumber();
        checkBirthdayDate();

        // Required fields are NOT OK
        if (!isProfileDataCompleted) {
            focusViewData.requestFocus();
        }
        // Required fields are OK
        else {
            final CustomerData customerData = new CustomerData();
            // Possible changed values
            customerData.setEmail(emailTxtView.getText().toString());
            customerData.setCountryPhoneNumberPrefix(ccp.getSelectedCountryCodeWithPlus());
            customerData.setPhoneNumber(String.valueOf(ccp.getPhoneNumber().getNationalNumber()));
            customerData.setBirthDayDate(birthDayDateTxtView.getText().toString());
            customerData.setProfilePictureIdentifier(newProfilePictureLocation);
            // These are unchangeable values
            customerData.setFullName(customerFromDb.getFullName());
            customerData.setPassword(customerFromDb.getPassword());
            customerData.setCustomerCode(customerFromDb.getCustomerCode());

            DatabaseReference customerEndpoint = Database.getCustomerEndpoint();
            customerEndpoint.child(customerCode).setValue(customerData, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.customer_profile_data_updated), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                    Log.e(getString(R.string.title_customer_personal_profile), databaseError.getMessage());
                }
            });
            customerEndpoint.push();
        }
    }

    private void updateProfilePassword() {
        isProfilePasswordCompleted = true;

        checkPassword();

        // Required fields are NOT OK
        if (!isProfilePasswordCompleted) {
            focusViewPassword.requestFocus();
        }
        // Required fields are OK
        else {
            Database.getCustomerEndpoint().orderByChild(getString(R.string.customer_code_firebase_field)).equalTo(customerCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    // Customer is registered on the database
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            customerFromDb = postSnapshot.getValue(CustomerData.class);
                            assert customerFromDb != null;
                            try {
                                if (!AESCrypt.decrypt(customerFromDb.getPassword()).equals(oldPasswordTxtView.getText().toString())) {
                                    new AlertDialog.Builder(CustomerPersonalProfileActivity.this).setTitle("Incorrect data!")
                                            .setMessage("The old password is not correct!")
                                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                            })
                                            .show();
                                } else {
                                    CustomerData customerData = new CustomerData();
                                    // These are unchangeable values
                                    customerData.setFullName(customerFromDb.getFullName());
                                    customerData.setEmail(customerFromDb.getEmail());
                                    customerData.setCustomerCode(customerFromDb.getCustomerCode());
                                    customerData.setCountryPhoneNumberPrefix(customerFromDb.getCountryPhoneNumberPrefix());
                                    customerData.setPhoneNumber(customerFromDb.getPhoneNumber());
                                    customerData.setBirthDayDate(customerFromDb.getBirthDayDate());
                                    customerData.setProfilePictureIdentifier(newProfilePictureLocation);
                                    // The only one possible changed value
                                    customerData.setPassword(AESCrypt.encrypt(newPasswordTxtView.getText().toString()));
                                    System.out.println(customerData.toString());
                                    DatabaseReference customerEndpoint = Database.getCustomerEndpoint();
                                    customerEndpoint.child(customerCode).setValue(customerData, (databaseError, databaseReference) -> {
                                        if (databaseError == null) {
                                            Toast.makeText(getApplicationContext(), getString(R.string.customer_profile_password_updated), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                                            Log.e(getString(R.string.title_customer_personal_profile), databaseError.getMessage());
                                        }
                                    });
                                    customerEndpoint.push();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // Customer is not registered into database
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.customer_does_not_exist_into_database), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                    Log.e(getString(R.string.title_customer_personal_profile), databaseError.getMessage());
                }
            });
        }
    }

    private void uploadImage() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(CustomerPersonalProfileActivity.this, "It requires permission for access your local storage!", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        }
    }

    private void updateProfilePicture() {

        String newPictureProfileIdentifier = Utils.composeCustomerProfilePictureName(customerFromDb.getCustomerCode(), customerFromDb.getFullName());
        Utils.uploadImageOnFirebaseStorage(newPictureProfileIdentifier, pictureProfileImgView, CustomerPersonalProfileActivity.this);

        // Update picture name in Firebase Real Database
        CustomerData customer = new CustomerData();
        // These are unchangeable values
        customer.setFullName(customerFromDb.getFullName());
        customer.setEmail(customerFromDb.getEmail());
        customer.setCustomerCode(customerFromDb.getCustomerCode());
        customer.setCountryPhoneNumberPrefix(customerFromDb.getCountryPhoneNumberPrefix());
        customer.setPhoneNumber(customerFromDb.getPhoneNumber());
        customer.setBirthDayDate(customerFromDb.getBirthDayDate());
        customer.setPassword(customerFromDb.getPassword());

        // The only one possible changed value
        customer.setProfilePictureIdentifier(newPictureProfileIdentifier);

        DatabaseReference customerEndpoint = Database.getCustomerEndpoint();
        customerEndpoint.child(customerCode).setValue(customer, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.customer_profile_picture_updated), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
                Log.e(getString(R.string.title_customer_personal_profile), databaseError.getMessage());
            }
        });
        customerEndpoint.push();
        Utils.getCustomerProfileFromDatabase(customerCode, txtName, txtEmail, imgProfile);
    }
}
