package com.sky.libhttptest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.NavUtils;

public class SearchActivity extends Activity {
	private final static int ADD_BOOK_LIST = 1;
	private final static int GET_URL_RIGHT = 2;
	private final static int ADD_MORE_BOOK = 3;
	private final static int NO_MORE_BOOK = 4;
	private final static int ONLY_ONE_PAGE = 5;
	private final static int NO_ANY_BOOK = 6;
	private boolean NEXT_BOOKACTIVITY = false;

	private final static String TAG = "SearchActivity";

	private MyHandler mHandler;
	private MakeUrl makeUrl;

	private EditText editText;
	private ImageButton getButton;
	private ListView lv;
	private ProgressBar progressbar;
	private Button loadButton;
	private View loadView;
	public ProgressDialog dialog;
	public InputMethodManager imm;

	public boolean STATUS;
	public int maxBookNum;
	public int maxPageNum;
	public int nowPageNum;
	private List<Map<String, Object>> list;
	public SimpleAdapter adapter;

	public String result;
	public String getInput;
	public String beforeName;
	
	public MyApplication mApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		mApp = ((MyApplication)getApplicationContext());
		
		//检测网络状态
		STATUS = mApp.getNetStatus();
		
		beforeName = "";
		//进度dialog
		dialog = new ProgressDialog(this);
		//dialog.setTitle("正在加载图书列表...");
		dialog.setMessage("正在加载图书列表...");
		dialog.setCanceledOnTouchOutside(false);
		
		//软键盘管理
		imm = (InputMethodManager)getSystemService(SearchActivity.this.INPUT_METHOD_SERVICE); 
		
		getButton = (ImageButton) findViewById(R.id.button_get);
		lv = (ListView) findViewById(R.id.lv);
		editText = (EditText) findViewById(R.id.name_book);

		loadView = getLayoutInflater().inflate(R.layout.footer_morebook, null);
		loadButton = (Button) loadView.findViewById(R.id.button_loading);
		progressbar = (ProgressBar) loadView.findViewById(R.id.progressbar);

		getButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(STATUS == false){
					Toast.makeText(SearchActivity.this, "请连接有效网络", 
							Toast.LENGTH_SHORT).show();
				}else{
					
					progressbar.setVisibility(View.GONE);
					loadButton.setVisibility(View.VISIBLE);

					getInput = editText.getText().toString();
					Log.i(TAG, "Input=" + getInput + "!!!");
					if (getInput.equals("")) {
						Toast.makeText(SearchActivity.this, "请输入书名",
								Toast.LENGTH_SHORT).show();
					} else {
						getInput = getInput.replace(' ', '+');
						getInput = getInput.replace("\\", "");
						
						//隐藏软键盘 显示进度框
						imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); 
						dialog.show();
						
						// 需要在这里初始化部分变量 page
						makeUrl = new MakeUrl();
						new HtmlThread().start();
					}
				}
			}
		});

		// 检测enter键
		editText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					getButton.performClick();
					return true;
				}
				return false;
			}
		});
	
		//点击ListView中的item 获得其中书本的url传递给另一个activity
		lv.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				HashMap<String,String> map=(HashMap<String,String>)lv.getItemAtPosition(arg2);
				String title = map.get("book_title");
				String author = map.get("book_author");
				String press = map.get("book_press");
				String url = map.get("book_netaddress");
								
				Intent i = new Intent(SearchActivity.this,BookActivity.class);				
				
				i.putExtra("title", title);
				i.putExtra("author", author);
				i.putExtra("press", press);
				i.putExtra("url", url);
				startActivity(i);
				//标记动画
				NEXT_BOOKACTIVITY = true;
			}

		});
		loadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				progressbar.setVisibility(View.VISIBLE);
				loadButton.setVisibility(View.GONE);

				new HtmlMoreThread().start();
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

			case ADD_BOOK_LIST:
				// 添加底部View but如果上一次没有remove就再次点击get会添加重复的loadview
				// 添加底部View的条件应该为判断listview中现实的个数
				if (lv.getFooterViewsCount() == 0) {
					//使其不可进行 listview 之中的itemclick loadbutton仍可点击
					lv.addFooterView(loadView,null,false);
				}
				lv.setAdapter(adapter);
				Log.i("TAG", "FootViewNum=" + lv.getFooterViewsCount());
				break;
			case ONLY_ONE_PAGE:
				if (lv.getFooterViewsCount() != 0) {
					lv.removeFooterView(loadView);
				}
				lv.setAdapter(adapter);
				Toast.makeText(SearchActivity.this, "全部加载完成，没有更多图书！",
						Toast.LENGTH_SHORT).show();
				Log.i("TAG", "ONLY ONE PAGE");
				break;
			case ADD_MORE_BOOK:
				adapter.notifyDataSetChanged();
				progressbar.setVisibility(View.GONE);
				loadButton.setVisibility(View.VISIBLE);
				break;
			case NO_MORE_BOOK:
				adapter.notifyDataSetChanged();
				lv.removeFooterView(loadView);
				Toast.makeText(SearchActivity.this, "全部加载完成，没有更多图书！",
						Toast.LENGTH_SHORT).show();
				break;
			case NO_ANY_BOOK:
				Toast.makeText(SearchActivity.this, "本馆没有您检索的馆藏书目",
						Toast.LENGTH_SHORT).show();
				makeUrl.resetName();
				break;
			}

		}
	}

	// 9.15 1:06 需要查找strong 标签 第二个显示的即为查找结果总数 其实修改displaypg
	// 键值可以修改每页显示的个数，但是局限于100个
	class HtmlThread extends Thread {

		@Override
		public void run() {

			Message msg = new Message();

			result = new HttpUrl().get(makeUrl.getSearchUrl());
			//！！！！！建议加入一个判断result是否为空的代码
			Document doc = Jsoup.parse(result);
			// 原分析
			Elements trs = doc.select("tr");
			int totalTrs = trs.size();

			// trs 数值大于 0时 表示存在列表
			if (totalTrs > 0) {

				SearchActivity.this.list = new ArrayList<Map<String, Object>>();
				// 获取结果册数 与 结果页数 与 当前page键值
				Elements strongs = doc.select("strong");
				// Strong不存在时无法取值
				beforeName = makeUrl.searchName;
				Log.i(TAG, "beforeName = " + beforeName);
				String temMax = strongs.get(1).html().toString();
				SearchActivity.this.maxBookNum = Integer.parseInt(temMax);
				Log.i(TAG, "maxBookNum = " + maxBookNum);

				if (maxBookNum % 20 == 0) {
					maxPageNum = maxBookNum / 20;
				} else {
					maxPageNum = maxBookNum / 20;
					maxPageNum++;
				}
				Log.i(TAG, "maxPageNum = " + maxPageNum);

				nowPageNum = 2;

				for (int i = 0; i < totalTrs - 1; i++) {

					Elements tds = trs.get(i + 1).select("td");
					int totalTds = tds.size();

					Map<String, Object> map = new HashMap<String, Object>();

					for (int j = 0; j < totalTds; j++) {

						switch (j) {

						case 1:
							map.put("book_title", tds.get(j).select("a").get(0)
									.html().toString());
							map.put("book_netaddress",tds.get(j).select("a")
									.attr("href").toString());
							break;
						case 2:
							map.put("book_author", tds.get(j).html()
									.toString());
							break;
						case 3:
							map.put("book_press", tds.get(j).html()
									.toString());
							break;
						case 4:
							map.put("book_address", tds.get(j).html()
									.toString());
							break;
						}
					}

					SearchActivity.this.list.add(map);
					adapter = new SimpleAdapter(SearchActivity.this, list,
							R.layout.book_list_item, new String[] {
									"book_title","book_author","book_press", "book_address" }, new int[] {
									R.id.title, R.id.author, R.id.press, R.id.info });
				}
				// great 给handler传递一切的时候到了！！！！

				if (maxBookNum <= 20) {
					msg.what = ONLY_ONE_PAGE;
				} else {
					msg.what = ADD_BOOK_LIST;
				}
			} else {
				msg.what = NO_ANY_BOOK;
			}
			mHandler.sendMessage(msg);
		}
	}

	// 为了解析并且加载后几页的内容做的新类 线程类？
	class HtmlMoreThread extends Thread {
		@Override
		public void run() {

			Log.i(TAG, "nowPageNum = " + nowPageNum);
			// 原分析
			// MainActivity.this.list = new ArrayList<Map<String, Object>>();
			result = new HttpUrl().get(makeUrl.getPageUrl(nowPageNum++));
			Document doc = Jsoup.parse(result);

			Elements trs = doc.select("tr");
			int totalTrs = trs.size();

			if (totalTrs > 0) {
				for (int i = 0; i < totalTrs - 1; i++) {

					Elements tds = trs.get(i + 1).select("td");
					int totalTds = tds.size();

					Map<String, Object> map = new HashMap<String, Object>();

					for (int j = 0; j < totalTds; j++) {

						switch (j) {

						case 1:
							map.put("book_title", tds.get(j).select("a").get(0)
									.html().toString());
							map.put("book_netaddress",tds.get(j).select("a")
									.attr("href").toString());
							break;
						case 2:
							map.put("book_author", tds.get(j).html()
									.toString());
							break;
						case 3:
							map.put("book_press", tds.get(j).html()
									.toString());
							break;
						case 4:
							map.put("book_address", tds.get(j).html()
									.toString());
							break;
						}
					}

					SearchActivity.this.list.add(map);
				}

			}

			// 模拟点击下一页,到达maxPageNum send 结束Message 取出footerview
			if (nowPageNum <= maxPageNum) {
				Message moreMsg = new Message();
				moreMsg.what = ADD_MORE_BOOK;
				mHandler.sendMessage(moreMsg);
			} else {
				Message endMsg = new Message();
				endMsg.what = NO_MORE_BOOK;
				mHandler.sendMessage(endMsg);
			}

		}
	}

	//
	public class MakeUrl {

		String name;
		public String searchName;

		public MakeUrl() {
			name = SearchActivity.this.getInput;
			Log.i(TAG, name);
			try {
				searchName = new String(name.getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String getSearchUrl() {
			String url;
			url = "http://210.32.33.91:8080/opac/openlink.php?strSearchType=title&historyCount=1&strText="
					+ searchName
					+ "&doctype=ALL&match_flag=any&displaypg=20&sort=CATA_DATE&orderby=desc&showmode=list&dept=ALL";
			Log.i("MainActivity.getSearchUrl", url);
			return url;
		}

		public String getPageUrl(int page) {
			String url;
			url = "http://210.32.33.91:8080/opac/openlink.php?dept=ALL&title="
					+ searchName
					+ "&doctype=ALL&lang_code=ALL&match_flag=any&displaypg=20&showmode=list&orderby=DESC&sort=CATA_DATE&onlylendable=no&count="
					+ maxBookNum + "&page=" + page;
			Log.i("MainActivity.getSearchUrl", url);
			return url;
		}

		public void resetName() {
			Log.i("beforeName",beforeName);
			if (SearchActivity.this.beforeName.equals("") == false) {
				try {
					searchName = new String(beforeName.getBytes(), "UTF-8");
					Log.i(TAG, "resetName");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	
	
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	Log.i(TAG,"onPause");
    	if(NEXT_BOOKACTIVITY == true) {
    		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    		NEXT_BOOKACTIVITY = false;
    	}else{
    		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    	}
    }
    
}

