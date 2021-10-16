package com.example.watermonitoringsystem;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.watermonitoringsystem.authentication.LoginFragment;
import com.example.watermonitoringsystem.authentication.RegisterFragment;
import com.example.watermonitoringsystem.firebase.Database;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

/**
 * @author Ioan-Alexandru Chirita
 */
public class MainActivity extends AppCompatActivity implements Serializable, RegisterFragment.FragmentRegisterListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Database.getDatabaseConnection().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(Boolean.class)) {
                    Toast.makeText(getApplicationContext(), "Firebase connection is up!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Firebase connection was lost!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(getString(R.string.title_login_fragment), "DatabaseError : " + error);
            }
        });

        ViewPager viewPager = findViewById(R.id.viewPager);
        AuthenticationPagerAdapter viewPagerAdapter = new AuthenticationPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onRegisterDone(String email, String password) {
        String newEmail = String.valueOf(email.toCharArray(), 0, email.length());
        String newPass =  String.valueOf(password.toCharArray(), 0, password.length());

        String tag = "android:switcher:" + R.id.viewPager + ":" + 0;
        LoginFragment login = (LoginFragment) getSupportFragmentManager().findFragmentByTag(tag);
        assert login != null;
        login.updateLoginDataAfterRegistration(newEmail, newPass);
    }

    public class AuthenticationPagerAdapter extends FragmentPagerAdapter {

        public AuthenticationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new LoginFragment();
                case 1:
                    return new RegisterFragment();
                default:
                    // This should never happen. Always account for each position above
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            if (position == 0) {
                title = getString(R.string.title_login_fragment);
            } else if (position == 1) {
                title = getString(R.string.title_register_fragment);
            }
            return title;
        }
    }
}