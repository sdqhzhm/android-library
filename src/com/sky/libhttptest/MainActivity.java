package com.sky.libhttptest;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {

	private final static String TAG = "MainActivity";

	private Button searchButton;
	private Button loginButton;
	private Button lendButton;
	private Button aboutButton;
	private Button searchButton2;

	public boolean loginStatus;
	public boolean netStatus;

	public MyApplication mApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mApp = ((MyApplication) getApplicationContext());
		
		netStatus = checkNetworkInfo();
		mApp.setNetStatus(netStatus);

		searchButton = (Button) findViewById(R.id.button1);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(MainActivity.this, SearchActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
			}

		});
		loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.in_from_top,
						R.anim.out_to_bottom);
			}
		});

		lendButton = (Button) findViewById(R.id.lendButton);
		lendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// ÅÐ¶Ï×´Ì¬
				loginStatus = mApp.getLoginStatus();

				if (loginStatus == false) {
					Toast.makeText(MainActivity.this, "ÇëÏÈµÇÂ¼", Toast.LENGTH_SHORT)
							.show();
				} else {
					Intent i = new Intent(MainActivity.this, InfoActivity.class);
					startActivity(i);
					overridePendingTransition(R.anim.in_from_left,
							R.anim.out_to_right);
				}

			}
		});

		aboutButton = (Button) findViewById(R.id.aboutButton);
		aboutButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				new AboutDialog(MainActivity.this).show();
			}
		});
		
		searchButton2 = (Button) findViewById(R.id.searchButton2);
		searchButton2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchButton.performClick();
			}
		});

	}
	
	private boolean checkNetworkInfo() {
		boolean netStatus = false;

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWifiConn = networkInfo.isConnected();
		networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileConn = false;
		if (networkInfo != null)
			isMobileConn = networkInfo.isConnected();
		Log.i(TAG, "Wifi connected: " + isWifiConn);
		Log.i(TAG, "Mobile connected: " + isMobileConn);

		if (isWifiConn || isMobileConn)
			netStatus = true;
		return netStatus;
	}


}
