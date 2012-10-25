package com.sky.libhttptest;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class LoginActivity extends Activity {

	private final static String TAG = "LoginActivity";

	private final static int LOGIN_IS_OK = 1;
	private final static int LOGIN_IS_LOSE = 2;

	private final static String URL_LIB = "http://210.32.33.91:8080/reader/redr_verify.php";

	private Button loginButton;
	private EditText editNumber;
	private EditText editPasswd;
	public ProgressDialog dialog;

	private Handler mHandler;

	private String studentNumber;
	private String studentPasswd;
	private CookieStore cookieStore;

	private String result;

	private boolean netStatus;

	public MyApplication mApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mApp = ((MyApplication) getApplicationContext());
		netStatus = mApp.getNetStatus();
		
		dialog = new ProgressDialog(this);
		dialog.setMessage("正在登录...");
		dialog.setCanceledOnTouchOutside(false);

		editNumber = (EditText) findViewById(R.id.student_number);
		editPasswd = (EditText) findViewById(R.id.student_passwd);

		loginButton = (Button) findViewById(R.id.button_login);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (netStatus == true) {
					
					studentNumber = editNumber.getText().toString();
					studentPasswd = editPasswd.getText().toString();
					dialog.show();
					new LoginThread().start();
				} else {
					Toast.makeText(LoginActivity.this, "请连接有效网络",
							Toast.LENGTH_SHORT).show();
				}

			}

		});

		mHandler = new MyHandler();
	}

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			dialog.cancel();
			switch (msg.what) {
			case LOGIN_IS_OK:
				Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT)
						.show();
				LoginActivity.this.finish();
				break;
			case LOGIN_IS_LOSE:
				Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	}

	class LoginThread extends Thread {

		@Override
		public void run() {

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("number", studentNumber));
			params.add(new BasicNameValuePair("passwd", studentPasswd));
			params.add(new BasicNameValuePair("returnUrl", ""));
			params.add(new BasicNameValuePair("select", "cert_no"));
			try {
				HttpUrl postUrl = new HttpUrl();
				result = postUrl.post(URL_LIB, params);

				Log.i(TAG, result);

				Document doc = Jsoup.parse(result);

				Elements tds = doc.select("td");
				int totalTds = tds.size();
				Log.i(TAG, "totalTds=" + totalTds);
				if (totalTds == 2) {

					mApp.setLibcookie(true);

					Log.i(TAG, "setLibCookie is ok");
					Message msg = new Message();
					msg.what = LOGIN_IS_OK;
					mHandler.sendMessage(msg);
				} else {
					mApp.setLibcookie(false);

					Message msg = new Message();
					msg.what = LOGIN_IS_LOSE;
					mHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG, "onPause");
		overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
	}
}
