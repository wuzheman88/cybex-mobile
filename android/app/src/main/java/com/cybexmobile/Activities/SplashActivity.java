package com.cybexmobile.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.cybexmobile.Market.MarketStat;
import com.cybexmobile.R;

public class SplashActivity extends AppCompatActivity implements MarketStat.startFirstActivityListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if(!isNetworkAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("No Internet Connection, Please turn on Internet");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user pressed "yes", then he is allowed to exit from application
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MarketStat.getInstance().getWebSocketConnect(SplashActivity.this);

                }
            }, 2000);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void startToRunFirstActivity() {
        Intent i = new Intent(SplashActivity.this, NavButtonActivity.class);
        startActivity(i);
        finish();
    }
}
