package com.sky.libhttptest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class BookActivity extends Activity {

	private final static String TAG = "BookActivity";

	private final static int ADD_BOOK_INFO = 1;
	private final static int ADD_BOOK_FACE = 2;
	private final static int RESERVE_IS_OK = 3;
	private final static int RESERVE_IS_LOSE = 4;

	private final static String URL_HEAD = "http://210.32.33.91:8080/opac/";
	private final static String URL_RESERVE_HEAD = "http://210.32.33.91:8080/opac/userpreg_result.php?";

	private ImageView imageView;
	private ListView lv;
	private TextView tv_title;
	private TextView tv_author;
	private TextView tv_press;
	private Button reserveButton;
	public ProgressDialog dialog;
	
	private MyHandler mHandler;
	
	private boolean loginStatus;

	private List<Map<String, Object>> list;
	public SimpleAdapter adapter;

	public String url;
	public String title;
	public String author;
	public String press;

	public String address;

	
	public Bitmap pngBM;

	public String result;
	
	public MyApplication mApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book);
		
		mApp = ((MyApplication)getApplicationContext());
		
		dialog = new ProgressDialog(this);
		//dialog.setTitle("正在加载图书信息...");
		dialog.setMessage("正在加载图书信息...");
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		url = this.getIntent().getStringExtra("url");
		title = this.getIntent().getStringExtra("title");
		author = this.getIntent().getStringExtra("author");
		press = this.getIntent().getStringExtra("press");

		new HtmlThread().start();

		tv_title = (TextView) findViewById(R.id.book_name);
		tv_title.setText(title);
		tv_author = (TextView) findViewById(R.id.book_author);
		tv_author.setText(author);
		tv_press = (TextView) findViewById(R.id.book_press);
		tv_press.setText(press);

		lv = (ListView) findViewById(R.id.book_lv);
		imageView = (ImageView) findViewById(R.id.bookface);
		
		reserveButton =(Button) findViewById(R.id.button_reserve);
		reserveButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				loginStatus = mApp.getLoginStatus();
				if(loginStatus == true){
					Toast.makeText(BookActivity.this, "预约中！", Toast.LENGTH_LONG).show();
					new ReserveThread().start();
				}else{
					Toast.makeText(BookActivity.this, "使用预约功能请先登录！", Toast.LENGTH_SHORT).show();
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
			
			case ADD_BOOK_INFO:

				lv.setAdapter(adapter);
				adapter.notifyDataSetChanged();

				break;
			case ADD_BOOK_FACE:

				imageView.setImageBitmap(pngBM);
				break;
			case RESERVE_IS_OK:
				Toast.makeText(BookActivity.this, "预约成功！", Toast.LENGTH_SHORT).show();
				break;
			case RESERVE_IS_LOSE:
				Toast.makeText(BookActivity.this, "预约失败！", Toast.LENGTH_SHORT).show();
				break;
			}

		}
	}

	class HtmlThread extends Thread {

		@Override
		public void run() {

			result = new HttpUrl().get(URL_HEAD + url);

			Document doc = Jsoup.parse(result);
			Elements trs = doc.select("tr");
			int totalTrs = trs.size();

			if (totalTrs > 0) {

				list = new ArrayList<Map<String, Object>>();

				for (int i = 0; i < totalTrs - 1; i++) {

					Elements tds = trs.get(i + 1).select("td");
					int totalTds = tds.size();

					Map<String, Object> map = new HashMap<String, Object>();

					for (int j = 0; j < totalTds; j++) {

						switch (j) {

						case 0:
							address = tds.get(j).html().toString();
							map.put("book_address", address);
							break;
						case 3:
							map.put("book_school", tds.get(j).html().toString());
							break;
						case 4:
							map.put("book_collection", tds.get(j).html()
									.toString());
							break;
						case 5:
							if (tds.get(j).select("font").size() == 0)
								map.put("book_status", tds.get(j).html()
										.toString());
							else
								map.put("book_status", tds.get(j)
										.select("font").get(0).html()
										.toString());
							break;
						}
					}

					list.add(map);
					adapter = new SimpleAdapter(BookActivity.this, list,
							R.layout.book_info_item, new String[] {
									"book_address", "book_school",
									"book_collection", "book_status" },
							new int[] { R.id.book_address, R.id.book_school,
									R.id.book_collection, R.id.book_status });
				}

				// 提前传递List加快载入速度
				Message msg = new Message();
				msg.what = ADD_BOOK_INFO;
				mHandler.sendMessage(msg);

				// 把图片弄下来 图片是第五个 图片的加载方式待商榷
				Elements imgs = doc.select("img");
				String imgUrl = imgs.get(4).attr("src").toString();
				Log.i(TAG, "imgUrl = " + imgUrl);
				try {
					URL picUrl = new URL(imgUrl);
					pngBM = BitmapFactory.decodeStream(picUrl.openStream());
					Message imgmsg = new Message();
					imgmsg.what = ADD_BOOK_FACE;
					mHandler.sendMessage(imgmsg);
					Log.i(TAG, "img is ok");
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}

	class ReserveThread extends Thread {

		@Override
		public void run() {

			String mUrl = url.substring(9, url.length());
			String mAddress = address.replace("/", "%2F");

			result = new HttpUrl().get(URL_RESERVE_HEAD + mUrl
					+ "0&count=1&preg_days1=45&take_loca1=00003&callno1="
					+ mAddress + "&location1=00003&pregKeepDays1=5&check=1");
			
			Log.i(TAG,URL_RESERVE_HEAD + mUrl
					+ "0&count=1&preg_days1=45&take_loca1=00003&callno1="
					+ mAddress + "&location1=00003&pregKeepDays1=5&check=1");

			Document doc = Jsoup.parse(result);
			Elements ps = doc.select("p");
			int totalPs = ps.size();
			if (totalPs == 1) {
				Message msg = new Message();
				msg.what = RESERVE_IS_OK;
				mHandler.sendMessage(msg);
			}else{
				Message msg = new Message();
				msg.what = RESERVE_IS_LOSE;
				mHandler.sendMessage(msg);
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG, "onPause");
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

}
