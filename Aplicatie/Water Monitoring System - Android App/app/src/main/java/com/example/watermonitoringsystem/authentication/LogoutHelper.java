package com.example.watermonitoringsystem.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watermonitoringsystem.MainActivity;
import com.example.watermonitoringsystem.R;
import com.example.watermonitoringsystem.utils.Utils;

public class LogoutHelper {

    public static void logoutFromActivity(AppCompatActivity activity){
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Utils.clearSharedPreferences(activity);
        Toast.makeText(activity, R.string.logout_successfully, Toast.LENGTH_SHORT).show();
        LoginFragment.stopMqttConnections();
        activity.startActivity(intent);
        activity.finish();
    }
}
